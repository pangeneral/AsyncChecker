package com.example.combination;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	private Tasker mTasker1 = null;
	private Tasker mTasker2 = null;
	private Tasker mTasker3 = null;
	private Tasker mTasker4 = null;
	private Tasker mTasker5 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		A();
	}

	private void A() {
		mTasker1 = new Tasker();
		mTasker1.execute("");
		B();
		C();
	}

	private void C() {	
		mTasker3 = new Tasker();
		mTasker3.execute(""); 
	}

	private void B() {
		mTasker2 = new Tasker();
		mTasker2.execute("");
		C();
		D();
		E();
	}

	private void E() {
		mTasker5 = new Tasker();
		mTasker5.execute("");
		C();
	}

	private void D() {
		mTasker4 = new Tasker();
		mTasker4.execute("");
		E();
	}

	@Override
	protected void onDestroy() {
		if (mTasker1 != null) {
			mTasker1.cancel(true);
		}
		if (mTasker2 != null) {
			mTasker2.cancel(true);
		}
		if (mTasker3 != null) {
			mTasker3.cancel(true);
		}
		if (mTasker4 != null) {
			mTasker4.cancel(true);
		}
		if (mTasker5 != null) {
			mTasker5.cancel(true);
		}
		super.onDestroy();
	}

}
