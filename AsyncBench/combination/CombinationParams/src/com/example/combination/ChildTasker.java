package com.example.combination;

import android.view.View;

public class ChildTasker extends Tasker{
	
	private View mView = null;
	
	public ChildTasker(View view){
		super();
		mView = view;
	}
	
	@Override
	protected String doInBackground(String... params) {
		StringBuffer sb = new StringBuffer();
		for(String string : params){
			sb.append(string);
		}
		sb.append(mView);
		return super.doInBackground(params);
	}
}
