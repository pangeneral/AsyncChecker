package com.example.memoryleaktest;

import java.util.List;

import android.os.AsyncTask;

public class ListReferenceAsyncTask extends AsyncTask<String, String, String> {

	private List<String> mList = null;

	public ListReferenceAsyncTask(List<String> list) {
		mList = list;
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mList);

		return stringBuffer.toString();
	}
}
