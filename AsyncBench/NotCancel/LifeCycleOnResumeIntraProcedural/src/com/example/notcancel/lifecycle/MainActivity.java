package com.example.notcancel.lifecycle;

import com.example.notcancel.R;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView view1;
	private AsyncTask task2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		view1 = (TextView) findViewById(R.id.view1);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		task2 = new Tasker(view1);
		task2.execute("");
	}

}
