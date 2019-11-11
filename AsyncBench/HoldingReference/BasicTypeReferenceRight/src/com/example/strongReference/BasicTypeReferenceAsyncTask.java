package com.example.strongReference;

import android.os.AsyncTask;

public class BasicTypeReferenceAsyncTask extends
		AsyncTask<String, String, String> {

	private byte mByte = '1';

	private short mShort = 0;

	private int mInt = 0;

	private long mLong = 0;

	private float mFloat = 0;

	private double mDouble = 0;

	private boolean mBoolean = false;

	private char mChar = 'A';

	public BasicTypeReferenceAsyncTask(byte bByte, short bShort, int bInt,
			long bLong, float bFloat, double bDouble, boolean bBoolean,
			char bChar) {
		mBoolean = bBoolean;
		mByte = bByte;
		mChar = bChar;
		mDouble = bDouble;
		mFloat = bFloat;
		mInt = bInt;
		mLong = bLong;
		mShort = bShort;
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mByte);
		stringBuffer.append(mShort);
		stringBuffer.append(mInt);
		stringBuffer.append(mLong);
		stringBuffer.append(mFloat);
		stringBuffer.append(mChar);
		stringBuffer.append(mDouble);
		stringBuffer.append(mBoolean);

		return stringBuffer.toString();
	}
}
