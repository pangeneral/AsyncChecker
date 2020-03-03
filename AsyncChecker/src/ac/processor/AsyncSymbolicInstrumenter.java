package ac.processor;

import java.util.Map;
import soot.Unit;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import ac.entity.AsyncTaskRefObject;
import ac.entity.AsyncTaskStatus;
import ac.entity.AsyncTypeState;
import ac.record.AsyncErrorRecord;
import androlic.entity.AbstractTypeState;
import androlic.entity.GlobalMessage;
import androlic.entity.value.IBasicValue;
import androlic.exception.AbstractAndrolicException;
import androlic.execution.ISymbolicEngineInstrumenter;
import androlic.util.Log;

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
	
	@Override
	public void onPreStmtExecution(Unit currentUnit, GlobalMessage globalMessage) {
//		String str = "virtualinvoke $r0.<com.farmerbb.taskbar.activity.SelectAppActivity: void setContentView(int)>(2131492930)";
		String str = "$r9 = new com.farmerbb.taskbar.activity.SelectAppActivity$AppListGenerator";
		if (currentUnit.toString().contains(str)) {
			Log.e("find asynctask");
		}
	}

	@Override
	public void onPostStmtExecution(Unit currentUnit, GlobalMessage globalMessage) {
		if (globalMessage.getContextStack().isEmpty() && (currentUnit instanceof ReturnStmt || currentUnit instanceof ReturnVoidStmt)) {
			Map<IBasicValue, AbstractTypeState> objectToTypeState = globalMessage.getObjectToTypeState();
			for (Map.Entry<IBasicValue, AbstractTypeState> entry: objectToTypeState.entrySet()) {
				if (entry.getKey() instanceof AsyncTaskRefObject) {
					AsyncTypeState state = (AsyncTypeState) entry.getValue();
					if (state.getCurrentStatus() == AsyncTaskStatus.RUNNING && !state.isCancelled()) {
						AsyncErrorRecord.recordNotCancel((AsyncTaskRefObject) entry.getKey(), globalMessage);
					}
				}
			}
		}
	}

	@Override
	public void onExceptionProcess(Unit currentUnit, GlobalMessage globalMessage, AbstractAndrolicException exception) {
		Map<IBasicValue, AbstractTypeState> objectToTypeState = globalMessage.getObjectToTypeState();
		for (Map.Entry<IBasicValue, AbstractTypeState> entry: objectToTypeState.entrySet()) {
			if (entry.getKey() instanceof AsyncTaskRefObject) {
				AsyncTypeState state = (AsyncTypeState) entry.getValue();
				if (state.getCurrentStatus() == AsyncTaskStatus.RUNNING && !state.isCancelled()) {
					AsyncErrorRecord.recordNotCancel((AsyncTaskRefObject) entry.getKey(), globalMessage);
				}
			}
		}
	}
}
