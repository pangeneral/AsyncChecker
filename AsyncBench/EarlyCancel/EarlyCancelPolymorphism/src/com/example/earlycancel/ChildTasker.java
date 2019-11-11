package com.example.earlycancel;

import android.view.View;

public class ChildTasker extends Tasker {

	public ChildTasker(View view) {
		super(view);
	}

	public void start() {
		execute("");
	}

	@Override
	protected String doInBackground(String... params) {
		StringBuffer sb = new StringBuffer();
		for (String param : params) {
			if (param.contains(".apk")) {
				continue;
			}
			sb.append(param);
		}
		return sb.toString();
	}
}
