package ru.pisklenov.android.GoodNight.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import ru.pisklenov.android.GoodNight.GN;

/**
 * Created by anpi0413 on 27.09.13.
 */
public class InternetHelper {
    private static final boolean DEBUG = GN.DEBUG;
    private static final String TAG = GN.TAG;


    public static boolean isWIFIAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Toast.makeText(context, "None Available", Toast.LENGTH_SHORT).show();
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            for (NetworkInfo inf : info) {
                if (inf.getTypeName().contains("WIFI"))
                    if (inf.isConnected())
                        return true;
            }
        }
        return false;
    }


    public static void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }

    public static boolean download(Context context, String fromUrl, String toFile) {
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

            //set the path where we want to save the file
            //in this case, going to save it on the root directory of the
            //sd card.
            File availablePath = FileHelper.getAvailablePath(context);

            //File availablePath = Environment.getExternalStorageDirectory();
            //create a new file, specifying the path, and the filename
            //which we want to save the file as.
            File outputFile = new File(availablePath, toFile);
            if (DEBUG) Log.i(TAG, "DownloadTask file ok");

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

//catch some possible errors...
        } catch (MalformedURLException e) {
            if (DEBUG) Log.e(TAG, "MalformedURLException " + e.getMessage());

            e.printStackTrace();

            return false;
        } catch (IOException e) {
            if (DEBUG) Log.e(TAG, "IOException " + e.getMessage());
            if (DEBUG) Log.e(TAG, "IOException " + e.getLocalizedMessage());

            e.printStackTrace();

            return false;
        }

        return true;
    }
}
