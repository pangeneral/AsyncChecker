package com.example.memoryleaktest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
	
	private ViewReferenceAsyncTask mViewAsyncTask = null;

	private ArrayReferenceAsyncTask mArrayAsyncTask = null;
	
	private ListReferenceAsyncTask mListAsyncTask = null;

	private MapKeyReferenceAsyncTask mMapAsyncTask = null;
	
	private MapValueReferenceAsyncTask mMapValueAsyncTask = null;
	
	private SetReferenceAsyncTask mSetAsyncTask = null;
	
	private ArrayReferenceAsyncTask2 mArrayReferenceAsyncTask2 = null;
	
	private MapKeyReferenceAsyncTask2 mMapKeyReferenceAsyncTask2 = null;
	
	private MapValueReferenceAsyncTask2 mMapValueReferenceAsyncTask2 = null;
	
	private SetReferenceAsyncTask2 mSetReferenceAsyncTask2 = null;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		View[] views = new View[]{};
		views[0] = findViewById(R.id.view1);
		mArrayAsyncTask = new ArrayReferenceAsyncTask(views);
		mArrayAsyncTask.execute("");
		mArrayAsyncTask.cancel(true);
		
		mViewAsyncTask = new ViewReferenceAsyncTask(findViewById(R.id.view1));
		mViewAsyncTask.execute("");
		mViewAsyncTask.cancel(true);
		
		List<View> list = new ArrayList<View>();
		list.add(findViewById(R.id.view1));
		mListAsyncTask = new ListReferenceAsyncTask(list);
		mListAsyncTask.execute("");
		mListAsyncTask.cancel(true);
		
		Map<View,String> map = new HashMap<View,String>();
		map.put(findViewById(R.id.view1), "value");
		mMapAsyncTask = new MapKeyReferenceAsyncTask(map);
		mMapAsyncTask.execute("");
		mMapAsyncTask.cancel(true);
		
		Map<String, View> map2 = new HashMap<String, View>();
		map2.put("value", findViewById(R.id.view1));
		mMapValueAsyncTask = new MapValueReferenceAsyncTask(map2);
		mMapValueAsyncTask.execute("");
		mMapValueAsyncTask.cancel(true);
		
		Set<View> set = new HashSet<View>();
		set.add(findViewById(R.id.view1));
		mSetAsyncTask = new SetReferenceAsyncTask(set);
		mSetAsyncTask.execute("");
		mSetAsyncTask.cancel(true);
		
		mArrayReferenceAsyncTask2 = new ArrayReferenceAsyncTask2(views);
		mArrayReferenceAsyncTask2.execute("");
		mArrayReferenceAsyncTask2.cancel(true);
		
		mMapKeyReferenceAsyncTask2 = new MapKeyReferenceAsyncTask2(map);
		mMapKeyReferenceAsyncTask2.execute("");
		mMapKeyReferenceAsyncTask2.cancel(true);
		
		mMapValueReferenceAsyncTask2 = new MapValueReferenceAsyncTask2(map2);
		mMapValueReferenceAsyncTask2.execute("");
		mMapValueReferenceAsyncTask2.cancel(true);
		
		mSetReferenceAsyncTask2 = new SetReferenceAsyncTask2(set);
		mSetReferenceAsyncTask2.execute("");
		mSetReferenceAsyncTask2.cancel(true);
		
	}

}
