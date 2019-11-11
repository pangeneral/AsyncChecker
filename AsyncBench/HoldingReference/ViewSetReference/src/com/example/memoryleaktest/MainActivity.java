package com.example.memoryleaktest;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

	private SetReferenceAsyncTask mSetAsyncTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Set<View> set = new HashSet<View>();
		set.add(findViewById(R.id.view1));
		mSetAsyncTask = new SetReferenceAsyncTask(set);
		mSetAsyncTask.execute("");
		mSetAsyncTask.cancel(true);
		
	}

}
