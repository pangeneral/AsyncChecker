package com.example.forloopright;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {

private AsyncTask at;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.at = new Tasker();
        at.execute("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if( at != null )
    		at.cancel(true);
    	else
    		System.out.println("");
    }
    
    static class Tasker extends AsyncTask<String,String,String>{
    	List<String> theList = new ArrayList<String>();
    	
    	@Override
    	protected String doInBackground(String... params) {
    		// TODO Auto-generated method stub
    		for(String s: theList){
    			if( this.isCancelled() )
    				break;
    			System.out.println(s);
    		}
    		return null;
    	}
    }
}
