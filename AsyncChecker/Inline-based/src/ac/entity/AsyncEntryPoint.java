package ac.entity;

import soot.SootClass;
import soot.SootMethod;

public class AsyncEntryPoint {
	private SootMethod callbackMethod;
	
	private SootMethod newEntryMethod;
	
	private SootClass asyncClass;
	
	public SootMethod getCallbackMethod() {
		return callbackMethod;
	}

	public void setCallbackMethod(SootMethod callbackMethod) {
		this.callbackMethod = callbackMethod;
	}

	public SootMethod getNewEntryMethod() {
		return newEntryMethod;
	}

	public void setNewEntryMethod(SootMethod newEntryMethod) {
		this.newEntryMethod = newEntryMethod;
	}

	public SootClass getAsyncClass() {
		return asyncClass;
	}

	public void setAsyncClass(SootClass asyncClass) {
		this.asyncClass = asyncClass;
	}

	public AsyncEntryPoint(SootMethod callback, SootMethod newEntry, SootClass async) {
		this.callbackMethod = callback;
		this.newEntryMethod = newEntry;
		this.asyncClass = async;
	}
	
	public boolean isNewMethodLifeCycleMethod() {
		return callbackMethod == newEntryMethod;
	}
	
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("{");
		stringBuffer.append("newEntryMethod=");
		stringBuffer.append(newEntryMethod);
		stringBuffer.append(",callbackMethod=");
		stringBuffer.append(callbackMethod);
		stringBuffer.append("}");
		return stringBuffer.toString();
	}
}
