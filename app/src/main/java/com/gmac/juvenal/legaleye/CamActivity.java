package com.gmac.juvenal.legaleye;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;
import android.widget.ZoomControls;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CamActivity extends AppCompatActivity implements SurfaceHolder.Callback,
    MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {

    /**Global variable declarations. **/

    private Button btnStop = null;
    private Button btnDelete = null;
    private Button btnSave = null;
    private Button btnRotate = null;
    private VideoView vvCam = null;
    private SurfaceHolder holder = null;
    private Camera camera = null;
    private MediaRecorder recorder = null;
    private String fileName = null;
    private int videoNumber;
    private CamcorderProfile profile;
    private File dirQuickVid;
    protected static RandomAccessFile outFile;
    private String TAG = "VideoRecorder";
    private int orientation;
    private boolean rotated;
    private boolean stopClicked;
    private int currentZoomLevel;
    private int maxZoomLevel;
    private ZoomControls zoomControls;
    private Camera.Parameters params;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnStop = (Button) findViewById(R.id.btnSTOP);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnRotate = (Button) findViewById(R.id.btnRotate);
        vvCam = (VideoView) findViewById(R.id.vv1);
        zoomControls = (ZoomControls) findViewById(R.id.zoomControls);

        videoNumber = 1;
        currentZoomLevel = 0;
        maxZoomLevel = 0;


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    /**This method prepares the MediaRecroder for use. **/
    private void createRecorder() {

        Log.v(TAG, "In method createRecorder");
        if(recorder != null) return;

        /**Creates a new directory in the user's external storage. **/
        dirQuickVid = new File(Environment.getExternalStorageDirectory()
                + "/QuickVid/");

        dirQuickVid.mkdirs();

        if (!dirQuickVid.exists()) {
            if (!dirQuickVid.mkdirs()) {
                Log.e(TAG, "Problem creating directory");
                finish();
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        try {
            outFile = new RandomAccessFile(dirQuickVid.getPath() + File.separator + "VID_" + timeStamp + ".mp4", "rw");
        } catch (FileNotFoundException ex) {

        }
//        /**Loops through and chooses a filename not in use. **/
//        do
//        {
//            fileName = "QuickVid" + "(" + videoNumber + ")" + ".mp4";
//
//            outFile = new File(dirQuickVid.toString(),fileName);
//
//            videoNumber++;
//
//
//
//        }while(outFile.exists());
//

        /**This gives all the settings for the MediaRecorder. **/
        try
        {
            camera.unlock();

            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);

            recorder = new MediaRecorder();
            recorder.setCamera(camera);
            recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
            recorder.setVideoFrameRate(40);/**Frame Rate is 40. **/
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setMaxDuration(300000); /** Max recording time is 5 minutes. **/
            recorder.setPreviewDisplay(holder.getSurface());
            recorder.setOutputFile(outFile.getFD()); //(outFile)   //.getAbsolutePath());/**Sets what file to record to. **/

            /** Sets recorder orientation if screen is in portrait. **/
            if (orientation == Configuration.ORIENTATION_PORTRAIT ) {
                recorder.setOrientationHint(90);
            }

            recorder.prepare();
            Log.v("@string/TAG", "Recorder prepared");
            startRecord();
        }catch (Exception e)
        {
            Log.v(TAG, "MediaRecorder failed to start");
            e.printStackTrace();
        }

        new StartVideoUpload().execute();

//        StartUpload mStartUpload = new StartUpload(outFile.getPath());
//
//        new StartVideoUpload().execute();
////
//        mStartUpload.startUpload();

    }
    /**Button that stops the recording then plays it back. **/
    public void btnStopClick(View view) {

        Log.v(TAG, "In btnStopClick");
        stopRecord();

        stopClicked = true;
        btnStop.setEnabled(false);
        btnStop.setVisibility(View.INVISIBLE);
        btnRotate.setEnabled(false);
        btnStop.setVisibility(View.INVISIBLE);
        zoomControls.setEnabled(false);
        zoomControls.setVisibility(View.INVISIBLE);
        //playback();

    }
    /**Deletes the video. **/
    public void btnDeleteClick(View view) {

        Log.v(TAG, "In btnDeleteClick");
        vvCam.stopPlayback();

        //outFile.delete();

        AlertDialog.Builder msgDelete = new AlertDialog.Builder(this);
        msgDelete.setMessage("The video was deleted successfully");
        msgDelete.setTitle("Video Deleted");
        msgDelete.setCancelable(false);
        msgDelete.setPositiveButton("EXIT", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Dismiss the dialog
                exitApp();
            }
        });

        msgDelete.show();

    }

    /**Keeps the video and allows the user to share it. **/
    public void btnSaveClick(View view) {

        Log.v(TAG, "In btnSaveClick");
        vvCam.stopPlayback();

        AlertDialog.Builder msgSave = new AlertDialog.Builder(this);
        msgSave.setMessage("The Video Was Saved Successfully!");
        msgSave.setTitle("Video Saved");
        msgSave.setCancelable(false);
        msgSave.setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                exitApp();
            }
        });
        msgSave.setPositiveButton("SHARE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
               // shareVideo();

            }
        });

        msgSave.show();

    }

    /** Switches orientation when the user clicks . **/
//    public void btnRotateClick(View view) {
//
//
//        outFile.delete();
//
//        switch(orientation) {
//            case Configuration.ORIENTATION_PORTRAIT:
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                break;
//            case Configuration.ORIENTATION_LANDSCAPE:
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                break;
//        }
//
//        rotated = true;
//    }

    /**Method that stops the recording. **/
    private void stopRecord()
    {
        if (recorder != null)
        {
            recorder.setOnInfoListener(null);
            recorder.setOnErrorListener(null);
            try {
                recorder.stop();
            }catch (IllegalStateException ise)
            {
                Log.e(TAG, "ERROR - Got an Illegal State Exception, stopping the video.");
                ise.printStackTrace();
            }
            releaseRecorder();
            releaseCamera();

        }
    }
    /**Method  that auto starts the recording. **/
    public void startRecord() {
        recorder.setOnInfoListener(this);
        recorder.setOnErrorListener(this);
        recorder.start();
        btnStop.setEnabled(true);


    }
    /** Plays back the video. **/
//    public void playback() {
//
//        MediaController mc = new MediaController(this);
//
//
//        vvCam.setMediaController(mc);
//        vvCam.setVideoPath(outFile.getAbsolutePath());
//        vvCam.start();
//        btnDelete.setVisibility(View.VISIBLE);
//        btnDelete.setEnabled(true);
//        btnSave.setVisibility(View.VISIBLE);
//        btnSave.setEnabled(true);
//
//    }



    /** sets the camera output to the SurfaceHolder. also sets display orientation correctly **/
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        Log.v(TAG, "In Surface Created");
        try {
            camera.setPreviewDisplay(holder);


        } catch (IOException e) {
            Log.v(TAG, "ERROR - Could not start preview");
            e.printStackTrace();
        }

        if (orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            camera.setDisplayOrientation(90);
        }
        createRecorder();
    }


    /**Allows the user to zoom in and out during record. **/
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {


        if(params.isZoomSupported()){
            maxZoomLevel = params.getMaxZoom();

            zoomControls.setIsZoomInEnabled(true);
            zoomControls.setIsZoomOutEnabled(true);

            zoomControls.setOnZoomInClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    if(currentZoomLevel <  maxZoomLevel){
                        currentZoomLevel = currentZoomLevel + 3;
                        params.setZoom(currentZoomLevel);
                        camera.setParameters(params);
                    }
                }
            });

            zoomControls.setOnZoomOutClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    if(currentZoomLevel > 0){
                        currentZoomLevel = currentZoomLevel - 3;
                        params.setZoom(currentZoomLevel);
                        camera.setParameters(params);
                    }
                }
            }); }
        else
            zoomControls.setVisibility(View.GONE);


    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {

    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {

    }

    @Override
    protected void onPause()
    {
        super.onPause();

        /**Stops recording and stops the app, if the user leaves the app. if the user clicked the
         * rotated button the app won't stop and will restart. **/

        if(!stopClicked){
            stopRecord();
        }

        if(!rotated) {
            exitApp();
        }
    }

    /**Set ups buttons and gets the current orientation. **/
    @Override
    protected void onResume()
    {

        Log.v(TAG, "In OnResume");
        super.onResume();
        rotated = false;
        stopClicked = false;

        orientation = getResources().getConfiguration().orientation;
        btnStop.setEnabled(false);
        btnDelete.setEnabled(false);
        btnDelete.setVisibility(View.INVISIBLE);
        btnSave.setEnabled(false);
        btnSave.setVisibility(View.INVISIBLE);

        /** App exits if  an error occurs **/
        if (!buildRecorder()) {
            exitApp();
        }


    }
    /** Method that starts to build the first parts of the app, camera surface holder. **/
    private boolean buildRecorder() {
        try {
            camera = Camera.open();
            params = camera.getParameters();
            camera.lock();


            holder = vvCam.getHolder();
            holder.addCallback(this);

            holder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        } catch (RuntimeException re) {
            Log.v(TAG, "ERROR - Could not initialize camera");
            re.printStackTrace();
            return false;
        }

        return true;
    }

    /** Releases the camera object **/
    private void releaseCamera() {
        if(camera != null){
            try{
                camera.reconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.release();
            camera = null;
        }

    }
    /** Releases the MediaRecorder object. **/
    private void releaseRecorder() {
        if (recorder != null)
        {
            recorder.release();
            recorder = null;
        }
    }


    /** Method that enables the user to share to other apps. **/
//    private void shareVideo() {
//
//        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//
//        Uri video = Uri.fromFile(outFile);
//
//        sharingIntent.setType("video/mp4");
//        sharingIntent.putExtra(Intent.EXTRA_STREAM, video);
//        sharingIntent.putExtra(Intent.EXTRA_TEXT, "This Video Was Shared by The App QuickVid");
//        startActivity(Intent.createChooser(sharingIntent, "Share The Video Using..."));
//
//    }

    public void exitApp()
    {
        finish();
        System.exit(1);
    }


}

class StartVideoUpload extends AsyncTask<Void, Integer, String> {
    URL url;
    HttpURLConnection urlConnection = null;

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
            RandomAccessFile randFileInputStream = CamActivity.outFile;

            long bytesAvailable = randFileInputStream.length();     //.available();
            int maxBufferSize = 1024;
            int bufferSize = (int) Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];
            int bytesRead = randFileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = randFileInputStream.length();
                bufferSize = (int) Math.min(bytesAvailable, maxBufferSize);
                bytesRead = randFileInputStream.read(buffer, 0, bufferSize);
            }

            randFileInputStream.close();
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
