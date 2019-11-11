package com.example.combination;

import android.os.AsyncTask;
import android.view.View;

public class Test {
	
	public void start( AsyncTask<String, ?, ?> asyncTask ){
		asyncTask.execute("");
	}
	
	public void cancel( AsyncTask<?, ?, ?> asyncTask ){
		asyncTask.cancel(true);
	}
	
	public Tasker newAsyncTask(){
		return new Tasker();
	}
	
	public Tasker newAsyncTaskChild(View view){
		return new ChildTasker(view);
	}
	
}
