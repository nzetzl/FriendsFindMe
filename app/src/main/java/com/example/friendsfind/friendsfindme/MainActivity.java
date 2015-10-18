package com.example.friendsfind.friendsfindme;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    public static String phoneNumber = "234902834";
    public static String[] friendNumbers = {"1","234098"};
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("690c8102-7031-44c6-9bc5-60487c416306");
    public String m_Text1;
    public String m_Text2;
    public ArrayList<Entries> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //contact list
        contacts = new ArrayList<Entries>();
        Entries entry1 = new Entries("Bob", "098234");
        Entries entry2 = new Entries("Sally", "08234908");
        contacts.add(entry1);
        contacts.add(entry2);
        ListAdapter adapter = new ArrayAdapter<Entries>(
                this,
                android.R.layout.simple_list_item_1,
                contacts);
        ListView contactsList = (ListView) findViewById(R.id.contactsListView);
        contactsList.setAdapter(adapter);
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        //Check if gps is enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }



        // Register the listener with the Location Manager to receive location
        // updates
        final MyLocationManager manager = new MyLocationManager();
        try {
            locationManager.requestLocationUpdates(5000, 5f, new Criteria(), manager, Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.v("high", String.valueOf(e));
        }
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            String response = "";
            @Override
            public void run() {
                response = manager.get();
                //Response from server should be a json string
                Log.v("Hi", response);
                JSONObject obj;
                try {
                    obj = new JSONObject(response);
                } catch (JSONException e) {
                    Log.v("Hi", String.valueOf(e));
                    return;
                }
                if (obj.length() > 0) {
                    PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
                    PebbleDictionary data = new PebbleDictionary();
                    byte[] bytes = new byte[obj.length()];
                    //iterate over json object
                    //convert integer key to contact name, then send to watch
                    Iterator<String> iterator = obj.keys();
                    for(int i = 0; i < obj.length(); i++) {
                        String key = iterator.next();
                        int x = obj.optInt(key);
                        bytes[i] = (byte)x;
                        i++;
                        String name = getName(key);
                        data.addString(i, name);
                    }
                    //send data to watch
                    data.addBytes(0, bytes);
                    PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, data);
                } else {
                    PebbleKit.closeAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
                }
            }
        }, 0, 15, TimeUnit.SECONDS);


    }

    //displays alert if gps is not enabled and asks user to enable it
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
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
        if (id == R.id.action_add) {
            //create dialog to get input to create a new contact(name, number)
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add a Contact");
            final EditText input1 = new EditText(this);
            final EditText input2 = new EditText(this);
            input1.setInputType(InputType.TYPE_CLASS_TEXT);
            input2.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input1);
            builder.setView(input2);
            LinearLayout layout = new LinearLayout(getApplicationContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(input1);
            layout.addView(input2);
            builder.setView(layout);
            builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    m_Text1 = input1.getText().toString();
                    m_Text2 = input2.getText().toString();
                    Entries entry = new Entries(m_Text1, m_Text2);
                    contacts.add(entry);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getName(String key){
        int keyNum = Integer.parseInt(key);
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).id == keyNum) {
                return contacts.get(i).name;
            }
        }
        return "";
    }

    @Override
    protected void onResume() {
        super.onResume();

        //check if pebble is connected and display whether it is or not
    	boolean isConnected = PebbleKit.isWatchConnected(this);
        Toast.makeText(this, "Pebble " + (isConnected ? "is" : "is not") + " connected!", Toast.LENGTH_LONG).show();

    }
}

class Entries {
    public static int count = 0;
    public int id;
    public String name;
    public String number;

    public Entries(String name, String number) {
        this.name = name;
        this.number = number;
        this.id = count;
        count++;
    }

    @Override
    public String toString() {
        return "      " + this.name + "          " + this.number;
    }
}