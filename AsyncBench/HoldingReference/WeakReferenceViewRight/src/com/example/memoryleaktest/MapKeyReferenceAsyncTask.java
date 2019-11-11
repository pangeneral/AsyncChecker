package com.example.memoryleaktest;

import java.lang.ref.WeakReference;
import java.util.Map;

import android.os.AsyncTask;
import android.view.View;

public class MapKeyReferenceAsyncTask extends AsyncTask<String, String, String> {

	private WeakReference<Map<View, String>> mMap = null;

	public MapKeyReferenceAsyncTask(Map<View, String> map) {
		mMap = new WeakReference<Map<View, String>>(map);
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mMap);

		return stringBuffer.toString();
	}
}
