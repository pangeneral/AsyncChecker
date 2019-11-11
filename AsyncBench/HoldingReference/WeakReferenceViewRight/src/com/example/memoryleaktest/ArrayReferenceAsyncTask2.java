package com.example.memoryleaktest;

import java.lang.ref.WeakReference;
import android.os.AsyncTask;
import android.view.View;

public class ArrayReferenceAsyncTask2 extends AsyncTask<String, String, String> {

	private Object[] mList = new Object[1];

	public ArrayReferenceAsyncTask2(View[] list) {
		WeakReference<View> viewReference = new WeakReference<View>(list[0]);	
		mList[0] = viewReference;
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mList);

		return stringBuffer.toString();
	}
}
