package ac.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.android.Main;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import ac.config.AsyncCheckerParamProcess;
import ac.config.Configuration;
import ac.graph.SlicedBriefUnitGraph;
import ac.graph.SlicedControlFlowGraphManager;
import ac.summary.alphabet.activity.AbstractActivityUnitSummary;
import ac.summary.analysis.ContainAsyncTaskProcessor;

class Unit {
	int val;
	public Unit() {
		this.val = Graph.count++;
	}
}

class Graph {
	public static int currentBranchNumber = 1;
	public static int count = 0;
	List<Unit> headList = new ArrayList<Unit>();
	List<Unit> unitList = new ArrayList<Unit>();
	Set<Unit> unitSet = new HashSet<Unit>();
	Map<Unit, Set<Unit>> unitToSuccs = new HashMap<Unit, Set<Unit>>();
//	Map<Unit, List<Unit>> unitToSuccs = new HashMap<Unit, List<Unit>>();
	
	public Set<Unit> getSuccsOf(Unit unit) {
		return this.unitToSuccs.getOrDefault(unit, new HashSet<Unit>());
	}
}

public class SliceGraphTest {
	
	@Before
	public void initSoot() {
		String args[] = new String[] {"-configureFile", "configure\\configuration.txt"};
		Main main = new Main();
		main.getProcessManager().setParamProcess(new AsyncCheckerParamProcess());
		main.sootInit(args);
	}
	
	@Test
	public void testSlicedUnitGraph() {
		SootClass theClass = Scene.v().getSootClassUnsafe("com.example.asynctasktest.DialogActivity$1");
		SootMethod method = theClass.getMethodUnsafe("void startAsyncTask()");
		
		System.out.println(method.getActiveBody().toString());
		
		SlicedControlFlowGraphManager.generateSlicedGraph(new BriefUnitGraph(method.getActiveBody()), method);
	}
	
	/**
	 * Generate a sliced control flow graph that only contains AsyncTask-related statements.
	 * All head units are kept in the sliced graph. 
	 * @param originalGraph
	 * @return
	 */
	public void generateSlicedGraph(Graph originalGraph, Graph slicedGraph) {
		for (Unit keyUnit: slicedGraph.unitList) {
			dfs(slicedGraph, keyUnit, keyUnit, originalGraph, new HashSet<Unit>());
		}
	}
	
	private void dfs(Graph slicedGraph, Unit keyUnit, Unit currentUnit, Graph originalGraph, Set<Unit> visitedUnit) {
		if (!visitedUnit.contains(currentUnit)) {
			visitedUnit.add(currentUnit);
			Set<Unit> unitList = originalGraph.getSuccsOf(currentUnit);
			for (Unit nextUnit: unitList) {
				if (slicedGraph.unitSet.contains(nextUnit)) {
					slicedGraph.unitToSuccs.putIfAbsent(keyUnit, new HashSet<Unit>());
					slicedGraph.unitToSuccs.get(keyUnit).add(nextUnit);
				}
				else {
					dfs(slicedGraph, keyUnit, nextUnit, originalGraph, visitedUnit);
				}
			}
		}
	}
	
	private void addEdge(Graph graph, Unit unitArray[], int i, int j) {
		graph.unitToSuccs.get(unitArray[i]).add(unitArray[j]);
	}
	
	private void traverseUnit(Unit currentUnit, Graph theGraph, Map<Unit, Integer> unitToFreq, List<Unit> currentUnitList) {
		unitToFreq.put(currentUnit, unitToFreq.getOrDefault(currentUnit, 0) + 1);
		currentUnitList.add(currentUnit);
		
		List<Unit> succeedUnits = new ArrayList<Unit>(theGraph.getSuccsOf(currentUnit));
		if (succeedUnits.size() == 0) {
			System.out.println("	path index: " + Graph.currentBranchNumber + ", path length: " + currentUnitList.size());
		}
		for (int i = 0; i < succeedUnits.size(); i++) { //Merge the summary of current Unit and its successive unit respectively
			Unit unit = succeedUnits.get(i);
			if (unitToFreq.getOrDefault(unit, 0) > 1) {
				System.out.println("	path-cut index: " + Graph.currentBranchNumber + ", path length: " + currentUnitList.size());
				continue;
			}
			if (Graph.currentBranchNumber >= Configuration.MAX_BRANCH_NUMBER && i > 0) {
				break;
			}
			Graph.currentBranchNumber += i > 0 ? 1 : 0;
			this.traverseUnit(unit, theGraph, unitToFreq, currentUnitList);
		}
		unitToFreq.put(currentUnit, unitToFreq.get(currentUnit) - 1);
		currentUnitList.remove(currentUnitList.size()-1);
	}
	
	@Test
	public void testGraph() {
		Graph theGraph = new Graph();
		Graph slicedGraph = new Graph();
		Graph originalGraph = new Graph();
		Unit unitArray[] = new Unit[]{new Unit(), new Unit(),new Unit(),new Unit(),new Unit(),new Unit(),new Unit(),new Unit(),new Unit(), new Unit(), new Unit()};
		theGraph.headList.add(unitArray[1]);
		
		for (int i = 1; i <= 7; i++) {
			theGraph.unitList.add(unitArray[i]);
			theGraph.unitToSuccs.putIfAbsent(unitArray[i], new HashSet<Unit>());
		}
		this.addEdge(theGraph, unitArray, 1, 2);
		this.addEdge(theGraph, unitArray, 1, 7);
		this.addEdge(theGraph, unitArray, 2, 3);
		this.addEdge(theGraph, unitArray, 3, 3);
		this.addEdge(theGraph, unitArray, 3, 4);
		this.addEdge(theGraph, unitArray, 4, 3);
		this.addEdge(theGraph, unitArray, 4, 5);
		this.addEdge(theGraph, unitArray, 5, 2);
		this.addEdge(theGraph, unitArray, 5, 6);
		
		this.traverseUnit(unitArray[1], theGraph, new HashMap<Unit, Integer>(), new ArrayList<Unit>());
		
//		for (int i = 1; i < unitArray.length; i++) {
//			originalGraph.unitList.add(unitArray[i]);
//			originalGraph.unitToSuccs.putIfAbsent(unitArray[i], new HashSet<Unit>());
//		}
//		
//		slicedGraph.unitList.add(unitArray[4]);
//		slicedGraph.unitList.add(unitArray[5]);
//		slicedGraph.unitList.add(unitArray[8]);
//		slicedGraph.unitSet.add(unitArray[4]);
//		slicedGraph.unitSet.add(unitArray[5]);
//		slicedGraph.unitSet.add(unitArray[8]);
//		
//		this.addEdge(originalGraph, unitArray, 1, 2);
//		this.addEdge(originalGraph, unitArray, 1, 3);
//		this.addEdge(originalGraph, unitArray, 2, 4);
//		this.addEdge(originalGraph, unitArray, 3, 4);
//		this.addEdge(originalGraph, unitArray, 4, 5);
//		this.addEdge(originalGraph, unitArray, 4, 6);
//		this.addEdge(originalGraph, unitArray, 5, 7);
//		this.addEdge(originalGraph, unitArray, 5, 9);
//		this.addEdge(originalGraph, unitArray, 6, 5);
//		this.addEdge(originalGraph, unitArray, 6, 8);
//		this.addEdge(originalGraph, unitArray, 6, 10);
//		this.addEdge(originalGraph, unitArray, 7, 2);
//		this.addEdge(originalGraph, unitArray, 8, 1);
//		this.addEdge(originalGraph, unitArray, 9, 6);
//		
//		this.generateSlicedGraph(originalGraph, slicedGraph);
//		for (int i = 0; i < slicedGraph.unitList.size(); i++) {
//			Set<Unit> targetNodeList = slicedGraph.unitToSuccs.getOrDefault(slicedGraph.unitList.get(i), new HashSet<Unit>());
//			for (Unit targetUnit: targetNodeList) {
//				System.out.println(slicedGraph.unitList.get(i).val + " -> " + targetUnit.val);
//			}
//		}
	}
	
	
}


