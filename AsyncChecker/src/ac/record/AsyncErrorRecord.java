package ac.record;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import soot.RefType;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import ac.AsyncMain;
import ac.constant.AsyncMethodSignature;
import ac.entity.AsyncTaskRefObject;
import ac.entity.AsyncTypeState;
import androlic.config.BaseConfiguration;
import androlic.entity.GlobalMessage;
import androlic.entity.value.IBasicValue;
import androlic.record.ExceptionRecord;
import androlic.util.MethodUtil;

public class AsyncErrorRecord {

	private static final String ERROR_FOLDER = BaseConfiguration.getErrorFolder() + File.separator;

	public synchronized static void recordStrongReference(AsyncTaskRefObject asyncObject,
			Map<SootField, IBasicValue> taintedField, Unit unit, GlobalMessage globalMessage) {
		if (!isAsyncObjectNeedRecord(AsyncMain.strongReferenceMap, asyncObject)) {
			return;
		}
		
		String filePath = ERROR_FOLDER + "strong-reference-sum.txt";
		recordSum(asyncObject, globalMessage, filePath);

		filePath = ERROR_FOLDER + "strong-reference.txt";
		String content = "Tainted field: ";
		int index = 0;
		for (Map.Entry<SootField, IBasicValue> entry : taintedField.entrySet()) {
			content += entry.getKey().getSignature() + "";
			index++;
			if (index != taintedField.size() - 1)
				content += ", ";
		}
		content += "\n";
		content += AsyncErrorRecord.recordPathInfo(asyncObject, globalMessage);

		record(content, filePath);
	}

	public synchronized static void recordRepeatStart(AsyncTaskRefObject asyncObject, Unit unit,
			GlobalMessage globalMessage) {
		if (!isAsyncObjectNeedRecord(AsyncMain.repeatStartMap, asyncObject))
			return;
		
		String filePath = ERROR_FOLDER + "repeat-start-sum.txt";
		recordSum(asyncObject, globalMessage, filePath);

		filePath = ERROR_FOLDER + "repeat-start.txt";
		String content = AsyncErrorRecord.recordPathInfo(asyncObject, globalMessage);
		record(content, filePath);
	}

	public synchronized static void recordEarlyCancel(AsyncTaskRefObject asyncObject, Unit unit,
			GlobalMessage globalMessage) {
		if (!isAsyncObjectNeedRecord(AsyncMain.earlyCancelMap, asyncObject))
			return;
		
		String filePath = ERROR_FOLDER + "early-cancel-sum.txt";
		recordSum(asyncObject, globalMessage, filePath);

		filePath = ERROR_FOLDER + "early-cancel.txt";
		String content = AsyncErrorRecord.recordPathInfo(asyncObject, globalMessage);
		record(content, filePath);
	}

	public synchronized static void recordNotCancel(AsyncTaskRefObject asyncObject, GlobalMessage globalMessage) {
		if (!isAsyncObjectNeedRecord(AsyncMain.notCancelMap, asyncObject))
			return;
		
		String filePath = ERROR_FOLDER + "not-cancel-sum.txt";
		recordSum(asyncObject, globalMessage, filePath);

		filePath = ERROR_FOLDER + "not-cancel.txt";
		String content = asyncObject.getInitStatement() + " is not cancelled after execute\n";
		content += AsyncErrorRecord.recordPathInfo(asyncObject, globalMessage);
		record(content, filePath);
	}

	public synchronized static void recordNotTerminate(AsyncTaskRefObject asyncObject, GlobalMessage globalMessage) {
		if (!isAsyncObjectNeedRecord(AsyncMain.notTerminateMap, asyncObject))
			return;
		
		String filePath = ERROR_FOLDER + "not-terminate-sum.txt";
		recordSum(asyncObject, globalMessage, filePath);

		filePath = ERROR_FOLDER + "not-terminate.txt";
		String content = asyncObject.getInitStatement() + " did not terminate\n";
		content += AsyncErrorRecord.recordPathInfo(asyncObject, globalMessage);
		SootMethod doInBackgroundMethod = MethodUtil.getMethod(((RefType) asyncObject.getType()).getSootClass(),
				AsyncMethodSignature.DO_IN_BACKGROUND_NAME);
		content += doInBackgroundMethod.getActiveBody().toString();
		record(content, filePath);
	}

	/**
	 * If processedMap doesn't contain asyncObject, return true. Otherwise, return false
	 * @param processedMap
	 * @param asyncObject
	 * @return
	 */
	private synchronized static boolean isAsyncObjectNeedRecord(Map<String, AsyncTaskRefObject> processedMap, AsyncTaskRefObject asyncObject) {
		AsyncMain.rightInstanceMap.remove(asyncObject.getObjectKey());
		if (!AsyncMain.errorInstanceMap.containsKey(asyncObject.getObjectKey())) {
			AsyncMain.errorInstanceMap.put(asyncObject.getObjectKey(), asyncObject);
			String errorInstanceFilePath = BaseConfiguration.getSymbolicPathFolder() + File.separator + "error-async.txt";
			record(asyncObject.getObjectKey()+"\n", errorInstanceFilePath);
		}
		
		if (!processedMap.containsKey(asyncObject.getObjectKey())) {
			processedMap.put(asyncObject.getObjectKey(), asyncObject);
			return true;
		}
		else {
			return false;
		}
	}
	
	private synchronized static String recordPathInfo(AsyncTaskRefObject asyncObject, GlobalMessage globalMessage) {
		String content = "";
		AsyncTypeState state = asyncObject.getTypeState(globalMessage);
		content += "AsyncTask path: \n";
		for (int i = 0; i < state.getExecutionUnitList().size(); i++) {
			content += state.getExecutionUnitList().get(i) + "\r\n";
		}
		content += "\n";

		content += "execution path: \n";
		for (int i = 0; i < globalMessage.getStmtList().size(); i++) {
			content += globalMessage.getStmtList().get(i) + "\r\n";
		}
		content += "\n";
		return content;
	}

	public synchronized static void recordTime(String source, long androlicStartTime, long sootStartTime) {
		String logFilePath = BaseConfiguration.getSymbolicPathFolder() + File.separator + "log.txt";
		String content = source + "sootTime: " + (androlicStartTime - sootStartTime) + "\n" + "androlic time: "
				+ (System.currentTimeMillis() - androlicStartTime) + "\n";
//		String content = source + Thread.currentThread().getName() + " sootTime: " + (androlicStartTime - sootStartTime) + "\n" + "androlic time: "
//				+ (System.currentTimeMillis() - androlicStartTime) + "\n";
		record(content, logFilePath);
	}

	public synchronized static void recordRightInstance() {
		String rightInstanceFilePath = BaseConfiguration.getSymbolicPathFolder() + File.separator + "right-async.txt";
		FileWriter errorWriter = null;
		try {
			errorWriter = new FileWriter(rightInstanceFilePath, true);
			for (String key: AsyncMain.rightInstanceMap.keySet()) {
				errorWriter.write(key + "\n");
			}
		} catch (Exception e) {
			ExceptionRecord.saveException(e);
		} finally {
			try {
				errorWriter.close();
			} catch (IOException e) {
				ExceptionRecord.saveException(e);
			}
		}
	}
	
	private static void recordSum(AsyncTaskRefObject asyncObject, GlobalMessage globalMessage, String filePath) {
		String content = asyncObject.getType().toString() + ";" + globalMessage.getCurrentPathInfo().getLength() + ";" + asyncObject.getInitStatement().toString() + ";"
				+ asyncObject.getObjectKey() + "\n";
		record(content, filePath);
	}
	
//	public synchronized static void recordInstance(AsyncTaskRefObject asyncObject) {
//		if(AsyncMain.rightInstanceMap.containsKey(asyncObject.getObjectKey())) {
//			return;
//		}
//		AsyncMain.rightInstanceMap.put(asyncObject.getObjectKey(), asyncObject);
//		
//		String rightInstanceFilePath = BaseConfiguration.getSymbolicPathFolder() + File.separator + "right-async.txt";
//		recordAsyncTaskRefObject(asyncObject, rightInstanceFilePath);
//	}


	public static void record(String content, String filePath) {
		FileWriter errorWriter = null;
		try {
			errorWriter = new FileWriter(filePath, true);
			errorWriter.write(content);
		} catch (Exception e) {
			ExceptionRecord.saveException(e);
		} finally {
			try {
				errorWriter.close();
			} catch (IOException e) {
				ExceptionRecord.saveException(e);
			}
		}
	}

}
