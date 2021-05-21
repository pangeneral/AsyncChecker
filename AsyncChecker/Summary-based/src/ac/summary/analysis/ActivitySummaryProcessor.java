package ac.summary.analysis;

import java.util.List;
import soot.Local;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import ac.constant.MethodSignature;
import ac.summary.ActivityMethodListSummary;
import ac.summary.alphabet.activity.AbstractActivityUnitSummary;
import ac.summary.alphabet.activity.ActivityNullSummary;
import ac.summary.alphabet.activity.AssignAsyncTaskUnitSummary;
import ac.summary.alphabet.activity.CancelAsyncTaskUnitSummary;
import ac.summary.alphabet.activity.StartAsyncTaskUnitSummary;
import ac.summary.valuesource.AbstractValueSource;
import ac.util.InheritanceProcess;

public class ActivitySummaryProcessor {
	
	private static ActivitySummaryProcessor processor;
	
	private ActivitySummaryProcessor() {
		
	}
	
	public static ActivitySummaryProcessor v() {
		if (processor == null) {
			processor = new ActivitySummaryProcessor();
		}
		return processor;
	}
	
	private void updateFieldToClass(AssignStmt assignStmt, ActivityMethodListSummary methodSummary, List<Unit> unitList) {
		Value leftOp = assignStmt.getLeftOp();
		if (leftOp instanceof FieldRef) {
			Type type = ((FieldRef) leftOp).getField().getType();
			if (type instanceof RefType) {
				if (InheritanceProcess.isInheritedFromAsyncTask(((RefType) type).getSootClass())) {
					ValueSourceProcessor.v().findValueSourceOfBase(methodSummary, leftOp, unitList);
				}
			}
		}
	}
	
	public AbstractActivityUnitSummary getUnitSummary(Unit currentUnit, ActivityMethodListSummary methodSummary, List<Unit> unitList) {
		AbstractActivityUnitSummary summary = null;
		Stmt currentStmt = (Stmt) currentUnit;
		SootMethod methodUnderAnalysis = methodSummary.getMethodUnderAnalysis();
		if (currentStmt.containsInvokeExpr()) { // check whether cancel or execute is invoked
			InvokeExpr expr = currentStmt.getInvokeExpr();
			String subSignature = expr.getMethod().getSubSignature();
			Local local = null;
			if (expr instanceof InstanceInvokeExpr) {
				local = (Local) ((InstanceInvokeExpr) expr).getBase();
			}
			if (local != null && local.getType() instanceof RefType
					&& InheritanceProcess.isInheritedFromAsyncTask(((RefType) local.getType()).getSootClass())) {
				switch (subSignature) {
					case MethodSignature.SUB_CANCEL_SIGNATURE:
						summary = new CancelAsyncTaskUnitSummary(methodUnderAnalysis, currentUnit);
						break;
					case MethodSignature.SUB_EXECUTE_SIGNATURE:
					case MethodSignature.SUB_EXECUTE_ON_EXECUTOR:
						summary = new StartAsyncTaskUnitSummary(methodUnderAnalysis, currentUnit, unitList);
						break;
				}
				if (summary != null) {
					AbstractValueSource source = ValueSourceProcessor.v().findValueSourceOfBase(methodSummary, local, unitList);
					if (source != null) {
						summary.setValueSource(source);
						return summary;
					}
				}
			}
		}
		else if (currentStmt instanceof AssignStmt) { // check whether AsyncTask instance is created 
			Value rightOp = ((AssignStmt) currentStmt).getRightOp();
			this.updateFieldToClass((AssignStmt) currentStmt, methodSummary, unitList);
			if (rightOp instanceof NewExpr) {
				if (InheritanceProcess.isInheritedFromAsyncTask(((NewExpr) rightOp).getBaseType().getSootClass())) {
					return new AssignAsyncTaskUnitSummary(methodUnderAnalysis, (AssignStmt) currentUnit);
				}
			}
		}
		return new ActivityNullSummary(methodUnderAnalysis, currentUnit);
	}
	
	
}
