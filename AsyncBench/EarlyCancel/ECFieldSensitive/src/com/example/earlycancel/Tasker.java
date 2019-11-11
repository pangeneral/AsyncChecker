package com.example.earlycancel;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;
import android.widget.TextView;

public class Tasker extends AsyncTask<String,String,String>{

	private WeakReference<TextView> referenceView;
	
	public WeakReference<TextView> getReferenceView() {
		return referenceView;
	}

	public void setReferenceView(WeakReference<TextView> referenceView) {
		this.referenceView = referenceView;
	}

	public Tasker(TextView view1){
		referenceView = new WeakReference<TextView>(view1);
	}
	
	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		return null;
	}
}
