package ac.util;

import java.util.HashSet;
import java.util.Map;

import soot.RefType;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import ac.constant.AsyncMethodSignature;
import ac.entity.AsyncTaskRefObject;
import ac.record.AsyncErrorRecord;
import ac.summary.DoInBackgroundMethodSummary;
import androlic.entity.GlobalMessage;
import androlic.entity.value.IBasicValue;

public class AsyncTaskOperationChecker {
	
	public static void checkStrongReference(AsyncTaskRefObject asyncObject, Unit executeUnit, GlobalMessage globalMessage) {
		Map<SootField, IBasicValue> taintedField = asyncObject.getHoldingStrongReferenceField(globalMessage);
		if (taintedField.size() > 0 ) {
//			if (!AsyncMain.strongReferenceSet.contains(executeUnit)) {
			AsyncErrorRecord.recordStrongReference(asyncObject, taintedField, executeUnit, globalMessage);
//			AsyncMain.errorInstanceMap.put(asyncObject.getObjectKey(), asyncObject);
//			AsyncMain.rightInstanceMap.remove(asyncObject.getObjectKey());
//			AsyncMain.errorInstanceSet.add(asyncObject);
//			AsyncMain.rightInstanceSet.remove(asyncObject);
		}
	}
	
	public static void checkNotTerminate(AsyncTaskRefObject asyncObject, Unit executeUnit, GlobalMessage globalMessage) {
		SootMethod doInBackgroundMethod = MethodUtil.getMethod(
				((RefType) asyncObject.getType()).getSootClass(), AsyncMethodSignature.DO_IN_BACKGROUND_NAME);
		DoInBackgroundMethodSummary summary = new DoInBackgroundMethodSummary(doInBackgroundMethod);
		summary.generation(new HashSet<SootMethod>());
		if (!summary.isAllLoopCancelled()) {
//			if (!AsyncMain.notTerminateSet.contains(executeUnit)) {
			AsyncErrorRecord.recordNotTerminate(asyncObject, globalMessage);
//			AsyncMain.errorInstanceMap.put(asyncObject.getObjectKey(), asyncObject);
//			AsyncMain.rightInstanceMap.remove(asyncObject.getObjectKey());
//			AsyncMain.errorInstanceSet.add(asyncObject);
//			AsyncMain.rightInstanceSet.remove(asyncObject);
		}
	}
}
