package ac.summary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ac.config.Configuration;
import ac.summary.alphabet.activity.AbstractActivityUnitSummary;
import ac.topology.AbstractTopologyOperation;
import ac.topology.ActivityTopologyOperation;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;

public class ActivityMethodSummary extends AbstractMethodSummary {

	private Map<SootClass, List<List<AbstractActivityUnitSummary>>> asyncClassToControlFlowSummaryList = 
			new HashMap<SootClass, List<List<AbstractActivityUnitSummary>>>();
	
	private Map<String, Map<SootClass, List<List<AbstractActivityUnitSummary>>>> unitToAsyncSummaryList = 
			new HashMap<String, Map<SootClass, List<List<AbstractActivityUnitSummary>>>>();

	public Map<SootClass, List<List<AbstractActivityUnitSummary>>> getAsyncClassToControlFlowSummaryList() {
		return asyncClassToControlFlowSummaryList;
	}

	public void setAsyncClassToControlFlowSummaryList(
			Map<SootClass, List<List<AbstractActivityUnitSummary>>> asyncClassToControlFlowSummaryList) {
		this.asyncClassToControlFlowSummaryList = asyncClassToControlFlowSummaryList;
	}

	public Map<String, Map<SootClass, List<List<AbstractActivityUnitSummary>>>> getUnitToAsyncSummaryList() {
		return unitToAsyncSummaryList;
	}

	public void setUnitToAsyncSummaryList(
			Map<String, Map<SootClass, List<List<AbstractActivityUnitSummary>>>> unitToAsyncSummaryList) {
		this.unitToAsyncSummaryList = unitToAsyncSummaryList;
	}

	public ActivityMethodSummary(SootMethod methodUnderAnalysis) {
		super(methodUnderAnalysis);
	}

	@Override
	public void printMethodSummary() {
		
	}

	@Override
	protected void generation(UnitGraph theGraph) {
		List<Unit> headList = theGraph.getHeads();
		for(Unit currentUnit: headList){
			Map<SootClass, List<List<AbstractActivityUnitSummary>>> currentSummary = 
					this.traverseUnit(currentUnit, theGraph, new HashSet<Unit>(), new ArrayList<Unit>());
			for (Map.Entry<SootClass, List<List<AbstractActivityUnitSummary>>> entry: currentSummary.entrySet()) {
				this.asyncClassToControlFlowSummaryList.putIfAbsent(entry.getKey(), new ArrayList<List<AbstractActivityUnitSummary>>());
				this.asyncClassToControlFlowSummaryList.get(entry.getKey()).addAll(entry.getValue());
			}
		}
	}
	
	/**
	 * 
	 * @param currentStmt, current unit
	 * @param theGraph
	 * @param unitList, current path from the head unit to current unit 
	 * @return
	 */
	protected Map<SootClass, List<List<AbstractActivityUnitSummary>>> processCurrentStmt(Stmt currentStmt, List<Unit> unitList) {
		Map<SootClass, List<List<AbstractActivityUnitSummary>>> resultSummaryList = 
				new HashMap<SootClass, List<List<AbstractActivityUnitSummary>>>();
		if (currentStmt.containsInvokeExpr() && currentStmt.getInvokeExpr().getMethod().hasActiveBody()) {
			String key = ActivityTopologyOperation.getActivityOperationKey(currentStmt.getInvokeExpr().getMethod());
			ActivityMethodSummary invokedMethodSummary = (ActivityMethodSummary) AbstractTopologyOperation.getsMethodKeyToSummary().get(key);
			return invokedMethodSummary.asyncClassToControlFlowSummaryList;
		}
		else {
//			AbstractActivityUnitSummary summary = ActivitySummaryProcessor.v().getUnitSummary(currentStmt, this, unitList);
//			if (!(summary instanceof ActivityNullSummary)) {
//				SootClass theClass = summary.getValueSource().getSourceClass();
//				if (theClass != null) {
//					resultSummaryList.putIfAbsent(theClass, new ArrayList<List<AbstractActivityUnitSummary>>());
//					resultSummaryList.get(theClass).add(new ArrayList<AbstractActivityUnitSummary>());
//					resultSummaryList.get(theClass).get(0).add(summary);
//				}
//			}
			return resultSummaryList;
		}
	}
	
	/**
	 * Traverse the given unit with DFS, return the list of summary after this unit in CFG
	 * @param currentUnit
	 * @param node
	 * @param theGraph
	 * @return
	 */
	private Map<SootClass, List<List<AbstractActivityUnitSummary>>> traverseUnit(Unit currentUnit, 
			UnitGraph theGraph, Set<Unit> visitedUnit, List<Unit> currentUnitList){
		assert(currentUnit.branches()||(!currentUnit.branches()&&theGraph.getSuccsOf(currentUnit).size()<=1));

		visitedUnit.add(currentUnit);
		currentUnitList.add(currentUnit);
		Map<SootClass, List<List<AbstractActivityUnitSummary>>> resultList = new HashMap<SootClass, List<List<AbstractActivityUnitSummary>>>();
		Map<SootClass, List<List<AbstractActivityUnitSummary>>> asyncClassToUnitSummaries = null;
		/**
		 * A unit and a control flow correspond to a unique unit summary
		 */
		String hashKey = String.valueOf(currentUnit.hashCode())+"-"+String.valueOf(currentUnitList.hashCode());
		if (this.unitToAsyncSummaryList.containsKey(hashKey)) {
			asyncClassToUnitSummaries = this.unitToAsyncSummaryList.get(hashKey);
		}
		else {
			asyncClassToUnitSummaries = this.processCurrentStmt((Stmt) currentUnit, currentUnitList);
			this.unitToAsyncSummaryList.put(hashKey, asyncClassToUnitSummaries);
		}
		List<Unit> succeedUnits = theGraph.getSuccsOf(currentUnit);
		for(Unit unit: succeedUnits){ //Merge the summary of current Unit and its successive unit respectively
			if (visitedUnit.contains(unit)) {
				continue;
			}
			Map<SootClass, List<List<AbstractActivityUnitSummary>>> asyncClassToSummariesAfterUnit = traverseUnit(unit, theGraph, visitedUnit, currentUnitList);
			this.mergeCurrentUnitAndSubsequentUnits(asyncClassToUnitSummaries, asyncClassToSummariesAfterUnit, resultList);
		}
		visitedUnit.remove(currentUnit);
		currentUnitList.remove(currentUnitList.size()-1);
		return resultList;
	}
	
	/**
	 * Traverse the given unit with DFS, return the list of summary after this unit in CFG
	 * @param currentUnit
	 * @param node
	 * @param theGraph
	 * @return
	 */
	private List<List<AbstractActivityUnitSummary>> traverseUnit(Unit currentUnit, UnitGraph theGraph, 
			Map<Unit, Integer> unitToFreq, List<Unit> currentUnitList) {
		unitToFreq.put(currentUnit, unitToFreq.getOrDefault(currentUnit, 0) + 1);
		currentUnitList.add(currentUnit);
		List<List<AbstractActivityUnitSummary>> resultList = new ArrayList<List<AbstractActivityUnitSummary>>();
		
		List<List<AbstractActivityUnitSummary>> unitFlowSummaryList = null;
		/**
		 * A unit and a control flow correspond to a unique unit summary
		 */
		String hashKey = String.valueOf(currentUnit.hashCode())+"-"+String.valueOf(currentUnitList.hashCode());
		if (this.unitToFlowSummaryList.containsKey(hashKey)) {
			unitFlowSummaryList = this.unitToFlowSummaryList.get(hashKey);
		}
		else {
			unitFlowSummaryList = this.processCurrentStmt((Stmt) currentUnit, currentUnitList);
			this.unitToFlowSummaryList.put(hashKey, unitFlowSummaryList);
		}
		
		List<Unit> succeedUnits = theGraph.getSuccsOf(currentUnit);
		for (int i = 0; i < succeedUnits.size(); i++) { //Merge the summary of current Unit and its successive unit respectively
			Unit unit = succeedUnits.get(i);
			if (this.currentBranchNumber >= Configuration.MAX_BRANCH_NUMBER && i > 0) {
				break;
			}
			this.currentBranchNumber += i > 0 ? 1 : 0;
			if (unitToFreq.getOrDefault(unit, 0) > 1) {
				if (Configuration.DEBUG) {
					System.out.println("	path-cut index: " + this.currentBranchNumber + ", path length: " + currentUnitList.size());
				}
				continue;
			}			
			List<List<AbstractActivityUnitSummary>> asyncClassToSummariesAfterUnit = traverseUnit(unit, theGraph, unitToFreq, currentUnitList);
			this.mergeCurrentUnitAndSubsequentUnits(unitFlowSummaryList, asyncClassToSummariesAfterUnit, resultList);
		}
		if (Configuration.DEBUG && succeedUnits.size() == 0) {
			System.out.println("	path index: " + this.currentBranchNumber + ", path length: " + currentUnitList.size());
		}
		unitToFreq.put(currentUnit, unitToFreq.get(currentUnit) - 1);
		currentUnitList.remove(currentUnitList.size()-1);
		return resultList;
	}
	
	public void mergeCurrentUnitAndSubsequentUnits(Map<SootClass, List<List<AbstractActivityUnitSummary>>> unitSummaryMap, 
				Map<SootClass, List<List<AbstractActivityUnitSummary>>> subsequentSummaryMap, 
				Map<SootClass, List<List<AbstractActivityUnitSummary>>> resultMap) {
		for (Map.Entry<SootClass, List<List<AbstractActivityUnitSummary>>> entry: unitSummaryMap.entrySet()) {
			SootClass asyncClass = entry.getKey();
			List<List<AbstractActivityUnitSummary>> classSummaryOfUnit = entry.getValue();
			if (subsequentSummaryMap.containsKey(asyncClass)) { // async class appears in both current unit and subsequent unit
				List<List<AbstractActivityUnitSummary>> classSummariesAfterUnit = subsequentSummaryMap.get(asyncClass);
				for(List<AbstractActivityUnitSummary> summaryAfterUnit: classSummariesAfterUnit) {
					for(List<AbstractActivityUnitSummary> summaryOfUnit: classSummaryOfUnit) {
						List<AbstractActivityUnitSummary> newSummary = new ArrayList<AbstractActivityUnitSummary>();
						this.summaryMerge(newSummary, summaryOfUnit);
						this.summaryMerge(newSummary, summaryAfterUnit);
						resultMap.putIfAbsent(entry.getKey(), new ArrayList<List<AbstractActivityUnitSummary>>());
						resultMap.get(entry.getKey()).add(newSummary);
					}
				}
			}
			else { // async class appears only in current unit
				this.addSummaryToResult(resultMap, classSummaryOfUnit, asyncClass);
			}
		}
		for (Map.Entry<SootClass, List<List<AbstractActivityUnitSummary>>> entry: subsequentSummaryMap.entrySet()) {
			if (!unitSummaryMap.containsKey(entry.getKey())) { // async class appears only in subsequent unit
				this.addSummaryToResult(resultMap, entry.getValue(), entry.getKey());
			}
		}
	}
	
	private void addSummaryToResult(Map<SootClass, List<List<AbstractActivityUnitSummary>>> resultMap, 
			List<List<AbstractActivityUnitSummary>> summaryList, SootClass asyncClass) {
		for (List<AbstractActivityUnitSummary> summaryAfterUnit: summaryList) {
			List<AbstractActivityUnitSummary> newSummary = new ArrayList<AbstractActivityUnitSummary>();
			this.summaryMerge(newSummary, summaryAfterUnit);
			resultMap.putIfAbsent(asyncClass, new ArrayList<List<AbstractActivityUnitSummary>>());
			resultMap.get(asyncClass).add(newSummary);
		}
	}
	
	/**
	 * Except NullActivitySummary, adding all unit summaries in subSummary to mainSummary
	 * @param mainSummary
	 * @param subSummary
	 */
	private void summaryMerge(List<AbstractActivityUnitSummary> mainSummary,List<AbstractActivityUnitSummary> subSummary){
		for(AbstractActivityUnitSummary unitSummary:subSummary){
			mainSummary.add(unitSummary);
		}
	}
}
