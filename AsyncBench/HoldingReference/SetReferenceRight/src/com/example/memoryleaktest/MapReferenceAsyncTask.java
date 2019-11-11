package com.example.memoryleaktest;

import java.util.Map;

import android.os.AsyncTask;

public class MapReferenceAsyncTask extends AsyncTask<String, String, String> {

	private Map<String, String> mMap = null;

	public MapReferenceAsyncTask(Map<String, String> map) {
		mMap = map;
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mMap);

		return stringBuffer.toString();
	}
}
