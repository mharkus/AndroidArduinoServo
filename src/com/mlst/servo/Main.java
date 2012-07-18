package com.mlst.servo;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Main extends Activity implements OnClickListener {

    private static final String TAG = "Main";
    private static final String ACTION_USB_PERMISSION = "com.mlst.servo.Main.action.USB_PERMISSION";

    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private UsbAccessory mAccessory;
    private boolean mPermissionRequestPending;

    private Button left;
    private Button right;
    private Button crazy;
    private Thread t;
    private boolean run;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	left = (Button) findViewById(R.id.left);
	right = (Button) findViewById(R.id.right);
	crazy = (Button) findViewById(R.id.crazy);

	left.setOnClickListener(this);
	right.setOnClickListener(this);
	crazy.setOnClickListener(this);

	mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
	mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
	IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
	filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
	registerReceiver(mUsbReceiver, filter);
    }

    @Override
    protected void onResume() {
	super.onResume();

	if (input != null && output != null) {
	    return;
	}

	UsbAccessory[] accessories = mUsbManager.getAccessoryList();
	UsbAccessory accessory = (accessories == null ? null : accessories[0]);
	if (accessory != null) {
	    if (mUsbManager.hasPermission(accessory)) {
		openAccessory(accessory);
	    } else {
		synchronized (mUsbReceiver) {
		    if (!mPermissionRequestPending) {
			mUsbManager.requestPermission(accessory, mPermissionIntent);
			mPermissionRequestPending = true;
		    }
		}
	    }
	} else {
	    Log.d(TAG, "mAccessory is null");
	}
    }

    @Override
    protected void onPause() {
	super.onPause();
	unregisterReceiver(mUsbReceiver);
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

	@Override
	public void onReceive(Context context, Intent intent) {
	    String action = intent.getAction();

	    if (ACTION_USB_PERMISSION.equals(action)) {
		synchronized (this) {
		    UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
		    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
			openAccessory(accessory);
		    } else {
			Log.d(TAG, "permission denied for accessory " + accessory);
		    }

		    mPermissionRequestPending = false;
		}
	    } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
		UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
		if (accessory != null && accessory.equals(mAccessory)) {
		    closeAccessory();
		}
	    }
	}
    };
    private ParcelFileDescriptor fileDescriptor;
    private FileInputStream input;
    private FileOutputStream output;

    protected void openAccessory(UsbAccessory accessory) {
	fileDescriptor = mUsbManager.openAccessory(accessory);
	if (fileDescriptor != null) {
	    mAccessory = accessory;
	    FileDescriptor fd = fileDescriptor.getFileDescriptor();
	    input = new FileInputStream(fd);
	    output = new FileOutputStream(fd);
	    Log.d(TAG, "accessory opened");
	} else {
	    Log.d(TAG, "accessory open fail");
	}

    }

    protected void closeAccessory() {
	try {
	    if (fileDescriptor != null) {
		fileDescriptor.close();
	    }
	} catch (IOException e) {
	} finally {
	    fileDescriptor = null;
	    mAccessory = null;
	}

    }

    @Override
    public void onClick(View v) {
	if (t != null) {
	    stopThread();
	}

	switch (v.getId()) {
	case R.id.left: {

	    send(0);
	    break;
	}
	case R.id.right: {

	    send(170);
	    break;
	}
	case R.id.crazy: {
	    run = true;
	    t = new Thread() {
		public void run() {
		    while (run) {
			send(0);
			delay(500);
			send(170);
			delay(500);
		    }
		};
	    };

	    t.start();
	    break;
	}
	}

    }

    private void stopThread() {
	run = false;
	try {
	    t.join();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    private void delay(long ms) {
	try {
	    Thread.sleep(ms);
	} catch (Exception e) {

	}
    }

    protected void send(final int data) {

	try {
	    output.write(data);
	} catch (Exception e) {
	    Log.e(TAG, "error in sending data to accessory", e);
	}
    }

}