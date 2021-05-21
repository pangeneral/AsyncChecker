package ac.summary.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ac.summary.ActivityMethodListSummary;
import ac.summary.ReturnValueMethodSummary;
import ac.summary.valuesource.AbstractValueSource;
import ac.summary.valuesource.ExplicitAllocationSite;
import ac.summary.valuesource.FieldRefValueSource;
import ac.summary.valuesource.LibraryAllocationSite;
import ac.summary.valuesource.ParameterValueSource;
import ac.summary.valuesource.ThisRefValueSource;
import ac.summary.valuesource.ValueSourceType;
import ac.topology.AbstractTopologyOperation;
import ac.topology.ActivityTopologyOperation;
import ac.topology.MethodReturnValueTopologyOperation;
import soot.Body;
import soot.Local;
import soot.RefType;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;

public class ValueSourceProcessor {
	
	private static ValueSourceProcessor processor;
	
	private ValueSourceProcessor() {
		
	}
	
	public static ValueSourceProcessor v() {
		if (processor == null) {
			processor = new ValueSourceProcessor();
		}
		return processor;
	}
	
	/**
	 * Process ThisRef and ParameterRef
	 * @param theMethod
	 * @return
	 */
	public Map<Local, AbstractValueSource> processLocalOfIdentityStmt(SootMethod theMethod) {
		if (theMethod.hasActiveBody()) {
			Map<Local, AbstractValueSource> localToClass = new HashMap<Local, AbstractValueSource>();
			Body body = theMethod.getActiveBody();
			if (!theMethod.isStatic()) {
				localToClass.put(body.getThisLocal(), new ThisRefValueSource(theMethod.getDeclaringClass()));
			}
			for (int i = 0; i < body.getParameterLocals().size(); i++) {
				if (body.getParameterRefs().get(i).getType() instanceof RefType) {
					ParameterRef pr = (ParameterRef) body.getParameterRefs().get(i);
					localToClass.put(body.getParameterLocals().get(i), new ParameterValueSource(i, pr));
				}
			}
			return localToClass;
		}
		else {
			return new HashMap<Local, AbstractValueSource>();
		}
	}
	
	/**
	 * Given a value, e.g. local, find which object it points and return the type of the object via intra-procedural analysis.
	 * If we can't find the object, return null.
	 * @param base
	 * @param theGraph
	 * @param currentUnit
	 * @param localToSource
	 * @param visitedSet
	 * @return
	 */
	public AbstractValueSource findValueSourceOfBase(ActivityMethodListSummary summary, Value base, List<Unit> unitList) {
		Value currentValue = base;
		Set<SootField> appearedField = new HashSet<SootField>();
		if (currentValue instanceof FieldRef) {
			appearedField.add(((FieldRef) currentValue).getField());
		}
		Map<Local, AbstractValueSource> localToSource = this.processLocalOfIdentityStmt(summary.getMethodUnderAnalysis());
		for (int i = unitList.size() - 1; i >= 0; i--) {
			Stmt currentStmt = (Stmt) unitList.get(i);
			if (localToSource.containsKey(currentValue)) { // current base points to ThisRef or ParameterRef
				return localToSource.get(currentValue);
			}
			// Check whether invoked method contains object which is assigned to a AsyncTask field
			if (currentStmt.containsInvokeExpr() && currentValue instanceof FieldRef && currentStmt.getInvokeExpr().getMethod().hasActiveBody()) {
				SootMethod invokedMethod = currentStmt.getInvokeExpr().getMethod();
				SootField field = ((FieldRef) currentValue).getField();
				String key = ActivityTopologyOperation.getActivityOperationKey(invokedMethod);
				ActivityMethodListSummary invokedMethodSummary = (ActivityMethodListSummary) AbstractTopologyOperation.getsMethodKeyToSummary().get(key);
				if (invokedMethodSummary != null && invokedMethodSummary.getFieldToAssignStmt().containsKey(field)) {
					AssignStmt stmt = invokedMethodSummary.getFieldToAssignStmt().get(field);
					summary.getFieldToAssignStmt().put(field, stmt);
					return new ExplicitAllocationSite(stmt);
				}
			}
			if (currentStmt instanceof AssignStmt) {
				Value leftOp = ((AssignStmt) currentStmt).getLeftOp();
				Value rightOp = ((AssignStmt) currentStmt).getRightOp();
				if (this.LeftOpEqual(leftOp, currentValue)) { // find the allocation site or update base
					if (rightOp instanceof NewExpr) {
						for (SootField field: appearedField) {
							summary.getFieldToAssignStmt().put(field, (AssignStmt) currentStmt);
						}
						return new ExplicitAllocationSite((AssignStmt) currentStmt); 
					}
					else if (rightOp instanceof Local) {
						currentValue = rightOp;
					}
					else if (rightOp instanceof FieldRef) {
						currentValue = rightOp;
						appearedField.add(((FieldRef) rightOp).getField());
					}
					else if (rightOp instanceof CastExpr) {
						currentValue = ((CastExpr) rightOp).getOp();
					}
					else if (rightOp instanceof InvokeExpr) { 
						if (((InvokeExpr) rightOp).getMethod().hasActiveBody()) {
							SootMethod invokedMethod = ((InvokeExpr) rightOp).getMethod(); 
							ReturnValueMethodSummary returnSummary = this.getReturnValueMethodSummary(invokedMethod);
							if (returnSummary.getSourceOfReturnValue() instanceof FieldRefValueSource) {
								currentValue = ((FieldRefValueSource) returnSummary.getSourceOfReturnValue()).getFieldRef();
							}
							else {
								return returnSummary.getSourceOfReturnValue();
							}
						}
						else {
							return new LibraryAllocationSite(currentStmt);
						}
					}
				}
			}
		}
		if (currentValue instanceof FieldRef) {
			return new FieldRefValueSource((FieldRef) currentValue);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Given a value, e.g. local, find which object it points and return the type of the object via intra-procedural analysis.
	 * If we can't find the object, return null.
	 * @param base
	 * @param theGraph
	 * @param currentUnit
	 * @param localToSource
	 * @param visitedSet
	 * @return
	 */
	public AbstractValueSource findValueSourceOfReturnValue(SootMethod methodUnderAnalysis, Value base, List<Unit> unitList) {
		Value currentValue = base;
		Set<SootField> appearedField = new HashSet<SootField>();
		if (currentValue instanceof FieldRef) {
			appearedField.add(((FieldRef) currentValue).getField());
		}
		Map<Local, AbstractValueSource> localToSource = this.processLocalOfIdentityStmt(methodUnderAnalysis);
		for (int i = unitList.size() - 1; i >= 0; i--) {
			Stmt currentStmt = (Stmt) unitList.get(i);
			if (localToSource.containsKey(currentValue)) { // current base points to ThisRef or ParameterRef
				return localToSource.get(currentValue);
			}
			if (currentStmt instanceof AssignStmt) {
				Value leftOp = ((AssignStmt) currentStmt).getLeftOp();
				Value rightOp = ((AssignStmt) currentStmt).getRightOp();
				if (this.LeftOpEqual(leftOp, currentValue)) { // find the allocation site or update base
					if (rightOp instanceof NewExpr) {
						return new ExplicitAllocationSite((AssignStmt) currentStmt); 
					}
					else if (rightOp instanceof Local) {
						currentValue = rightOp;
					}
					else if (rightOp instanceof FieldRef) {
						currentValue = rightOp;
						appearedField.add(((FieldRef) rightOp).getField());
					}
					else if (rightOp instanceof CastExpr) {
						currentValue = ((CastExpr) rightOp).getOp();
					}
					else if (rightOp instanceof InvokeExpr) {
						if (((InvokeExpr) rightOp).getMethod().hasActiveBody()) {
							SootMethod invokedMethod = ((InvokeExpr) rightOp).getMethod(); 
							ReturnValueMethodSummary summary = this.getReturnValueMethodSummary(invokedMethod);
							if (summary.getSourceOfReturnValue() instanceof FieldRefValueSource) {
								currentValue = ((FieldRefValueSource) summary.getSourceOfReturnValue()).getFieldRef();
							}
							else {
								return summary.getSourceOfReturnValue();
							}
						}
						else {
							return new LibraryAllocationSite(currentStmt);
						}
					}
				}
			}
		}
		if (currentValue instanceof FieldRef) {
			return new FieldRefValueSource((FieldRef) currentValue);
		}
		else {
			return null;
		}
	}
	
	public AbstractValueSource updateValueSource(AbstractValueSource source, InvokeExpr expr, List<Unit> unitList, ActivityMethodListSummary summary) {
		Value base = null;
		if (source.getType() == ValueSourceType.FIELD_REF) {
			base = ((FieldRefValueSource) source).getFieldRef();
		}
		else if (source.getType() == ValueSourceType.PARAMETER) {
			base = expr.getArg(((ParameterValueSource) source).getParameterIndex());
		}
		if (base != null) {
			return this.findValueSourceOfBase(summary, base, unitList);
		}
		else {
			return source;
		}
	}
	
	private boolean LeftOpEqual(Value leftOp, Value value) {
		if (value instanceof FieldRef && leftOp instanceof FieldRef) {
			return ((FieldRef) leftOp).getField().equals(((FieldRef) value).getField());
		}
		else {
			return leftOp.equals(value);
		}
	}
	
	private ReturnValueMethodSummary getReturnValueMethodSummary(SootMethod method) {
		String key = MethodReturnValueTopologyOperation.getMethodReturnValueKey(method); 
		if (!AbstractTopologyOperation.getsMethodKeyToSummary().containsKey(key)) {
			 AbstractTopologyOperation operation = new MethodReturnValueTopologyOperation(method);
			 operation.constructMainSummary();
			 AbstractTopologyOperation.getsMethodKeyToSummary().put(key, operation.getSourceMethodSummary());
		}
		return (ReturnValueMethodSummary) AbstractTopologyOperation.getsMethodKeyToSummary().get(key);
	}
}
