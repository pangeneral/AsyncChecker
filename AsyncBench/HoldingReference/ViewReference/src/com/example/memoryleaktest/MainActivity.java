package com.example.memoryleaktest;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	private ViewReferenceAsyncTask mListAsyncTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mListAsyncTask = new ViewReferenceAsyncTask(findViewById(R.id.view1));
		mListAsyncTask.execute("");
		mListAsyncTask.cancel(true);
		
	}

}
