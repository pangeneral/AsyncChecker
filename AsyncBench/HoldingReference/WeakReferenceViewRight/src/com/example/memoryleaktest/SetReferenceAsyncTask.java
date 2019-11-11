package com.example.memoryleaktest;

import java.lang.ref.WeakReference;
import java.util.Set;

import android.os.AsyncTask;
import android.view.View;

public class SetReferenceAsyncTask extends
		AsyncTask<String, String, String> {

	private WeakReference<Set<View>> mSet = null;

	public SetReferenceAsyncTask(Set<View> set) {
		mSet = new WeakReference<Set<View>>(set);
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mSet);

		return stringBuffer.toString();
	}
}
