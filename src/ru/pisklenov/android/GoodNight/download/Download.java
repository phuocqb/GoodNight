package ru.pisklenov.android.GoodNight.download;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import ru.pisklenov.android.GoodNight.GN;
import ru.pisklenov.android.GoodNight.util.MD5Helper;

/**
 * Created by anpi0413 on 27.09.13.
 */
public class Download {
    private static final boolean DEBUG = GN.DEBUG;
    private static final String TAG = GN.TAG;

    private ActionOnCompleteListener onCompleteListener;

    //private Context context;
    private String fromUrl;
    private File outputFile;


    public Download(String fromUrl, File outputFile) {
        //this.context = context;
        this.fromUrl = fromUrl;
        this.outputFile = outputFile;
    }

    public void setOnCompleteListener(ActionOnCompleteListener listener) {
        this.onCompleteListener = listener;
    }

    private void onComplete(Boolean isCompleteOk) {
        if (onCompleteListener != null) {
            onCompleteListener.onComplete(isCompleteOk);
        }
    }

    public void startDownload() {
        boolean isCompleteOk = false;

        try {
            //set the download URL, a url that points to a file on the internet
            //this is the file to be downloaded
            //https://drive.google.com/uc?id=0B96N99jRte7mMk93eDczODliZ2c&export=download
            URL url = new URL(fromUrl);
            if (DEBUG) Log.i(TAG, "DownloadTask url ok");

            //create the new connection
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            if (DEBUG) Log.i(TAG, "DownloadTask urlConnection ok");

            //set up some things on the connection
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            //urlConnection.setSSLSocketFactory(MainActivity.this.getSocketFactory());

            //and connect!
            urlConnection.connect();
            if (DEBUG) Log.i(TAG, "DownloadTask connect ok");



            //this will be used to write the downloaded data into the file we created
            FileOutputStream fileOutput = new FileOutputStream(outputFile);

            //this will be used in reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            //this is the total size of the file
            int totalSize = urlConnection.getContentLength();
            //variable to store total downloaded bytes
            int downloadedSize = 0;

            //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0; //used to store a temporary size of the buffer

            //now, read through the input buffer and write the contents to the file
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                //add the data in the buffer to the file in the file output stream (the file on the sd card
                fileOutput.write(buffer, 0, bufferLength);
                //add up the size so we know how much is downloaded
                downloadedSize += bufferLength;


                //this is where you would do something to report the prgress, like this maybe
                //updateProgress(downloadedSize, totalSize);

                //if (DEBUG) Log.i(TAG, "downloadedSize = " + downloadedSize + " / " + totalSize);
            }
            //close the output stream when done
            fileOutput.close();

            if (DEBUG) Log.i(TAG, "DownloadTask OK");

            if (DEBUG) Log.i(TAG, "DownloadTask calculateMD5 " + MD5Helper.calculateMD5(outputFile));


            isCompleteOk = true;
        } catch (MalformedURLException e) {
            if (DEBUG) Log.e(TAG, "MalformedURLException " + e.getMessage());

            e.printStackTrace();
        } catch (IOException e) {
            if (DEBUG) Log.e(TAG, "IOException " + e.getMessage());
            if (DEBUG) Log.e(TAG, "IOException " + e.getLocalizedMessage());

            e.printStackTrace();
        }

        onComplete(isCompleteOk);
    }


    public interface ActionOnCompleteListener {
        public void onComplete(Boolean isCompleteOk);
    }
}
