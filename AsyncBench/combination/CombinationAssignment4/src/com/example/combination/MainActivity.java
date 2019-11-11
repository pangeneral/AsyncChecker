package com.example.combination;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public class MainActivity extends Activity {

	private Tasker mChildTasker = null;
	
	private Tasker mChildTasker1 = null;
	
	private Tasker mChildTasker2 = null;
	
	private Tasker mChildTasker3 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mChildTasker = new ChildTasker();
		mChildTasker1 = mChildTasker2 = mChildTasker3 = mChildTasker;
		
		mChildTasker.execute("");
		mChildTasker1.execute("");
		mChildTasker2.execute("");
		mChildTasker3.execute("");
		
	}

	@Override
	protected void onDestroy() {
		if (mChildTasker != null) {
			mChildTasker.cancel(true);
		}
		super.onDestroy();
	}

}
