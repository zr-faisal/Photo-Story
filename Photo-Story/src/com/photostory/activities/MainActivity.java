package com.photostory.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Toast.makeText(getBaseContext(), "This is photo-story", 1000).show();
        Intent i = new Intent(MainActivity.this, CameraActivity.class);
        startActivity(i);
    }
}