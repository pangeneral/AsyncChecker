package com.example.memoryleaktest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
	
	private ArrayReferenceAsyncTask mArrayAsyncTask = null;

	private SetReferenceAsyncTask mSetAsyncTask = null;
	
	private MapReferenceAsyncTask mMapAsyncTask = null;
	
	private ListReferenceAsyncTask mListAsyncTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		String[] strings = new String[]{"A","Z"};
		mArrayAsyncTask = new ArrayReferenceAsyncTask(strings);
		mArrayAsyncTask.execute("");
		mArrayAsyncTask.cancel(true);
		
		Set<String> set = new HashSet<String>();
		set.add("set");
		mSetAsyncTask = new SetReferenceAsyncTask(set);
		mSetAsyncTask.execute("");
		mSetAsyncTask.cancel(true);
		
		Map<String,String> map = new HashMap<String,String>();
		map.put("Key", "map");
		mMapAsyncTask = new MapReferenceAsyncTask(map);
		mMapAsyncTask.execute("");
		mMapAsyncTask.cancel(true);
		
		List<String> list = new ArrayList<String>();
		list.add("list");
		mListAsyncTask = new ListReferenceAsyncTask(list);
		mListAsyncTask.execute("");
		mListAsyncTask.cancel(true);
		
	}

}
