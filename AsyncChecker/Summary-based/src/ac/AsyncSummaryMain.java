package ac;

import java.util.HashMap;
import java.util.Map;

import soot.SootMethod;
import ac.checker.SummaryBasedChecker;
import ac.config.AndroidConfigAdapter;
import ac.config.AsyncCheckerParamProcess;
import ac.config.Configuration;
import ac.record.AsyncClassCountRecorder;
import ac.record.ExceptionRecorder;
import ac.record.TimeRecorder;

import com.android.Main;

public class AsyncSummaryMain {
	
	public static Map<SootMethod, Boolean> methodToIsContainAsyncOperation = new HashMap<SootMethod, Boolean>();
	
	public static void main(String[] args) throws Exception {
		final Object lock = new Object();
		
		Main main = new Main();
		main.getProcessManager().setParamProcess(new AsyncCheckerParamProcess());
		main.sootInit(args);
		AndroidConfigAdapter.setJymbolicAndroidConfigItem();
		
		AsyncClassCountRecorder.recordInstantiatedAsyncClass();
		
		final TimeOutDetectionThread thread = new TimeOutDetectionThread(lock);
		thread.setSleepTime(Configuration.MAX_RUNNING_TIME);
		thread.start();
		
//		long preprocessStartTime = System.currentTimeMillis();
		
		try {
			System.out.println("============================================");
			System.out.println("AsyncChecker analysis begins");
			long analysisStartTime = System.currentTimeMillis();
			SummaryBasedChecker mDetector = new SummaryBasedChecker();
			mDetector.detect();
//			Log.i("----------detector.detectAsyncTask() starts ");
//			Log.i(" --- detector.detectAsyncTask has been finished");
			synchronized (lock) {
//				long preprocessTime = analysisStartTime - preprocessStartTime;
				long analysisTime = System.currentTimeMillis() - analysisStartTime;
				TimeRecorder.record(10000, analysisTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionRecorder.saveException(e);
		} finally {
			thread.killSelf();
		}
	}
	
}
