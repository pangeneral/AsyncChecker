package com.example.memoryleaktest;

import java.util.Set;

import android.os.AsyncTask;

public class SetReferenceAsyncTask extends
		AsyncTask<String, String, String> {

	private Set<String> mSet = null;

	public SetReferenceAsyncTask(Set<String> set) {
		mSet = set;
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mSet);

		return stringBuffer.toString();
	}
}
