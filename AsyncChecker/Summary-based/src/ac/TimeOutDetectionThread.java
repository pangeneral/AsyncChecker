/* AsyncDetecotr - an Android async component misuse detection tool
 * Copyright (C) 2018 Baoquan Cui
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package ac;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import ac.util.Log;
import com.config.parameter.BaseConfiguration;

/**
 * A background thread which compulsively terminates AsyncDetector if the running time reaches the threshold 
 * @author Baoquan Cui
 * @version 1.0
 */
public class TimeOutDetectionThread extends Thread {

	private Object mLock;
	private String mDataSet;
	private String mFilePath;
	@SuppressWarnings("unused")
	private String mOutputFolder;
	
	/**
	 * The threshold of running time of AsyncDetector
	 */
	private long SLEEP_TIME = 300000;

	public TimeOutDetectionThread(Object theLock) {
		this.mLock = theLock;
		this.mFilePath = BaseConfiguration.APK_BASE_PATH;
		this.mOutputFolder = BaseConfiguration.OUTPUT_BASE_PATH;
	}
	
	public void setSleepTime(long sleepTime) {
		this.SLEEP_TIME = sleepTime;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(SLEEP_TIME);
			synchronized (mLock) {
				Log.e("Program is executing with too long time and will be killed ! ");
//				LongTimeException.saveException(mDataSet, mFilePath);
				killSelf();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			killSelf();
		}
	}

	public void killSelf() {		
		String name = ManagementFactory.getRuntimeMXBean().getName();
		String pid = name.split("@")[0];
		String os = System.getProperty("os.name");
		try {
			if (os != null && os.startsWith("Windows")) {
				Runtime.getRuntime().exec("Taskkill /f /IM " + pid);
			} else {
				String[] cmd = { "sh", "-c", "kill -9 " + pid };
				Runtime.getRuntime().exec(cmd);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//		Log.i("-----------program has been finished --------  ");
	}

}
