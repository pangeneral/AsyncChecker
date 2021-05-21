package ac.summary.analysis;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import ac.constant.MethodSignature;
import ac.util.InheritanceProcess;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class ContainAsyncTaskProcessor {
	
	private static ContainAsyncTaskProcessor processor;
	
	private ContainAsyncTaskProcessor() {
		
	}
	
	public static ContainAsyncTaskProcessor v() {
		if (processor == null) {
			processor = new ContainAsyncTaskProcessor();
		}
		return processor;
	}
	
	/**
	 * Judge whether method represented by theGraph contains AsyncTask Operation.
	 * If any statement contains variables whose type is AsyncTask or subclass of AsyncTask, we take it as a candidate method.
	 * 
	 * Methods that have many path conditions will lead to path explosion. 
	 * From our experience, such methods usually don't contain AsyncTask operation and can be omitted. 
	 * @param theGraph
	 */
	public boolean isMethodContainAsyncOperation(SootMethod currentMethod) {
		return this.isMethodContainAsyncOperation(currentMethod, new HashSet<SootMethod>());
	}
	
	private boolean isMethodContainAsyncOperation(SootMethod currentMethod, Set<SootMethod> visitedMethod) {
		if (visitedMethod.contains(currentMethod)) {
			return false;
		}
		visitedMethod.add(currentMethod);
		UnitGraph theGraph = new BriefUnitGraph(currentMethod.getActiveBody());
		Iterator<Unit> it = theGraph.iterator();
		boolean flag = false;
		while (it.hasNext()) {
			Unit currentUnit = it.next();
			if (this.isUnitContainAsyncTaskOperation(currentUnit, visitedMethod)) {
				flag = true;
			}
		}
		return flag;
	}
	
	public boolean isUnitContainAsyncTaskOperation(Unit currentUnit, Set<SootMethod> visitedMethod){
		if (currentUnit instanceof InvokeStmt) {
			InvokeExpr ie = ((InvokeStmt) currentUnit).getInvokeExpr();
			if (this.isInvokeExpressionContainAsyncTaskOperation(ie, visitedMethod))
				return true;
		}
		else if (currentUnit instanceof AssignStmt) {
			Type leftType = ((AssignStmt) currentUnit).getLeftOp().getType();
			Value rightOp = ((AssignStmt) currentUnit).getRightOp();
			if (this.isAsyncTaskType(leftType)) {
				return true;
			}
			if (this.isRightOpContainAsyncTaskOperation(rightOp, visitedMethod)) {
				return true;
			}
		}
		else if (currentUnit instanceof ReturnStmt) {
			return this.isAsyncTaskType(((ReturnStmt) currentUnit).getOp().getType());
		}
		return false;
	}
	
	private boolean isRightOpContainAsyncTaskOperation(Value rightOp, Set<SootMethod> visitedMethod) {
		if (rightOp instanceof FieldRef) {
			return this.isAsyncTaskType(((FieldRef) rightOp).getField().getType());
		}
		else if (rightOp instanceof InvokeExpr) {
			if (rightOp instanceof InstanceInvokeExpr) {
				Type type = ((InstanceInvokeExpr) rightOp).getBase().getType();
				if (this.isAsyncTaskType(type)) {
					return true;
				}
			}
			return this.isInvokeExpressionContainAsyncTaskOperation((InvokeExpr) rightOp, visitedMethod);
		}
		else {
			return this.isAsyncTaskType(rightOp.getType());
		}
	}
	
	private boolean isAsyncTaskType(Type type) {
		return type instanceof RefType && InheritanceProcess.isInheritedFromAsyncTask(((RefType) type).getSootClass());
	}
	
	private boolean isInvokeExpressionContainAsyncTaskOperation(InvokeExpr ie, Set<SootMethod> visitedMethod) {
		String subSignature = ie.getMethod().getSubSignature();
		if (subSignature.equals(MethodSignature.SUB_EXECUTE_SIGNATURE) 
				|| subSignature.equals(MethodSignature.SUB_CANCEL_SIGNATURE)
				|| subSignature.equals(MethodSignature.SUB_EXECUTE_ON_EXECUTOR))
			return true;
		if (ie.getMethod().hasActiveBody() && this.isMethodContainAsyncOperation(ie.getMethod(), visitedMethod)) 
			return true;
		else 
			return false;
	}
}
