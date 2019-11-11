package com.example.combination;

import android.os.AsyncTask;

public class Tasker extends AsyncTask<String,String,String>{
	
	public Tasker(){
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
