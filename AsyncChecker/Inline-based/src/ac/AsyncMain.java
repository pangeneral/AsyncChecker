package ac;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;
import ac.entity.AsyncTaskRefObject;
import ac.processor.AsyncLibraryInvocationProcessor;
import ac.processor.AsyncNewArrayExprProcessor;
import ac.processor.AsyncNewExprProcessor;
import ac.processor.AsyncSymbolicInstrumenter;
import ac.processor.AsyncTaskInterProceduralJudge;
import ac.record.AsyncErrorRecord;
import androlic.TimeOutDetectionThread;
import androlic.config.AndrolicConfigurationManager;
import androlic.config.DefaultSootConfig;
import androlic.config.ISootConfig;
import androlic.config.SymbolicConfiguration;
import androlic.entrypoint.activity.ActivityEntryPointWithoutFragment;
import androlic.entrypoint.activity.AndroidCallBacks;
import androlic.entrypoint.fragment.FragmentEntryPoint;
import androlic.execution.SymbolicEngine;
import androlic.record.ExceptionRecord;
import androlic.util.ClassInheritanceProcess;
import androlic.util.Log;

public class AsyncMain {
	
	private static ExecutorService sEXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()+1);
	
	private static TimeOutDetectionThread timeThread;
	
	private static volatile long SOOT_START_TIME = 0;
	
	private static volatile long ANDROLIC_START_TIME = 0;
	
	private static volatile boolean EXIT = false;
	
	private static volatile boolean isRecord = false;
	
	public static Map<String, AsyncTaskRefObject> strongReferenceMap = new HashMap<String, AsyncTaskRefObject>();
	public static Map<String, AsyncTaskRefObject> repeatStartMap = new HashMap<String, AsyncTaskRefObject>();
	public static Map<String, AsyncTaskRefObject> earlyCancelMap = new HashMap<String, AsyncTaskRefObject>();
	public static Map<String, AsyncTaskRefObject> notCancelMap = new HashMap<String, AsyncTaskRefObject>();
	public static Map<String, AsyncTaskRefObject> notTerminateMap = new HashMap<String, AsyncTaskRefObject>();
	
	public static Map<String, AsyncTaskRefObject> errorInstanceMap = new HashMap<String, AsyncTaskRefObject>();
	public static Map<String, AsyncTaskRefObject> rightInstanceMap = new HashMap<String, AsyncTaskRefObject>();
	
	public static void main(String[] args) {
		AndrolicConfigurationManager.init(args);
		
		final Object lock = new Object();
		timeThread = new TimeOutDetectionThread(lock);
		timeThread.setOnKillSelfListener(new TimeOutDetectionThread.OnKillSelfListener() {
			
			@Override
			public void onPreKillSelf() {
				EXIT = true;
				sEXECUTOR.shutdownNow();
				AsyncErrorRecord.recordTime("", ANDROLIC_START_TIME, SOOT_START_TIME);
				if (!isRecord) {
					AsyncErrorRecord.recordRightInstance();
					if (Options.v().output_format() != Options.output_format_none)
						PackManager.v().writeOutput();
					isRecord = true;
				}
			}
		});
		
		timeThread.setPriority(10);
		timeThread.start();
		
		ISootConfig mSootConfig = new DefaultSootConfig();
		SymbolicConfiguration.setInterProceduralJudge(AsyncTaskInterProceduralJudge.v());
		SymbolicConfiguration.setNewExprProcessor(AsyncNewExprProcessor.v());
		SymbolicConfiguration.setNewArrayExprProcessor(AsyncNewArrayExprProcessor.v());
		SymbolicConfiguration.setLibraryInvocationProcessor(AsyncLibraryInvocationProcessor.v());
		SymbolicConfiguration.setSymbolicInstrumenter(AsyncSymbolicInstrumenter.v());
		
		SOOT_START_TIME = System.currentTimeMillis();
		mSootConfig.sootInitialization();
		ANDROLIC_START_TIME = System.currentTimeMillis();
		
		try {
			Log.i("----------analysis start------------");
//			test("de.tap.easy_xkcd.fragments.NestedPreferenceFragment");
			analyzeActivity(timeThread);
			
			Log.i("----------analysis finish------------");
		} catch (Exception e) {
			ExceptionRecord.saveException(e);
		} catch (Error e) {
			ExceptionRecord.saveException(e);
		}finally {
			Log.i("----------analysis finish------------");
			if (!isRecord) {
				AsyncErrorRecord.recordRightInstance();
				if (Options.v().output_format() != Options.output_format_none)
					PackManager.v().writeOutput();
				isRecord = true;
			}
			timeThread.killSelf();
		}
	}
	
	public static void test(String className) {
		SootClass targetClass = Scene.v().getSootClassUnsafe(className);
		SootMethod dummyMethod = new FragmentEntryPoint(targetClass).getMainMethod();
		SymbolicEngine cs = new SymbolicEngine(targetClass);
		System.out.println(cs.solve(dummyMethod));
	}
	
	public static void analyzeActivity(TimeOutDetectionThread timeThread) throws IOException {
		System.out.println("analysis begins");
		Iterator<SootClass> it = Scene.v().getApplicationClasses().iterator();
		while (it.hasNext() && !EXIT) {
			SootMethod dummyMethod = null;
			SootClass sc = it.next();
			if (!sc.isAbstract() && ClassInheritanceProcess.isInheritedFromFragment(sc)) {
				dummyMethod = new FragmentEntryPoint(sc).getMainMethod();
			} else if (!sc.isAbstract() && ClassInheritanceProcess.isInheritedFromActivity(sc)) {
				dummyMethod = new ActivityEntryPointWithoutFragment(sc).getMainMethod();
			} else {
				continue;
			}
			
			if (!AsyncTaskInterProceduralJudge.v().isContainAsyncTaskOperation(dummyMethod)) {
				continue;
			}
			if (EXIT) {
				break;
			}
			try {
				start(sc, dummyMethod);
			} catch (Exception e) {
				ExceptionRecord.saveException(e);
			} catch (Error e) {
				ExceptionRecord.saveException(e);
			}
//			SootClass sc = it.next();
//			if (!sc.isAbstract() && ClassInheritanceProcess.isInheritedFromActivity(sc)) {
//				List<AsyncEntryPoint> entryMethodList = new ArrayList<AsyncEntryPoint>();
//				
//				List<SootMethod> lifeCycleMethodList = EntryPointCreation.generateLifeCycleMethodList(sc);
//				for (int i = 0; i < lifeCycleMethodList.size(); i++) {
//					EntryPointCreation.generateEntryMethodList(lifeCycleMethodList.get(i), entryMethodList, 
//							new HashSet<SootMethod>(), new ArrayList<SootMethod>());
//				}
//				List<SootMethod> callbackMethodList = ActivityCallBackCollector.getCallBackSootMethods(sc);
//				for (int i = 0; i < callbackMethodList.size(); i++) {
//					EntryPointCreation.generateEntryMethodList(callbackMethodList.get(i), entryMethodList, 
//							new HashSet<SootMethod>(), new ArrayList<SootMethod>());
//				}
//				
//				List<SootMethod> dummyMethodList = new ActivityEntryPoints(entryMethodList).getSootMethods();
//				for (int i = 0; i < dummyMethodList.size(); i++) {
//					try {
//						start(sc, dummyMethodList.get(i));
//					} catch (Exception e) {
//						ExceptionRecord.saveException(e);
//					} catch (Error e) {
//						ExceptionRecord.saveException(e);
//					}
//				} 
//			}
//			if (EXIT) {
//				break;
//			}
		}
		sEXECUTOR.shutdown();
		while (true) {
			if (sEXECUTOR.isTerminated()) {
				break;
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void start(final SootClass sc, final SootMethod sootMethod) { 
		AndroidCallBacks.getCallBackSootClasses(); 
		Thread thread = new Thread() {
			@Override
			public void run() {
				int time = -1;
				try {
					SymbolicEngine cs = new SymbolicEngine(sc);
//					Log.e("before analysis");
					time = cs.solve(sootMethod);
				}
				catch (Exception e) {
					ExceptionRecord.saveException(e);
				} catch (Error e) {
					ExceptionRecord.saveException(e);
				}finally {
//					Log.e("after analysis");
					record(sc, time);
				}
			}
		};
		sEXECUTOR.execute(thread);
	}
	
	private synchronized static void record(SootClass sc, int time) {
		String content = sc.getName() + " " + time + "\n";
		AsyncErrorRecord.recordTime(content, ANDROLIC_START_TIME, SOOT_START_TIME);
	}

}