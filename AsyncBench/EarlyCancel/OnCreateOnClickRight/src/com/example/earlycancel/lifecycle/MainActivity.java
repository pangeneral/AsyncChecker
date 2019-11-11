package com.example.earlycancel.lifecycle;

import com.example.earlycanceltest.R;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView mView;
	private AsyncTask<String, ?, ?> mTask2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mView = (TextView) findViewById(R.id.view1);
		
		mView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				mTask2.cancel(true);
			}
		});

		mTask2 = new Tasker(mView);
		mTask2.execute("");
	}

}
