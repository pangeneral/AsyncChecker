package com.example.combination;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	private Tasker mTasker1 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		A();
	}

	private void A() {
		if (mTasker1 != null) {
			mTasker1.cancel(true);
		}
		B();
		C();
	}

	private void C() {	
		mTasker1.execute("");
		A();
	}

	private void B() {
		mTasker1 = new Tasker();
		C();
		D();
		E();
	}

	private void E() {
		mTasker1.cancel(true);
		C();
	}

	private void D() {
		E();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
