package ac.entity;

import java.util.HashMap;
import java.util.Map;

import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.Stmt;
import ac.constant.AsyncClassSignature;
import androlic.entity.GlobalMessage;
import androlic.entity.value.IBasicValue;
import androlic.entity.value.heap.ref.NewRefHeapObject;
import androlic.util.ClassInheritanceProcess;
import androlic.util.ClassInheritanceProcess.MatchType;


public class AsyncTaskRefObject extends NewRefHeapObject {
	
	private String objectKey = "";
	
	public String getObjectKey() {
		return objectKey;
	}
	
	public AsyncTaskRefObject(Stmt initStatement, GlobalMessage globalMessage) {
		super(initStatement);
		StringBuilder sb = new StringBuilder("");
		for (int i = 0; i < globalMessage.getContextStack().size(); i++) {
			SootMethod sootMethod = globalMessage.getContextStack().get(i).getMethod();
			String signature = sootMethod.getName();
			try {
				signature = sootMethod.getSignature();
			} catch (Exception e) {
				// TODO: handle exception
			}
			sb.append(signature + "--");
		}
		sb.append(initStatement.hashCode());
		this.objectKey = sb.toString();
	}
	
	public AsyncTypeState getTypeState(GlobalMessage globalMessage) {
		return (AsyncTypeState) globalMessage.getObjectToTypeState().get(this);
	}
	
	public Map<SootField, IBasicValue> getHoldingStrongReferenceField(GlobalMessage globalMessage) {
		Map<SootField, IBasicValue> fieldToValue = globalMessage.getNonStaticFieldToObject().getOrDefault(this, new HashMap<SootField, IBasicValue>());
		Map<SootField, IBasicValue> resultMap = new HashMap<SootField, IBasicValue>();
		for (Map.Entry<SootField, IBasicValue> entry: fieldToValue.entrySet()) {
			SootClass fieldClass = Scene.v().getSootClassUnsafe(entry.getKey().getType().toString());
			if (this.isInheritedFromView(fieldClass) || ClassInheritanceProcess.isInheritedFromActivity(fieldClass))
				resultMap.put(entry.getKey(), entry.getValue());
		}
		return resultMap;
	}
	
	private boolean isInheritedFromView(SootClass sc) {
		return ClassInheritanceProcess.isInheritedFromGivenClass(sc, AsyncClassSignature.VIEW, MatchType.equal);
	}

}
