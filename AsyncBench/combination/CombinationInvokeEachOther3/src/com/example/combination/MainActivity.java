package com.example.combination;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	private Tasker mTasker1 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTasker1 = new Tasker();
		A();
	}

	private void A() {
		mTasker1.execute("");
		A();
	}

	@Override
	protected void onDestroy() {
		if(mTasker1!=null){
			mTasker1.cancel(true);
		}
		super.onDestroy();
	}

}
