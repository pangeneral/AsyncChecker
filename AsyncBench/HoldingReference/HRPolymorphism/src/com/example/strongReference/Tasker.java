package com.example.strongReference;

import android.os.AsyncTask;
import android.view.View;

public class Tasker extends AsyncTask<String,String,String>{
	
	private View mView = null;
	
	public Tasker(View view){
		mView = view;
		mView.setVisibility(View.INVISIBLE);
	}
	
	@Override
	protected String doInBackground(String... params) {
		StringBuffer sb = new StringBuffer();
		for(String param:params){
			sb.append(param);
		}
		return sb.toString();
	}
}
