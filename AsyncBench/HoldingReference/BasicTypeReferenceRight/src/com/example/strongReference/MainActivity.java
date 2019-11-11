package com.example.strongReference;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	private BasicTypeReferenceAsyncTask mAsyncTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		byte bByte = '1';
		short bShort = 0;
		mAsyncTask = new BasicTypeReferenceAsyncTask(bByte, bShort, 0, 0l, 0f,
				0.0, false, 'A');
		mAsyncTask.execute("");
		mAsyncTask.cancel(true);
	}

}
