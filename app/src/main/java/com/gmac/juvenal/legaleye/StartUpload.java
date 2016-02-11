package com.gmac.juvenal.legaleye;


import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Michael Brooks on 2/10/2016.
 */
public class StartUpload {

    private String myVideoPath;
    private URL url;
    HttpURLConnection urlConnection = null;
    FileInputStream fileInpustStream = null;
    byte[] dataToServer;


    public StartUpload(String filePath) {
        myVideoPath = filePath;
        try {
            fileInpustStream = new FileInputStream(Environment.getExternalStorageDirectory().toString() + filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }


    public class StartVideoUpload extends AsyncTask<Void, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(Void... params) {

            String responseString = null;
            String Tag = "fSnd";
            try {
                url = new URL(Config.FILE_UPLOAD_URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Connection", "Keep-Alive");
                DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                int bytesAvailable = fileInpustStream.available();
                int maxBufferSize = 1024;
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                byte[] buffer = new byte[bufferSize];
                int bytesRead = fileInpustStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInpustStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInpustStream.read(buffer, 0, bufferSize);
                }

                fileInpustStream.close();
                dos.flush();

                Log.e(Tag, "File Sent, Response: " + String.valueOf(urlConnection.getResponseCode()));

                InputStream is = urlConnection.getInputStream();

                // retrieve the response from server
                int ch;

                StringBuffer b = new StringBuffer();
                while ((ch = is.read()) != -1) {
                    b.append((char) ch);
                }
                responseString = b.toString();
                Log.i("Response", responseString);
                dos.close();

            } catch (MalformedURLException ex)

            {
                Log.e(Tag, "URL error: " + ex.getMessage(), ex);
                //urlConnection.connect();
            } catch (IOException ioe)

            {
                Log.e(Tag, "IO error: " + ioe.getMessage());
                //Toast.makeText(this, "Invalid IP Address", Toast.LENGTH_LONG).show();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);
        }

    }


}


