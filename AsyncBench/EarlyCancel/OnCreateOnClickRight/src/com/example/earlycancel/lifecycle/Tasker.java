package com.example.earlycancel.lifecycle;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;
import android.widget.TextView;

public class Tasker extends AsyncTask<String,String,String>{
	private WeakReference<TextView> mReferenceView;
	
	public WeakReference<TextView> getReferenceView() {
		return mReferenceView;
	}

	public void setReferenceView(WeakReference<TextView> referenceView) {
		this.mReferenceView = referenceView;
	}

	public Tasker(TextView view1){
		mReferenceView = new WeakReference<TextView>(view1);
	}
	
	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		return null;
	}

}
