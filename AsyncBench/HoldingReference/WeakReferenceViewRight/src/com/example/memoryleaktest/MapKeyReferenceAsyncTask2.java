package com.example.memoryleaktest;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.os.AsyncTask;
import android.view.View;

public class MapKeyReferenceAsyncTask2 extends AsyncTask<String, String, String> {

	private Map<WeakReference<View>, String> mMap = null;

	public MapKeyReferenceAsyncTask2(Map<View, String> map) {
		mMap = new HashMap<WeakReference<View>, String>();
		
		for (Entry<View, String> entry:map.entrySet()) {
			mMap.put(new WeakReference<View>(entry.getKey()),entry.getValue());
		}
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mMap);

		return stringBuffer.toString();
	}
}
