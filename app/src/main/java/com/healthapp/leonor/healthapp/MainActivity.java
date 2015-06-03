package com.healthapp.leonor.healthapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtnClick();
        bluetoothConfigurationBtnClick();
        salirBtnClick();
    }

    private void startBtnClick() {
        Button startBtn = (Button)findViewById(R.id.startButton);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, InitialActivity.class);
                startActivity(intent);
            }
        });
    }

    private void salirBtnClick() {
        Button salirBtn = (Button)findViewById(R.id.salirButton);

        salirBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtenemos el adaptador Bluetooth. Si es NULL, el dispositivo no posee Bluetooth.
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if(bluetoothAdapter.isEnabled()){
                    bluetoothAdapter.disable();
                }
                finish();
                System.exit(0);
            }
        });
    }

    private void bluetoothConfigurationBtnClick() {
        Button btConfigBtn = (Button)findViewById(R.id.btConfigButton);

        btConfigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, BluetoothConfigActivity.class);
                startActivity(intent);
            }
        });
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
