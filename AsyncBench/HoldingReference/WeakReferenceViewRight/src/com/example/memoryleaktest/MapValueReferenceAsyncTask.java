package com.example.memoryleaktest;

import java.lang.ref.WeakReference;
import java.util.Map;

import android.os.AsyncTask;
import android.view.View;

public class MapValueReferenceAsyncTask extends AsyncTask<String, String, String> {

	private WeakReference<Map<String, View>> mMap = null;

	public MapValueReferenceAsyncTask(Map<String, View> map) {
		mMap = new WeakReference<Map<String, View>>(map);
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mMap);

		return stringBuffer.toString();
	}
}
