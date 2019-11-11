package com.example.repeatstart;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity implements OnClickListener{
	
	private TextView view1;
	private AsyncTask task1;
	private int i1;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view1 = (TextView)findViewById(R.id.view1);
        view1.setOnClickListener(this);
        task1 = new Tasker(view1);
    }
    
    @Override
    protected void onDestroy() {
    	task1.cancel(true);
    	super.onDestroy();
    }

	@Override
	public void onClick(View v) {
    	task1.execute("");
	}
}
