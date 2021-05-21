package ac.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import androlic.constant.ClassSignature;
import androlic.constant.MethodSignature;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.SpecialInvokeExpr;


public class PolymorphismProcess {
	
	/**
	 * Find the method with given subsignature.
	 * If sc doesn't have the method, we find the method in the super class of sc until we find the method. 
	 * If sc is "java.lang.Object" or an interface, return null method.
	 * @param sc
	 * @param subsignature
	 * @return
	 */
	public static SootMethod getInterfaceOrVirtualInvokedMethod(SootClass sc, String subsignature){
		if( sc == null || sc.getName().equals(ClassSignature.OBJECT) || sc.isInterface() )
			return null;
		SootMethod sm = sc.getMethodUnsafe(subsignature);
		if( sm == null )
			return getInterfaceOrVirtualInvokedMethod(sc.getSuperclassUnsafe(), subsignature);
		else
			return sm;
	}
	
	public static SootMethod getSpecialInvokedMethod(SpecialInvokeExpr ie){
		return ie.getMethod().getDeclaringClass().getMethodUnsafe(ie.getMethod().getSubSignature());
	}
	
	public static SootMethod getClinitMethod(SootClass sc){
		return sc.getMethodByNameUnsafe(MethodSignature.CLINIT_NAME);
	}
	
	public static Set<SootMethod> getPossibleMethodList(InstanceInvokeExpr ie){
		SootClass baseClass = Scene.v().getSootClassUnsafe(ie.getBase().getType().toString());
		String subsignature = ie.getMethod().getSubSignature();
		Set<SootMethod> resultSet = new HashSet<SootMethod>();
		
		if( ie.getMethod() != null && ie.getMethod().hasActiveBody() )
			resultSet.add(ie.getMethod());
		
		//The invoked method may be the method of superclass of baseClass
//		SootClass currentClass = baseClass;
//		while (currentClass != null && !currentClass.getType().toString().equals(ClassSignature.OBJECT)) {
//			SootMethod currentMethod = null;
//			try {
//				currentMethod = currentClass.getMethodUnsafe(subsignature);
//			} catch (Exception e) {
////				ExceptionRecord.saveException(e);
//			}
//			currentClass.getSuperclass();
//			currentClass = currentClass.getSuperclassUnsafe();
//			if (currentMethod != null && currentMethod.hasActiveBody())
//				resultSet.add(currentMethod);
//		}
		
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
		//The invoked method may be the method of superclass of baseClass
		Collection<SootClass> subclasses = Scene.v().getOrMakeFastHierarchy().getSubclassesOf(baseClass);
		Iterator<SootClass> it = subclasses.iterator();
		while( it.hasNext() ){
			SootClass currentSubClass = it.next();
			SootMethod currentMethod = null;
			try {
				currentMethod = currentSubClass.getMethodUnsafe(subsignature);
			} catch (Exception e) {
//				ExceptionRecord.saveException(e);
			}
			if( currentMethod != null && currentMethod.hasActiveBody() )
				methodSet.add(currentMethod);
			findAllMethodInSubClass(methodSet, currentSubClass, subsignature);
		}
	}
}
