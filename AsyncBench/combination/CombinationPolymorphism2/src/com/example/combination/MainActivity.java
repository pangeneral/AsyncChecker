package com.example.combination;

import android.app.Activity;
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
		mChildTasker.execute("");
		
		mChildTasker2.execute("");
		
		mChildTasker3 = new ChildTasker();
		mChildTasker3.cancel(true);
	}

	@Override
	protected void onDestroy() {
		if (mChildTasker != null) {
			mChildTasker.cancel(true);
		}
		super.onDestroy();
	}

}
