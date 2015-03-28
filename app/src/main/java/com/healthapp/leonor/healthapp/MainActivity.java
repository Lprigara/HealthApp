package com.healthapp.leonor.healthapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fillTextViews();

        tempBtnClick();
        airBtnClick();
        pulseBtnClick();
        oxygenBtnClick();
        conductanceBtnClick();
        resistanceBtnClick();

    }

    public void fillTextViews(){
        TextView tempTextView = (TextView) findViewById(R.id.tempTextView);
        tempTextView.setText("Temp");
        TextView airTextView = (TextView) findViewById(R.id.airTextView);
        airTextView.setText("Air");
        TextView pulseTextView = (TextView) findViewById(R.id.pulseTextView);
        pulseTextView.setText("Pulse");
        TextView oxygenTextView = (TextView) findViewById(R.id.oxygenTextView);
        oxygenTextView.setText("Oxygen");
        TextView conductanceTextView = (TextView) findViewById(R.id.conductanceTextView);
        conductanceTextView.setText("Conduct");
        TextView resistanceTextView = (TextView) findViewById(R.id.resistanceTextView);
        resistanceTextView.setText("Resist");
    }

    private void tempBtnClick() {
        Button tempBtn = (Button)findViewById(R.id.tempButton);

        tempBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InfoTemperatureActivity.class);
                startActivity(intent);
            }
        });
    }

    private void airBtnClick() {
        Button airBtn = (Button)findViewById(R.id.airButton);

        airBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InfoAirActivity.class);
                startActivity(intent);
            }
        });
    }

    private void pulseBtnClick() {
        Button pulseBtn = (Button)findViewById(R.id.pulseButton);

        pulseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InfoPulseActivity.class);
                startActivity(intent);
            }
        });
    }

    private void oxygenBtnClick() {
        Button oxygenBtn = (Button)findViewById(R.id.oxygenButton);

        oxygenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InfoOxygenActivity.class);
                startActivity(intent);
            }
        });
    }

    private void conductanceBtnClick() {
        Button conductanceBtn = (Button)findViewById(R.id.conductanceButton);

        conductanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InfoConductanceActivity.class);
                startActivity(intent);
            }
        });
    }

    private void resistanceBtnClick() {
        Button resistanceBtn = (Button)findViewById(R.id.resistanceButton);

        resistanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InfoResistanceActivity.class);
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
