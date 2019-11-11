package com.example.memoryleaktest;

import java.lang.ref.WeakReference;
import java.util.List;

import android.os.AsyncTask;
import android.view.View;

public class ListReferenceAsyncTask extends AsyncTask<String, String, String> {

	private WeakReference<List<View>> mList = null;

	public ListReferenceAsyncTask(List<View> list) {
		mList = new WeakReference<List<View>>(list);
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mList);

		return stringBuffer.toString();
	}
}
