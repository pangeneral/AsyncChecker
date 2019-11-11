package com.example.memoryleaktest;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import android.os.AsyncTask;
import android.view.View;

public class SetReferenceAsyncTask2 extends AsyncTask<String, String, String> {

	private Set<WeakReference<View>> mSet = new HashSet<WeakReference<View>>();

	public SetReferenceAsyncTask2(Set<View> set) {
		for (View view : set) {
			mSet.add(new WeakReference<View>(view));
		}
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mSet);

		return stringBuffer.toString();
	}
}
