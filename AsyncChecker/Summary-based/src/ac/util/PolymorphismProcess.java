package ac.util;

import ac.constant.ClassSignature;
import soot.SootClass;
import soot.SootMethod;

public class PolymorphismProcess {
	/**
	 * Find the method with given subsignature.
	 * If sc doesn't have the method, we find the method in the super class of sc until we find the method. 
	 * If sc is "java.lang.Object" or an interface, return null method.
	 * @param sc
	 * @param subsignature
	 * @return
	 */
	public static SootMethod getInterfaceOrVirtualInvokedMethod(SootClass sc, String subsignature) {
		if (sc == null || sc.getName().equals(ClassSignature.OBJECT) || sc.isInterface()) {
			return null;
		}
		if (sc.declaresMethod(subsignature)) {
			return sc.getMethodUnsafe(subsignature);
		}
		else {
			return getInterfaceOrVirtualInvokedMethod(sc.getSuperclassUnsafe(), subsignature);
		}
	}
}
