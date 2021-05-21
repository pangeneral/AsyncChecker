package ac.record;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import com.config.parameter.BaseConfiguration;
import ac.util.InheritanceProcess;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.NewExpr;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class AsyncClassCountRecorder {
	private static void searchInstantiatedAsyncClass(Set<SootClass> asyncClasses) {
		for (SootClass sc: Scene.v().getApplicationClasses()) {
			for (SootMethod method: sc.getMethods()) {
				if (method.hasActiveBody()) {
					try {
						UnitGraph graph = new BriefUnitGraph(method.getActiveBody());
						Iterator<Unit> it = graph.iterator();
						while (it.hasNext()) {
							Unit unit = it.next();
							if (unit instanceof AssignStmt) {
								if (((AssignStmt) unit).getRightOp() instanceof NewExpr) {
									RefType newType = ((NewExpr) ((AssignStmt) unit).getRightOp()).getBaseType();
									if (InheritanceProcess.isInheritedFromAsyncTask(newType.getSootClass())) {
										asyncClasses.add(newType.getSootClass());
									}
								}
							}
						}
					}
					catch(Exception e) {
						ExceptionRecorder.saveException(e);
						continue;
					}
				}
			}
		}
	}
	
	public static void recordInstantiatedAsyncClass() {
		Set<SootClass> asyncClasses = new HashSet<SootClass>();
		AsyncClassCountRecorder.searchInstantiatedAsyncClass(asyncClasses);
		String filePath = BaseConfiguration.OUTPUT_BASE_PATH + File.separator + "async-class-count.txt";
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath), true)));
			for (SootClass sc: asyncClasses) {
				out.write(sc.getName() + "\n");
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void recordCheckedAsyncClass(SootClass sc) {
		String filePath = BaseConfiguration.OUTPUT_BASE_PATH + File.separator + "checked-async-class-count.txt";
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath), true)));
			out.write(sc.getName() + "\n");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
