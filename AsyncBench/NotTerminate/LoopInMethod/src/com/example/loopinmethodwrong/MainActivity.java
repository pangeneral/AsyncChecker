package com.example.loopinmethodwrong;

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
    }
    
    static class Tasker extends AsyncTask<String,String,String>{
    	
    	public void theMethod(){
    		int i=0;
    		while( i < 100 ){
    			i++;
    		}
    	}
    	
    	@Override
    	protected String doInBackground(String... params) {
    		// TODO Auto-generated method stub
    		theMethod();
    		return null;
    	}
    }
}
