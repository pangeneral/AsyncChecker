package com.example.combination;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Tasker mTasker = null;

	private Tasker mTasker2 = null;

	private Tasker mTasker3 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTasker2 = new Tasker((TextView) findViewById(R.id.view1));
		mTasker3 = new Tasker((TextView) findViewById(R.id.view1));
		mTasker3.execute("");
		findViewById(R.id.view1).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mTasker2.cancel(true);
			}
		});
		findViewById(R.id.button1).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mTasker2 = new Tasker((TextView) findViewById(R.id.view1));
			}
		});
		mTasker2.execute("");
	}

	@Override
	protected void onResume() {
		mTasker.execute("");
		mTasker2.execute("");
		super.onResume();
	}

	@Override
	protected void onPause() {
		mTasker = new Tasker((TextView) findViewById(R.id.view1));
		super.onPause();
	}

}
