package com.example.strongReference;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	private Tasker child = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		child = new ChildTasker(findViewById(R.id.button1));
		child.execute("");
	}

	@Override
	protected void onDestroy() {
		if (child != null) {
			child.cancel(true);
		}
		super.onDestroy();
	}

}
