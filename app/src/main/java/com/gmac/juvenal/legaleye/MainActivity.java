package com.gmac.juvenal.legaleye;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    private String fileName = "UserData";
    private String filePath = "/data/data/com.gmac.juvenal.legaleye/files/UserData";
    private String[] states;
    //private Spinner spinner;
    private SharedPreferences preferences;
    private EditText editEmailText;
    private EditText editPhoneText;
    //generic for testing need to find a way to use location for api <=23
    private String stringLatitude = "28.4158";
    private String stringLongitude = "81.2989";
    private String email;
    private String phone;
    private final String SEGMENT = "1";
    private String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File file = new File(filePath);
        if (file.exists()) {
            //getLatAndLong();
            initUserVarsFromFile();
            new JSONTask().execute(Config.SERVER_URL);

            Intent myIntent = new Intent(MainActivity.this, CamActivity.class);
            MainActivity.this.startActivity(myIntent);
        } else {
            setContentView(R.layout.activity_main);

//            states = getResources().getStringArray(R.array.states_list);
//            spinner = (Spinner) findViewById(R.id.state_spinner);
//
//            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, states);
//            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spinner.setAdapter(dataAdapter);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

//            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//            fab.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                }
//            });
        }
    }

    public void saveButtonClick(View v) {

        editEmailText = (EditText) findViewById(R.id.editText_email);
        editPhoneText = (EditText) findViewById(R.id.editText_phone);

        email = editEmailText.getText().toString();
        phone = editPhoneText.getText().toString();


        if (!isValidEmail(email)) {
            editEmailText.setError("Invalid Email");
            return;
        }

        if (!isValidMobile((phone))) {
            editPhoneText.setError("Invalid Phone Number");
            return;
        }

//        if (spinner.getSelectedItemPosition() == 0) {
//            Toast toast = Toast.makeText(getApplicationContext(), "Enter your state", Toast.LENGTH_SHORT);
//            toast.show();
//            return;
//        }

        FileOutputStream outputStream = null;
        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(email.getBytes());
            outputStream.write('\n');
            outputStream.write(phone.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        Intent myIntent = new Intent(MainActivity.this, CamActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    // validating email id
    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isValidMobile(String phone)
    {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    public void getLatAndLong() {
//        GPSTracker gpsTracker = new GPSTracker(this);
//
//        if (gpsTracker.getIsGPSTrackingEnabled()) {
//            stringLatitude = String.valueOf(gpsTracker.latitude);
//            stringLongitude = String.valueOf(gpsTracker.longitude);
//        } else {
//            gpsTracker.showSettingsAlert();
//        }
//    }

    public void initUserVarsFromFile() {
        String line = "";
        try {
            FileInputStream fStream = new FileInputStream(("/data/data/com.gmac.juvenal.legaleye/files/UserData"));
            DataInputStream in = new DataInputStream(fStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            if ((line = br.readLine()) != null) {
                email = line;
            }
            if ((line = br.readLine()) != null) {
               phone = line;
            }

        } catch (IOException iOE) {
            Log.e("LegaalEye", "exception:" + iOE.getMessage());
        }

    }

public class JSONTask extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            //connection.connect();

            JSONObject request = new JSONObject();
            try {
                request.put("segment", SEGMENT);
                request.put("emailAddress", email);
                request.put("phoneNumber", phone);
                request.put("latitude", stringLatitude);
                request.put("longitude", stringLongitude);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String postParam2 = "request=";
            //StringBuilder postParams2 = new StringBuilder(200); //"request=";
            //postParams2.append("request=");
            //postParams2.append(request.toString());
            postParam2 += request.toString();



            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setFixedLengthStreamingMode(postParam2.getBytes().length);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");


            //postParams2.(request.toString());
            PrintWriter out = new PrintWriter(connection.getOutputStream());
            out.print(postParam2);
            out.close();

            int statusCode = connection.getResponseCode();
            String message = connection.getResponseMessage();

            if (statusCode != HttpURLConnection.HTTP_CREATED) {
                return "could not find " + statusCode +"\n" + message + "\n" + postParam2;

            }

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            String finJson = buffer.toString();

            JSONObject responseObject = new JSONObject(finJson); //(finJson);
            //responseObject.getJSONObject("data");
//            JSONObject dataObject = parentObject.getJSONObject(finJson);
            String id = responseObject.getJSONObject("data").getString("session"); //.getString("session");
            String dialPhoneNum = responseObject.getJSONObject("data").getString("dial");
            String apiKey = responseObject.getJSONObject("data").getString("apiKey");
            UploadData.getInstance().setApiKey(apiKey);
            UploadData.getInstance().setSession(id);
            UploadData.getInstance().setDialNumber(dialPhoneNum);
            //return "id: " + id + " ,  dial number:" + dialPhoneNum + ",  " + "apikey: " + apiKey;


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                connection.disconnect();

            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //tvData.setText(result);
    }
}
}
