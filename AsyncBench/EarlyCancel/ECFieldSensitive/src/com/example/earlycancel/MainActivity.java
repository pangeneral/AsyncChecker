package com.example.earlycancel;

import android.app.Activity;
import android.os.Bundle;


public class MainActivity extends Activity {
	
	Test test = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test = new Test(findViewById(R.id.view1));
        test.cancel();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	test.newTasker();
    	test.startTask();
    }
   
}
