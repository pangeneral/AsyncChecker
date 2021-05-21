package ac.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class DataFlowProcess {	
	
	/**
	 * Process ThisRef and ParameterRef
	 * @param theMethod
	 * @return
	 */
	public static Map<Local, SootClass> processLocalOfIdentityStmt(SootMethod theMethod) {
		if (theMethod.hasActiveBody()) {
			Map<Local, SootClass> localToClass = new HashMap<Local, SootClass>();
			Body body = theMethod.getActiveBody();
			if (!theMethod.isStatic()) {
				localToClass.put(body.getThisLocal(), theMethod.getDeclaringClass());
			}
			for (int i = 0; i < body.getParameterLocals().size(); i++) {
				if (body.getParameterRefs().get(i).getType() instanceof RefType) {
					RefType parameterType = (RefType) body.getParameterRefs().get(i).getType();
					localToClass.put(body.getParameterLocals().get(i), parameterType.getSootClass());
				}
			}
			return localToClass;
		}
		else {
			return new HashMap<Local, SootClass>();
		}
	}
	
	public static SootClass findInstantiateClassOfLocal(Local base, SootMethod theMethod, Unit currentUnit) {
		Map<Local, SootClass> localToClass = DataFlowProcess.processLocalOfIdentityStmt(theMethod);
		
		UnitGraph theGraph = new BriefUnitGraph(theMethod.getActiveBody());
		Set<Unit> visitedSet = new HashSet<Unit>();
		
		SootClass theClass = DataFlowProcess.findInstantiateClassOfLocal(base, theGraph, currentUnit, localToClass, visitedSet);
		visitedSet.clear();
		return theClass;
	}
	
	/**
	 * Given a reference local variable, find which object it points and return the type of the object via intra-procedural analysis.
	 * If we can't find the object, return null.
	 * @param base
	 * @param theGraph
	 * @param currentUnit
	 * @param localToClass
	 * @param visitedSet
	 * @return
	 */
	private static SootClass findInstantiateClassOfLocal(Value base, UnitGraph theGraph, 
			Unit currentUnit, Map<Local, SootClass> localToClass, Set<Unit> visitedSet) {
		if (theGraph.getBody().getMethod().getSignature().equals("<com.afollestad.materialdialogs.DialogInit: void init(com.afollestad.materialdialogs.MaterialDialog)>")) {
			System.out.println(currentUnit);
		}
		if (localToClass.containsKey(base)) { // current local variable points to ThisRef or ParameterRef
			return localToClass.get(base);
		}
		List<Unit> unitList = theGraph.getPredsOf(currentUnit); // analyze backwardly
		for (int i = 0; i < unitList.size(); i++) {
			Unit prevUnit = unitList.get(i);
			if (visitedSet.contains(prevUnit)) { // a unit can only be analyzed once in a backward path
				continue;
			}
			visitedSet.add(prevUnit);
			if (prevUnit instanceof AssignStmt) {
				Value leftOp = ((AssignStmt) prevUnit).getLeftOp();
				Value rightOp = ((AssignStmt) prevUnit).getRightOp();
				if (leftOp.equals(base)) { // find the base
					if (rightOp instanceof NewExpr) {
						return ((NewExpr) rightOp).getBaseType().getSootClass(); 
					}
					else if (rightOp instanceof Local || rightOp instanceof FieldRef) {
						return findInstantiateClassOfLocal(rightOp, theGraph, prevUnit, localToClass, visitedSet);
					}
					else if (rightOp instanceof CastExpr) {
						if (rightOp.getType() instanceof RefType) {
							return ((RefType) rightOp.getType()).getSootClass();
						}
						else {
							return null;
						}
					}
					else if (rightOp instanceof InvokeExpr) {
						Type returnType = ((InvokeExpr) rightOp).getMethod().getReturnType();
						if (returnType instanceof RefType) {
							return ((RefType) returnType).getSootClass();
						}
						else {
							return null;
						}
					}
					else {
						return null;
					}
				}
				else {
					SootClass resultClass = findInstantiateClassOfLocal(base, theGraph, prevUnit, localToClass, visitedSet);
					if (resultClass != null) {
						return resultClass;
					}
				}
			}
			else {
				SootClass resultClass = findInstantiateClassOfLocal(base, theGraph, prevUnit, localToClass, visitedSet);
				if (resultClass != null) {
					return resultClass;
				}
			}
			visitedSet.remove(prevUnit);
		}
		return null;
	}
}
