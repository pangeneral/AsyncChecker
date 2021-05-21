package ac.entrypoint;

import java.util.ArrayList;
import java.util.List;

import androlic.entrypoint.activity.AndroidCallBacks;
import androlic.entrypoint.activity.AndroidCallBacks.ActivityInfo;
import soot.SootClass;
import soot.SootMethod;
import soot.util.MultiMap;

public class ActivityCallBackCollector {

	private ActivityCallBackCollector() {
	}

	public static List<SootMethod> getCallBackSootMethods(SootClass activity) {
		List<SootMethod> sootMethods = new ArrayList<SootMethod>();
		ActivityInfo activityInfo = AndroidCallBacks.getSootClassesInvoked(activity, null, null);

		MultiMap<SootClass, SootMethod> callbackClasses = AndroidCallBacks
				.getCallBackMultiMap(activityInfo.mInvokedSootClasses);
		for (SootClass listenerClass : callbackClasses.keySet()) {
			sootMethods.addAll(callbackClasses.get(listenerClass));
		}
		return sootMethods;
	}

}
