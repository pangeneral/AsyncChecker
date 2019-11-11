package com.example.combination;

import android.view.View;

public class ChildTasker extends Tasker {

	public ChildTasker(View view) {
		super(view);
	}

	public void start() {
		execute("");
	}
}
