package com.example.combination;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	private Tasker mTasker1 = null;
	
	private Tasker mTasker2 = null;
	
	private Tasker mTasker3 = null;
	
	private Tasker mTasker4 = null;
	
	private Tasker mTasker5 = null;
	
	private Tasker mTasker6 = null;
	
	private Test mTest = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTest = new Test();
		mTasker1 = mTest.newAsyncTask();
		mTest.start(mTasker1);
		
		
		mTasker2 = mTest.newAsyncTask();
		mTest.start(mTasker2);
		
		
		//mTest.cancel(mTasker3);
		
		mTasker4 = mTest.newAsyncTask();
		mTest.cancel(mTasker4);
		mTest.start(mTasker4);
		
		mTasker5= mTest.newAsyncTask();
		
		mTasker6 = mTest.newAsyncTaskChild(findViewById(R.id.view1));
		mTest.start(mTasker6);
	}
	
	@Override
	protected void onResume() {
		mTest.start(mTasker5);
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mTest.cancel(mTasker1);
		mTest.cancel(mTasker5);
//		mTest.cancel(mTasker6);
		super.onDestroy();
	}

}
