package com.mlst.servo;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;

public final class UsbAccessoryActivity extends Activity {

    static final String TAG = "UsbAccessoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	Intent intent = new Intent(this, Main.class);
	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	try {
	    startActivity(intent);
	} catch (ActivityNotFoundException e) {

	}
	finish();
    }
}
