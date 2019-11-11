package com.example.memoryleaktest;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;
import android.view.View;

public class ViewReferenceAsyncTask extends AsyncTask<String, String, String> {

	private WeakReference<View> mView = null;

	public ViewReferenceAsyncTask(View view) {
		
		mView = new WeakReference<View>(view);
		
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mView);

		return stringBuffer.toString();
	}
}
