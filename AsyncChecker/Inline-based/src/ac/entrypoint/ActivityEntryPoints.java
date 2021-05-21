package ac.entrypoint;

import java.util.ArrayList;
import java.util.List;

import ac.entity.AsyncEntryPoint;
import soot.SootMethod;

public class ActivityEntryPoints {

	private List<AsyncEntryPoint> list = null;

	private List<SootMethod> sootMethods = new ArrayList<SootMethod>();

	public List<SootMethod> getSootMethods() {
		return sootMethods;
	}

	public ActivityEntryPoints(List<AsyncEntryPoint> list) {
		this.list = list;
		createSootMethods();
	}

	private void createSootMethods() {
		if (list == null) {
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			sootMethods.add(ActivityEntryMethod.create(list.get(i),i));
		}
	}

}
