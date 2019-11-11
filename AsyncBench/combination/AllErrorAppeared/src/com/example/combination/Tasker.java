package com.example.combination;

import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

public class Tasker extends AsyncTask<String, String, String> {

	private TextView mReferenceView;

	public Tasker(TextView view) {
		mReferenceView = view;
		mReferenceView.setVisibility(View.VISIBLE);
	}

	@Override
	protected String doInBackground(String... params) {
		StringBuffer sb = new StringBuffer();
		for (String param : params) {
			sb.append(param);
		}
		return sb.toString();
	}
}
