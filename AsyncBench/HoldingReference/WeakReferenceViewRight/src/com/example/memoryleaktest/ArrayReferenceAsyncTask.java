package com.example.memoryleaktest;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;
import android.view.View;

public class ArrayReferenceAsyncTask extends AsyncTask<String, String, String> {

	private WeakReference<View[]> mList = null;

	public ArrayReferenceAsyncTask(View[] list) {
		mList = new WeakReference<View[]>(list);
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mList);

		return stringBuffer.toString();
	}
}
