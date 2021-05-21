package ac.topology;

import ac.summary.AbstractMethodSummary;
import ac.summary.ReturnValueMethodSummary;
import soot.SootMethod;

public class MethodReturnValueTopologyOperation extends AbstractTopologyOperation {

	public MethodReturnValueTopologyOperation(SootMethod sourceMethod) {
		super(sourceMethod);
	}
	
	public static String getMethodReturnValueKey(SootMethod theMethod) {
		return theMethod.getSignature() + "-RETURN-VALUE";
	}
	
	@Override
	public String getKey(SootMethod theMethod) {
		return MethodReturnValueTopologyOperation.getMethodReturnValueKey(theMethod);
	}

	@Override
	public AbstractMethodSummary getSourceMethodSummary() {
		return AbstractTopologyOperation.sMethodKeyToSummary.get(getMethodReturnValueKey(this.mSourceMethod));
	}

	@Override
	public AbstractMethodSummary getMethodSummary(TopologyNode topNode) {
		return new ReturnValueMethodSummary(topNode.mMethod);
	}
	
}
