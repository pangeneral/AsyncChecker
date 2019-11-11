package ac.util;

import soot.Local;
import soot.Unit;
import ac.entity.AsyncTaskOperation;
import ac.entity.AsyncTaskRefObject;
import ac.entity.AsyncTaskStatus;
import ac.entity.AsyncTypeState;
import ac.record.AsyncErrorRecord;
import androlic.entity.GlobalMessage;

public class AsyncTaskStatusChecker {
	
	public static void checkAsyncTaskOperation(Local variable, AsyncTaskRefObject asyncObject, AsyncTaskOperation operation, 
			Unit unit, GlobalMessage globalMessage) {
		AsyncTypeState state = (AsyncTypeState) globalMessage.getObjectToTypeState().get(asyncObject);
		if (state.isExecuted() && operation == AsyncTaskOperation.Execute) {
			AsyncErrorRecord.recordRepeatStart(asyncObject, unit, globalMessage);
//			AsyncMain.errorInstanceSet.add(asyncObject);
//			AsyncMain.rightInstanceSet.remove(asyncObject);
//			throw new AsyncTaskRepeatStartException(variable, asyncObject);
		}
		else if (state.getCurrentStatus() == AsyncTaskStatus.PENDING && operation == AsyncTaskOperation.Cancel) {
			AsyncErrorRecord.recordEarlyCancel(asyncObject, unit, globalMessage);
//			AsyncMain.errorInstanceSet.add(asyncObject);
//			AsyncMain.rightInstanceSet.remove(asyncObject);
//			throw new AsyncTaskEarlyCancelException(variable, asyncObject);
		}
	}
}