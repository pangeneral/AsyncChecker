package com.example.memoryleaktest;

import android.os.AsyncTask;
import android.view.View;

public class ViewReferenceAsyncTask extends AsyncTask<String, String, String> {

	private View mView = null;

	public ViewReferenceAsyncTask(View view) {
		mView = view;
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mView);

		return stringBuffer.toString();
	}
}
