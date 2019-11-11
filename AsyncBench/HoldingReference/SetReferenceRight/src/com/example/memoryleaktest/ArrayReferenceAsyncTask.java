package com.example.memoryleaktest;

import android.os.AsyncTask;

public class ArrayReferenceAsyncTask extends AsyncTask<String, String, String> {

	private String[] mList = null;

	public ArrayReferenceAsyncTask(String[] list) {
		mList = list;
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mList);

		return stringBuffer.toString();
	}
}
