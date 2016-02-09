package com.gmac.juvenal.legaleye;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private String fileName = "UserData";
    private String filePath = "/data/data/com.gmac.juvenal.juvenal/files/UserData";
    private String[] states;
    private Spinner spinner;
    private SharedPreferences preferences;
    private EditText editNameText;
    private EditText editEmailText;
    private EditText editAddressText;
    private EditText editPhoneText;
    private EditText editCityText;
    private EditText editZipText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File file = new File(filePath);
        if (file.exists()) {
            Intent myIntent = new Intent(MainActivity.this, CamActivity.class);
            MainActivity.this.startActivity(myIntent);
        } else {
            setContentView(R.layout.activity_main);

            states = getResources().getStringArray(R.array.states_list);
            spinner = (Spinner) findViewById(R.id.state_spinner);

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, states);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(dataAdapter);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }
    }

    public void saveButtonClick(View v) {

        editEmailText = (EditText) findViewById(R.id.editText_email);
        editNameText = (EditText) findViewById(R.id.editText_name);
        editPhoneText = (EditText) findViewById(R.id.editText_phone);
        editAddressText = (EditText) findViewById(R.id.editText_address);
        editCityText = (EditText) findViewById(R.id.editText_city);
        editZipText = (EditText) findViewById(R.id.editText_zip);

        final String email = editEmailText.getText().toString();
        final String name = editNameText.getText().toString();
        final String phone = editPhoneText.getText().toString();
        final String address = editAddressText.getText().toString();
        final String city = editCityText.getText().toString();
        final String zip = editZipText.getText().toString();
        final String state = spinner.getSelectedItem().toString();

        if (!isValidEmail(email)) {
            editEmailText.setError("Invalid Email");
            return;
        }

        if (name == null || name.isEmpty()) {
            editNameText.setError("Invalid name");
            return;
        }

        if (address == null || address.isEmpty()) {
            editAddressText.setError("Invalid address");
            return;
        }

        if (phone == null || phone.isEmpty()) {
            editPhoneText.setError("Invalid phone");
            return;
        }

        if (city == null || city.isEmpty()) {
            editCityText.setError("Invalid city");
            return;
        }

        if (zip == null || zip.isEmpty()) {
            editZipText.setError("Invalid zip");
        }

        if (spinner.getSelectedItemPosition() == 0) {
            Toast toast = Toast.makeText(getApplicationContext(), "Enter your state", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        FileOutputStream outputStream = null;
        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(name.getBytes());
            outputStream.write(email.getBytes());
            outputStream.write(phone.getBytes());
            outputStream.write(address.getBytes());
            outputStream.write(city.getBytes());
            outputStream.write(zip.getBytes());
            outputStream.write(state.getBytes());
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


}
