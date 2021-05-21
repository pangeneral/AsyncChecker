package ac.checker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ac.constant.MisuseType;
import ac.record.AsyncClassCountRecorder;
import ac.record.DefaultAsyncMisuseRecorder;
import ac.summary.ActivityMethodListSummary;
import ac.summary.alphabet.activity.AbstractActivityUnitSummary;
import ac.summary.alphabet.activity.AssignAsyncTaskUnitSummary;
import ac.topology.AbstractTopologyOperation;
import ac.topology.ActivityTopologyOperation;
import ac.util.InheritanceProcess;
import ac.util.Log;
import jymbolic.android.entrypoint.activity.ActivityEntryPointWithoutFragment;
import jymbolic.android.entrypoint.fragment.FragmentEntryPoint;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class SummaryBasedChecker {
	
	public static Map<SootClass, Set<String>> asyncClassToRecordError = new HashMap<SootClass, Set<String>>();
	public static Set<SootClass> checkedClass = new HashSet<SootClass>();
	
	public void detect() {
		for (SootClass currentClass: Scene.v().getApplicationClasses()) {
			// check misused AsyncTask in all Activities
			SootMethod dummyMainMethod = null;
			if (currentClass.isAbstract()) {
				continue;
			}
			if (InheritanceProcess.isInheritedFromActivity(currentClass)) {
				dummyMainMethod = new ActivityEntryPointWithoutFragment(currentClass).getMainMethod();
			}
			else if (InheritanceProcess.isInheritedFromFragment(currentClass)) {
				dummyMainMethod = new FragmentEntryPoint(currentClass).getMainMethod();
			}
			if (dummyMainMethod == null) {
				continue;
			}
			Log.i("Dummy method construction complete");
			AbstractTopologyOperation operation = new ActivityTopologyOperation(dummyMainMethod);
			operation.constructMainSummary();
			
			ActivityMethodListSummary sourceSummary = (ActivityMethodListSummary) operation.getSourceMethodSummary(); 
//					sourceSummary.printMethodSummary();
			this.checkMethodSummaryMisuse(sourceSummary);
		}
	}
	
	public void printCallGraph(CallGraph callGraph) {
		Iterator<Edge> it = callGraph.iterator();
		while (it.hasNext()) {
			Edge currentEdge = it.next();
			String sourceClassArray[] = currentEdge.getSrc().method().getDeclaringClass().getName().split("\\.");
			String targetClassArray[] = currentEdge.getTgt().method().getDeclaringClass().getName().split("\\.");
			String sourceName = currentEdge.getSrc().method().getName();
			String targetName = currentEdge.getTgt().method().getName();
			String sourceMethodName = sourceClassArray[sourceClassArray.length - 1] + "." + sourceName;
			String targetMethodName = targetClassArray[targetClassArray.length - 1] + "." + targetName;
			System.out.println(sourceMethodName + " -> " + targetMethodName);
		}
	}
	
	private void checkMethodSummaryMisuse(ActivityMethodListSummary methodSummary) {
		Map<SootClass, List<List<AbstractActivityUnitSummary>>> asyncClassToFlowSummaryList = methodSummary.generateAsyncClassToFlowSummaryList();
		for (Map.Entry<SootClass, List<List<AbstractActivityUnitSummary>>> entry: asyncClassToFlowSummaryList.entrySet()) {
			SootClass asyncClass = entry.getKey();
			SummaryBasedChecker.asyncClassToRecordError.putIfAbsent(asyncClass, new HashSet<String>());
			List<List<AbstractActivityUnitSummary>> flowSummaryList = entry.getValue();
			this.checkAsyncTaskMisuse(asyncClass, flowSummaryList);
		}
	}
	
	private void checkAsyncTaskMisuse(SootClass asyncClass, List<List<AbstractActivityUnitSummary>> flowSummaryList) {
		Set<String> recordedMisuseType = new HashSet<String>();
		boolean canceled = false;
		for (List<AbstractActivityUnitSummary> currentFlowSummary: flowSummaryList) {
			int beginIndex = 0, size = currentFlowSummary.size();
			AssignAsyncTaskUnitSummary beginAssignUnitSummary = null;
			for (int i = 0; i < size; i++) { // begin the check of control flow summary from AssignAsyncTaskUnitSummary
				if (currentFlowSummary.get(i) instanceof AssignAsyncTaskUnitSummary) {
					beginAssignUnitSummary = (AssignAsyncTaskUnitSummary) currentFlowSummary.get(i);
					beginIndex = i;
					break;
				}
			}
			if (beginAssignUnitSummary == null) {
				continue;
			}
			List<AbstractActivityUnitSummary> subList = currentFlowSummary.subList(beginIndex, size);
			if (ValueFlowChecker.check(subList, asyncClass, beginAssignUnitSummary, recordedMisuseType)) {
				canceled = true;
			}
			if (recordedMisuseType.size() == 4) {
				break;
			}
		}
		if (!SummaryBasedChecker.checkedClass.contains(asyncClass)) {
			AsyncClassCountRecorder.recordCheckedAsyncClass(asyncClass);
			SummaryBasedChecker.checkedClass.add(asyncClass);
		}
		if (!canceled) {
			DefaultAsyncMisuseRecorder recorder = new DefaultAsyncMisuseRecorder();
			recorder.recordAsyncTaskMisuse(asyncClass, MisuseType.NotCancel);
		}
	}
}