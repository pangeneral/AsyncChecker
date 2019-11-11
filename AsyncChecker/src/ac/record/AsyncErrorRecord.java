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
		if(AsyncMain.errorInstanceMap.containsKey(asyncObject.getObjectKey())) {
			return;
		}
		AsyncMain.errorInstanceMap.put(asyncObject.getObjectKey(), asyncObject);
		recordError(asyncObject);
		
		String filePath = ERROR_FOLDER + "strong-reference-sum.txt";
		String content = asyncObject.getType().toString() + ";" + unit + ";" + asyncObject.getObjectKey() + "\n";
		record(content, filePath);

		filePath = ERROR_FOLDER + "strong-reference.txt";
		content = "Tainted field: ";
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
		if(AsyncMain.errorInstanceMap.containsKey(asyncObject.getObjectKey())) {
			return;
		}
		AsyncMain.errorInstanceMap.put(asyncObject.getObjectKey(), asyncObject);
		recordError(asyncObject);
		
		String filePath = ERROR_FOLDER + "repeat-start-sum.txt";
		String content = asyncObject.getType().toString() + ";" + unit + ";" + asyncObject.getObjectKey() + "\n";
		record(content, filePath);

		filePath = ERROR_FOLDER + "repeat-start.txt";
		content = AsyncErrorRecord.recordPathInfo(asyncObject, globalMessage);
		record(content, filePath);
	}

	public synchronized static void recordEarlyCancel(AsyncTaskRefObject asyncObject, Unit unit,
			GlobalMessage globalMessage) {
		if(AsyncMain.errorInstanceMap.containsKey(asyncObject.getObjectKey())) {
			return;
		}
		AsyncMain.errorInstanceMap.put(asyncObject.getObjectKey(), asyncObject);
		recordError(asyncObject);
		
		String filePath = ERROR_FOLDER + "early-cancel-sum.txt";
		String content = asyncObject.getType().toString() + ";" + unit + ";" + asyncObject.getObjectKey() + "\n";
		record(content, filePath);

		filePath = ERROR_FOLDER + "early-cancel.txt";
		content = AsyncErrorRecord.recordPathInfo(asyncObject, globalMessage);
		record(content, filePath);
	}

	public synchronized static void recordNotCancel(AsyncTaskRefObject asyncObject, GlobalMessage globalMessage) {
		if(AsyncMain.errorInstanceMap.containsKey(asyncObject.getObjectKey())) {
			return;
		}
		AsyncMain.errorInstanceMap.put(asyncObject.getObjectKey(), asyncObject);
		recordError(asyncObject);
		
		String filePath = ERROR_FOLDER + "not-cancel-sum.txt";
		String content = asyncObject.getType().toString() + ";" + asyncObject.getInitStatement().toString() + ";"
				+ asyncObject.getObjectKey() + "\n";
		record(content, filePath);

		filePath = ERROR_FOLDER + "not-cancel.txt";
		content = asyncObject.getInitStatement() + " is not cancelled after execute\n";
		content += AsyncErrorRecord.recordPathInfo(asyncObject, globalMessage);
		record(content, filePath);
	}

	public synchronized static void recordNotTerminate(AsyncTaskRefObject asyncObject, GlobalMessage globalMessage) {
		if(AsyncMain.errorInstanceMap.containsKey(asyncObject.getObjectKey())) {
			return;
		}
		AsyncMain.errorInstanceMap.put(asyncObject.getObjectKey(), asyncObject);
		recordError(asyncObject);
		
		String filePath = ERROR_FOLDER + "not-terminate-sum.txt";
		String content = asyncObject.getType().toString() + ";" + asyncObject.getInitStatement().toString() + ";"
				+ asyncObject.getObjectKey() + "\n";
		record(content, filePath);

		filePath = ERROR_FOLDER + "not-terminate.txt";
		content = asyncObject.getInitStatement() + " did not terminate\n";
		content += AsyncErrorRecord.recordPathInfo(asyncObject, globalMessage);
		SootMethod doInBackgroundMethod = MethodUtil.getMethod(((RefType) asyncObject.getType()).getSootClass(),
				AsyncMethodSignature.DO_IN_BACKGROUND_NAME);
		content += doInBackgroundMethod.getActiveBody().toString();
		record(content, filePath);
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
		record(content, logFilePath);
	}

	public synchronized static void recordInstance(AsyncTaskRefObject asyncObject) {
		if(AsyncMain.rightInstanceMap.containsKey(asyncObject.getObjectKey())) {
			return;
		}
		AsyncMain.rightInstanceMap.put(asyncObject.getObjectKey(), asyncObject);
		
		String rightInstanceFilePath = BaseConfiguration.getSymbolicPathFolder() + File.separator + "right-async.txt";
		recordAsyncTaskRefObject(asyncObject, rightInstanceFilePath);
	}

	public synchronized static void recordError(AsyncTaskRefObject asyncObject) {
		String errorInstanceFilePath = BaseConfiguration.getSymbolicPathFolder() + File.separator + "error-async.txt";
		recordAsyncTaskRefObject(asyncObject, errorInstanceFilePath);
	}

	private static void recordAsyncTaskRefObject(AsyncTaskRefObject asyncObject, String filePath) {
		String content = asyncObject.getObjectKey() + "\n";
		record(content, filePath);
	}

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
