package com.example.combination;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public class MainActivity extends Activity {

	private Tasker mChildTasker = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mChildTasker = new ChildTasker();
		AsyncTask<String, String, String> tasker = mChildTasker.execute("");
		tasker.execute("");
	}

	@Override
	protected void onDestroy() {
		if (mChildTasker != null) {
			mChildTasker.cancel(true);
		}
		super.onDestroy();
	}

}
