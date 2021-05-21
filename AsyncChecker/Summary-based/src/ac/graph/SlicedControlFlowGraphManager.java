package ac.graph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ac.config.Configuration;
import ac.record.DebugRecorder;
import ac.summary.analysis.ContainAsyncTaskProcessor;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;

public class SlicedControlFlowGraphManager {
	
	/**
	 * Generate a sliced control flow graph that only contains AsyncTask-related statements.
	 * All head units are kept in the sliced graph. 
	 * @param originalGraph
	 * @return
	 */
	public static SlicedBriefUnitGraph generateSlicedGraph(UnitGraph originalGraph, SootMethod currentMethod) {
		if (Configuration.DEBUG) {
			DebugRecorder.recordDebugMessage("===================================================");
			DebugRecorder.recordDebugMessage("Method under slice: " + currentMethod.getSignature());
			System.out.println("===================================================");
			System.out.println("Method under slice: " + currentMethod.getSignature());
		}
		List<Unit> headList = originalGraph.getHeads();
		SlicedBriefUnitGraph slicedGraph = new SlicedBriefUnitGraph();
		
		for (int i = 0; i < headList.size(); i++) { // generate head node 
			slicedGraph.getHeadUnit().add(headList.get(i));
			slicedGraph.getUnitChain().add(headList.get(i));
		}
		
		Iterator<Unit> it = originalGraph.iterator();
		while (it.hasNext()) { // generate node of sliced graph
			Unit currentUnit = it.next();
			Set<SootMethod> visitedMethod = new HashSet<SootMethod>();
			visitedMethod.add(currentMethod);
			if (ContainAsyncTaskProcessor.v().isUnitContainAsyncTaskOperation(currentUnit, visitedMethod)) {
				if (Configuration.DEBUG) {
					DebugRecorder.recordDebugMessage(currentUnit.toString());
					System.out.println(currentUnit.toString());
				}
				slicedGraph.getUnitChain().add(currentUnit);
			}
		}
		
		for (Unit keyUnit: slicedGraph.getUnitChain()) {
			dfs(slicedGraph, keyUnit, keyUnit, originalGraph, new HashSet<Unit>());
		}
		
		if (Configuration.DEBUG) {
			DebugRecorder.recordDebugMessage("slice end");
			DebugRecorder.recordDebugMessage("=========================================");
			System.out.println("slice end");
			System.out.println("=========================================");
		}
		
		return slicedGraph;
	}
	
	private static void dfs(SlicedBriefUnitGraph slicedGraph, Unit keyUnit, Unit currentUnit, UnitGraph originalGraph, Set<Unit> visitedUnit) {
		if (!visitedUnit.contains(currentUnit)) {
			visitedUnit.add(currentUnit);
			List<Unit> unitList = originalGraph.getSuccsOf(currentUnit);
			for (Unit nextUnit: unitList) {
				if (slicedGraph.getUnitChain().contains(nextUnit)) {
					slicedGraph.getUnitToSuccs().putIfAbsent(keyUnit, new HashSet<Unit>());
					slicedGraph.getUnitToSuccs().get(keyUnit).add(nextUnit);
				}
				else {
					dfs(slicedGraph, keyUnit, nextUnit, originalGraph, visitedUnit);
				}
			}
		}
	}
}
