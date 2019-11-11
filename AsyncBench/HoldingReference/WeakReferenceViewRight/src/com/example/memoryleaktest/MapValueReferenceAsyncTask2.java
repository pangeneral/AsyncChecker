package com.example.memoryleaktest;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.os.AsyncTask;
import android.view.View;

public class MapValueReferenceAsyncTask2 extends
		AsyncTask<String, String, String> {

	private Map<String, WeakReference<View>> mMap = null;

	public MapValueReferenceAsyncTask2(Map<String, View> map) {
		mMap = new HashMap<String, WeakReference<View>>();

		for (Entry<String, View> entry : map.entrySet()) {
			mMap.put(entry.getKey(), new WeakReference<View>(entry.getValue()));
		}
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mMap);

		return stringBuffer.toString();
	}
}
