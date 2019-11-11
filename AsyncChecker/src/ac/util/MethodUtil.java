package ac.util;

import androlic.util.MethodSynchronize;
import soot.SootClass;
import soot.SootMethod;

public class MethodUtil {
	/**
	 * Get the doInBackground implemented by the class that extends AsyncTask
	 * @param sootClass
	 * @param methodName
	 * @return
	 */
	public static SootMethod getMethod(SootClass sootClass, String methodName) {
		if (sootClass == null) {
			return null;
		}
		SootMethod resultMethod = null;
		for (SootMethod sootMethod : MethodSynchronize.getSootMethods(sootClass)) {
			if (sootMethod.getName().equals(methodName)
					&& sootMethod.getDeclaration().contains("transient")
					&& !sootMethod.getDeclaration().contains("volatile")) {
				return sootMethod;
			}
			if (sootMethod.getName().equals(methodName)
					&& sootMethod.getDeclaration().contains("transient")) {
				resultMethod = sootMethod;
				continue;
			}
			if (sootMethod.getName().equals(methodName) && resultMethod == null) {
				resultMethod = sootMethod;
			}
		}

		if (resultMethod == null && sootClass.hasSuperclass()) {
			SootClass superclass = sootClass.getSuperclass();
			return getMethod(superclass, methodName);
		}

		return resultMethod;
	}

}
