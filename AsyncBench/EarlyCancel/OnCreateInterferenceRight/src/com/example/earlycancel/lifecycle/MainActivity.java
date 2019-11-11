package com.example.earlycancel.lifecycle;

import com.example.earlycanceltest.R;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView mView1;
	private AsyncTask<String, ?, ?> mAysncTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mView1 = (TextView) findViewById(R.id.view1);

		mAysncTask = new Tasker(mView1);
		@SuppressWarnings("unused")
		AsyncTask<String, ?, ?> excute = mAysncTask.execute("");
	}

	@Override
	protected void onDestroy() {
		if (mAysncTask != null) {
			mAysncTask.cancel(true);
		}
		super.onDestroy();
	}

}
