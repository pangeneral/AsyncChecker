package ac.processor;

import java.util.Map;

import soot.Unit;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import ac.AsyncMain;
import ac.entity.AsyncTaskRefObject;
import ac.entity.AsyncTaskStatus;
import ac.entity.AsyncTypeState;
import ac.record.AsyncErrorRecord;
import androlic.entity.AbstractTypeState;
import androlic.entity.GlobalMessage;
import androlic.entity.value.IBasicValue;
import androlic.exception.AbstractAndrolicException;
import androlic.execution.ISymbolicEngineInstrumenter;

public class AsyncSymbolicInstrumenter implements ISymbolicEngineInstrumenter{

	private static AsyncSymbolicInstrumenter processor;

	private AsyncSymbolicInstrumenter() {
	}

	public static AsyncSymbolicInstrumenter v() {
		if (processor == null) {
			processor = new AsyncSymbolicInstrumenter();
		}
		return processor;
	}
	
	public void onPreStmtExecution(Unit currentUnit, GlobalMessage globalMessage) {
//		String str = "$r0 = staticinvoke <com.afollestad.materialdialogs.MaterialDialog$ListType: com.afollestad.materialdialogs.MaterialDialog$ListType[] values()>()";
//		if (currentUnit.toString().contains(str)) {
//			System.out.println("");
//		}
	}

	public void onPostStmtExecution(Unit currentUnit, GlobalMessage globalMessage) {
		if (globalMessage.getContextStack().isEmpty() && (currentUnit instanceof ReturnStmt || currentUnit instanceof ReturnVoidStmt)) {
			Map<IBasicValue, AbstractTypeState> objectToTypeState = globalMessage.getObjectToTypeState();
			for (Map.Entry<IBasicValue, AbstractTypeState> entry: objectToTypeState.entrySet()) {
				if (entry.getKey() instanceof AsyncTaskRefObject) {
					AsyncTaskRefObject asyncObject = (AsyncTaskRefObject) entry.getKey();
					AsyncTypeState state = (AsyncTypeState) entry.getValue();
					if (state.getCurrentStatus() == AsyncTaskStatus.RUNNING && !state.isCancelled()) {
//						if (!AsyncMain.notCancelSet.contains(asyncObject.getInitStatement())) {
						if (!AsyncMain.notCancelMap.containsKey(asyncObject.getObjectKey())) {
							AsyncErrorRecord.recordNotCancel((AsyncTaskRefObject) entry.getKey(), globalMessage);
//							AsyncMain.notCancelSet.add(asyncObject);
							AsyncMain.notCancelMap.put(asyncObject.getObjectKey(), asyncObject);
						}
//						AsyncMain.errorInstanceSet.add(asyncObject);
//						AsyncMain.rightInstanceSet.remove(asyncObject);
					}
				}
			}
		}
	}

	public void onExceptionProcess(Unit currentUnit, GlobalMessage globalMessage, AbstractAndrolicException exception) {
		
	}

}
