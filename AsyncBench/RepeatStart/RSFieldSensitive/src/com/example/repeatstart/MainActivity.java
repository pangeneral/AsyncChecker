package com.example.repeatstart;

import android.app.Activity;
import android.os.Bundle;


public class MainActivity extends Activity {
	
	Test test = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test = new Test(findViewById(R.id.view1));
        test.newTasker();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	test.startTask();
    }
   
}
