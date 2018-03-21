package com.example.bluetoothproject;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.RxBleScanResult;

import org.reactivestreams.Subscription;


import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.Subject;


public class MainActivity extends AppCompatActivity {

    RxBleClient rxBleClient;
    LocationManager locationManager;
    RxBleDevice device;
    String name;


    Disposable connect;
    ArrayList<ScanResult> results = new ArrayList<>();
    private Subscription scanSubscription;

    ListView lv;
    TextView errormessage;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("")
                        .setMessage("")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

    private void scan() {
        rxBleClient.scanBleDevices().subscribe(x -> {
            if (x.getBleDevice() != null) {
                //Log.d("BLUETOOTH", String.valueOf(x.getBleDevice().getMacAddress()));

            }
        });

    }

    private void flowDisposable() {
        rxBleClient.observeStateChanges()
                .switchMap(state -> { // switchMap makes sure that if the state will change the rxBleClient.scanBleDevices() will dispose and thus end the scan
                    errormessage = (TextView) findViewById(R.id.Error);
                    switch (state) {

                        case READY:
                            errormessage.setText("Good");
                            // everything should work
                            return rxBleClient.scanBleDevices();
                        case BLUETOOTH_NOT_AVAILABLE:
                            errormessage.setText("Bluetooth not available");
                            // basically no functionality will work here
                        case LOCATION_PERMISSION_NOT_GRANTED:
                            errormessage.setText("LOCATION_PERMISSION_NOT_GRANTED");
                            // scanning and connecting will not work
                        case BLUETOOTH_NOT_ENABLED:
                            errormessage.setText("BLUETOOTH NOT ENABLED");
                            // scanning and connecting will not work
                        case LOCATION_SERVICES_NOT_ENABLED:
                            errormessage.setText("LOCATION_SERVICES_NOT_ENABLED:");
                            // scanning will not work
                        default:
                            return Observable.empty();
                    }
                })
                .subscribe(
                        rxBleScanResult -> {
                            // Process scan result here.
                        },
                        throwable -> {
                            // Handle an error here.
                        }
                );
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.listView);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        int REQUEST_ENABLE_BT = 1;
        this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        rxBleClient = RxBleClient.create(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLocationPermission();
        scan();
        //flowDisposable();
        errormessage = (TextView) findViewById(R.id.Error);
        errormessage.setText("hi");
    }

    boolean connected;
    String message;
    RxBleConnection rx;

    public void Onconnect(View view) {
        name = "98:7B:F3:5A:CE:D9";

        device = rxBleClient.getBleDevice(name);

        if (connect == null) {
            connect = device.establishConnection(false) // <-- autoConnect flag
                    .subscribe(
                            this::isConnected
                    );

        }


        if (connected == true) {
            errormessage = findViewById(R.id.Error);
            //Log.d("device", String.valueOf(rx.readCharacteristic()));
            //message=String.valueOf(rx.readCharacteristic());
            errormessage.setText("Ellen");
        }

    }

    public void isConnected(RxBleConnection connection) {
        Log.d("BLE", "CONNECTED");
        rxBleClient = null;
        errormessage = findViewById(R.id.Error);
        errormessage.setText(connection.getMtu());
    }


}