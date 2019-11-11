package com.example.strongReference;

import android.view.View;

public class ChildTasker extends Tasker{
	
	public ChildTasker(View view){
		super(view);
	}
	
	public void start(){
		execute("");
	}
	
	@Override
	protected String doInBackground(String... params) {
		return super.doInBackground(params);
	}
}
