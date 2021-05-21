package ac.entrypoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import ac.entity.AsyncEntryPoint;
import androlic.constant.ClassSignature;
import androlic.entrypoint.activity.AndroidEntryPointConstants;
import androlic.util.ClassInheritanceProcess;
import androlic.util.PolymorphismProcess;
import androlic.util.ClassInheritanceProcess.MatchType;

public class EntryPointCreation {
	private static Set<SootClass> recordedSootClass = new HashSet<SootClass>();
	private static Set<SootMethod> recordedSootMethod = new HashSet<SootMethod>();
	
	public static List<SootMethod> generateLifeCycleMethodList(SootClass activityClass) {
		List<SootMethod> methodList = new ArrayList<SootMethod>();
		for (int i = 0; i < AndroidEntryPointConstants.getActivityLifecycleMethods().size(); i++) {
			SootMethod currentMethod = activityClass.getMethodUnsafe(
					AndroidEntryPointConstants.getActivityLifecycleMethods().get(i));
			if (currentMethod != null && currentMethod.hasActiveBody()) {
				methodList.add(currentMethod);
			}
		}
		return methodList;
	}
	
	public static void generateEntryMethodList(SootMethod sm, List<AsyncEntryPoint> entryMethodList, 
			Set<SootMethod> recordedMethodSet, List<SootMethod> contextMethods) {
		if (sm != null && sm.hasActiveBody()) {
			contextMethods.add(sm);
			UnitGraph graph = new BriefUnitGraph(sm.getActiveBody());
			Iterator<Unit> unitIt = graph.iterator();
			while (unitIt.hasNext()) {
				Unit currentUnit = unitIt.next();
				if (currentUnit instanceof AssignStmt) {
					Value rightOp = ((AssignStmt) currentUnit).getRightOp();
					if (rightOp instanceof NewExpr) {
						SootClass currentClass = ((NewExpr) ((AssignStmt) currentUnit).getRightOp()).getBaseType().getSootClass();
						if (ClassInheritanceProcess.isInheritedFromGivenClass(currentClass, "android.os.AsyncTask", MatchType.equal) && 
								!recordedSootClass.contains(currentClass)) {
							recordedSootClass.add(currentClass);
							SootMethod newEntryMethod = contextMethods.get(contextMethods.size() - 1);
							if (recordedSootMethod.contains(newEntryMethod)) {
								continue;
							}
							
							recordedSootMethod.add(newEntryMethod);
							AsyncEntryPoint newEntryPoint = new AsyncEntryPoint(contextMethods.get(0), 
									newEntryMethod, currentClass);
							entryMethodList.add(newEntryPoint);
						}
					}
					if (rightOp instanceof InvokeExpr) {
						List<SootMethod> methodList = getSootMethod((InvokeExpr) rightOp);
						for (int i = 0; i < methodList.size(); i++) {
							if (!recordedMethodSet.contains(methodList.get(i))) {
								recordedMethodSet.add(methodList.get(i));
								generateEntryMethodList(methodList.get(i), entryMethodList, recordedMethodSet, contextMethods);
							}
						}
					}
				}
				else if (currentUnit instanceof InvokeStmt) {
					List<SootMethod> methodList = getSootMethod(((InvokeStmt) currentUnit).getInvokeExpr());
					for (int i = 0; i < methodList.size(); i++) {
						if (!recordedMethodSet.contains(methodList.get(i))) {
							recordedMethodSet.add(methodList.get(i));
							generateEntryMethodList(methodList.get(i), entryMethodList, recordedMethodSet, contextMethods);
						}
					}
				}
			}
			contextMethods.remove(contextMethods.size() - 1);
		}
	}
		
	
	private static List<SootMethod> getSootMethod(InvokeExpr ie) {
		if (ie instanceof StaticInvokeExpr) {
			return Collections.singletonList(ie.getMethod());
		}
		else if (ie instanceof SpecialInvokeExpr) {
			return Collections.singletonList(PolymorphismProcess.getSpecialInvokedMethod((SpecialInvokeExpr) ie));
		}
		else if (ie instanceof VirtualInvokeExpr || ie instanceof InterfaceInvokeExpr) {
			return new ArrayList<SootMethod>(getPossibleMethodList((InstanceInvokeExpr) ie));
		}
		else
			return new ArrayList<SootMethod>();
	}
	
	public static Set<SootMethod> getPossibleMethodList(InstanceInvokeExpr ie){
		SootClass baseClass = Scene.v().getSootClassUnsafe(ie.getBase().getType().toString());
		String subsignature = ie.getMethod().getSubSignature();
		Set<SootMethod> resultSet = new HashSet<SootMethod>();
		
		if( ie.getMethod() != null && ie.getMethod().hasActiveBody() )
			resultSet.add(ie.getMethod());
		
		//The invoked method may be the method of superclass of baseClass
		SootClass currentClass = baseClass;
		while( currentClass != null && !currentClass.getType().toString().equals(ClassSignature.OBJECT) ){
			SootMethod currentMethod = currentClass.getMethodUnsafe(subsignature);
			if( currentMethod != null && currentMethod.hasActiveBody() )
				resultSet.add(currentMethod);
			currentClass = currentClass.getSuperclassUnsafe();
		}
		
		findAllMethodInSubClass(resultSet, baseClass, subsignature);
		return resultSet;
	}
	
	/**
	 * Find all methods with given subsignature in subclass of baseClass or subclass of subclass of baseClass
	 * @param methodSet
	 * @param baseClass
	 * @param subsignature
	 */
	private static void findAllMethodInSubClass(Set<SootMethod> methodSet, SootClass baseClass, String subsignature){
		Collection<SootClass> subclasses = Scene.v().getOrMakeFastHierarchy().getSubclassesOf(baseClass);
		Iterator<SootClass> it = subclasses.iterator();
		while( it.hasNext() ){
			SootClass currentSubClass = it.next();
			if (currentSubClass.resolvingLevel() < SootClass.SIGNATURES)
				continue;
			SootMethod currentMethod = currentSubClass.getMethodUnsafe(subsignature);
			if( currentMethod != null && currentMethod.hasActiveBody() )
				methodSet.add(currentMethod);
			findAllMethodInSubClass(methodSet, currentSubClass, subsignature);
		}
	}
}
