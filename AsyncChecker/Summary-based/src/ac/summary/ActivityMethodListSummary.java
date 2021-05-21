package ac.summary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;
import ac.AsyncSummaryMain;
import ac.config.Configuration;
import ac.graph.SlicedBriefUnitGraph;
import ac.graph.SlicedControlFlowGraphManager;
import ac.record.DebugRecorder;
import ac.summary.alphabet.activity.AbstractActivityUnitSummary;
import ac.summary.alphabet.activity.ActivityNullSummary;
import ac.summary.alphabet.activity.CancelAsyncTaskUnitSummary;
import ac.summary.analysis.ActivitySummaryProcessor;
import ac.summary.analysis.ContainAsyncTaskProcessor;
import ac.summary.analysis.ValueSourceProcessor;
import ac.summary.valuesource.AbstractValueSource;
import ac.topology.AbstractTopologyOperation;
import ac.topology.ActivityTopologyOperation;

public class ActivityMethodListSummary extends AbstractMethodSummary {
	
	private int currentBranchNumber = 1;
	
	private List<List<AbstractActivityUnitSummary>> controlFlowSummaryList = new ArrayList<List<AbstractActivityUnitSummary>>();
	
	/**
	 * Record the latest object that is assigned to AsyncTask field
	 */
	private Map<SootField, AssignStmt> fieldToAssignStmt = new HashMap<SootField, AssignStmt>(); 
	
	private Map<String, List<List<AbstractActivityUnitSummary>>> unitToFlowSummaryList = 
			new HashMap<String, List<List<AbstractActivityUnitSummary>>>();

	public ActivityMethodListSummary(SootMethod methodUnderAnalysis) {
		super(methodUnderAnalysis);
	}

	public List<List<AbstractActivityUnitSummary>> getControlFlowSummaryList() {
		return controlFlowSummaryList;
	}

	public void setControlFlowSummaryList(List<List<AbstractActivityUnitSummary>> controlFlowSummaryList) {
		this.controlFlowSummaryList = controlFlowSummaryList;
	}

	public Map<SootField, AssignStmt> getFieldToAssignStmt() {
		return fieldToAssignStmt;
	}

	public void setFieldToAssignStmt(Map<SootField, AssignStmt> fieldToAssignStmt) {
		this.fieldToAssignStmt = fieldToAssignStmt;
	}

	@Override
	public void printMethodSummary() {
		int index = 1;
		for (List<AbstractActivityUnitSummary> flowSummary: this.controlFlowSummaryList) {
			System.out.println(index++ + ".");
			for (int i = 0; i < flowSummary.size(); i++) {
				AbstractActivityUnitSummary unitSummary = flowSummary.get(i);
				SootClass asyncClass = unitSummary.getValueSource().getSourceClass();
				System.out.println(unitSummary.getSummary() + " " + (asyncClass == null ? "null" : asyncClass));
			}
			System.out.println("\n");
		}
	}

	@Override
	protected void generation(UnitGraph theGraph) {
		if (!AsyncSummaryMain.methodToIsContainAsyncOperation.containsKey(this.methodUnderAnalysis)) {
			AsyncSummaryMain.methodToIsContainAsyncOperation.put(this.methodUnderAnalysis, 
					ContainAsyncTaskProcessor.v().isMethodContainAsyncOperation(this.methodUnderAnalysis));
		}
		if (!AsyncSummaryMain.methodToIsContainAsyncOperation.get(this.methodUnderAnalysis)) {
			return;
		}
		
		if (Configuration.DEBUG) {
			DebugRecorder.recordDebugMessage("Current method: " + this.methodUnderAnalysis);
			System.out.println("Current method: " + this.methodUnderAnalysis);
		}
		
		SlicedBriefUnitGraph slicedGraph = SlicedControlFlowGraphManager.generateSlicedGraph(theGraph, this.methodUnderAnalysis);		
		List<Unit> headList = theGraph.getHeads();
		for (Unit currentUnit: headList) {
			Map<Unit, Integer> unitToFreq = new HashMap<Unit, Integer>();
			List<Unit> unitList = new ArrayList<Unit>();
//			List<List<AbstractActivityUnitSummary>> currentSummary = this.traverseUnit(currentUnit, theGraph, unitToFreq, unitList);
			List<List<AbstractActivityUnitSummary>> currentSummary = this.traverseUnit(currentUnit, slicedGraph, unitToFreq, unitList);
			this.summaryListMerge(this.controlFlowSummaryList, currentSummary);
		}
		if (Configuration.DEBUG) {
			DebugRecorder.recordDebugMessage("	summary number: " + this.controlFlowSummaryList.size());
			System.out.println("	summary number: " + this.controlFlowSummaryList.size());
		}
	}
	
	/**
	 * Process the controlFlowSummaryList, put unit summaries that belong to the same async class into a new list.
	 * The orders of unit summaries in the new list are the same as the original ones in the controlFlowSummaryList. 
	 * @return
	 */
	public Map<SootClass, List<List<AbstractActivityUnitSummary>>> generateAsyncClassToFlowSummaryList() {
		Map<SootClass, List<List<AbstractActivityUnitSummary>>> asyncClassToFlowSummaryList = 
				new HashMap<SootClass, List<List<AbstractActivityUnitSummary>>>();
		for (List<AbstractActivityUnitSummary> list: this.controlFlowSummaryList) {
			Set<SootClass> visitedClass = new HashSet<SootClass>();
			for (int i = 0; i < list.size(); i++) {
				AbstractActivityUnitSummary unitSummary = list.get(i);
				if (unitSummary.getValueSource() == null || unitSummary.getValueSource().getSourceClass() == null) {
					continue;
				}
				SootClass asyncClass = unitSummary.getValueSource().getSourceClass();
				if (!visitedClass.contains(asyncClass)) {
					visitedClass.add(asyncClass);
					asyncClassToFlowSummaryList.putIfAbsent(asyncClass, new ArrayList<List<AbstractActivityUnitSummary>>());
					asyncClassToFlowSummaryList.get(asyncClass).add(new ArrayList<AbstractActivityUnitSummary>());
				}
				List<List<AbstractActivityUnitSummary>> summaryList = asyncClassToFlowSummaryList.get(asyncClass);
				summaryList.get(summaryList.size() - 1).add(unitSummary);
			}
		}
		return asyncClassToFlowSummaryList;
	}
	
	/**
	 * 
	 * @param currentStmt, current unit
	 * @param theGraph
	 * @param unitList, current path from the head unit to current unit 
	 * @return
	 */
	protected List<List<AbstractActivityUnitSummary>> processCurrentStmt(Stmt currentStmt, List<Unit> unitList) {
		List<List<AbstractActivityUnitSummary>> resultSummaryList = new ArrayList<List<AbstractActivityUnitSummary>>();
		if (currentStmt.containsInvokeExpr() && currentStmt.getInvokeExpr().getMethod().hasActiveBody()) {
			String key = ActivityTopologyOperation.getActivityOperationKey(currentStmt.getInvokeExpr().getMethod());
			ActivityMethodListSummary invokedMethodSummary = (ActivityMethodListSummary) AbstractTopologyOperation.getsMethodKeyToSummary().get(key);
			if (invokedMethodSummary != null) {
				return this.getUpdateSummary(invokedMethodSummary, currentStmt, unitList);
			}
			else {
				return resultSummaryList;
			}
		}
		else {
			AbstractActivityUnitSummary summary = ActivitySummaryProcessor.v().getUnitSummary(currentStmt, this, unitList);
			if (!(summary instanceof ActivityNullSummary)) {
				resultSummaryList.add(new ArrayList<AbstractActivityUnitSummary>());
				resultSummaryList.get(0).add(summary);
			}
			return resultSummaryList;
		}
	}
	
	/**
	 * Update the ValueSource for summary of invoked method
	 * @param invokedSummary
	 * @param currentStmt
	 * @param unitList
	 * @return
	 */
	private List<List<AbstractActivityUnitSummary>> getUpdateSummary(ActivityMethodListSummary invokedSummary, Stmt currentStmt, List<Unit> unitList) {
		InvokeExpr invokeExpr = currentStmt.getInvokeExpr();
		List<List<AbstractActivityUnitSummary>> newSummaryList = new ArrayList<List<AbstractActivityUnitSummary>>();
		for (List<AbstractActivityUnitSummary> invokedFlowSummary: invokedSummary.controlFlowSummaryList) {
			List<AbstractActivityUnitSummary> newFlowSummary = new ArrayList<AbstractActivityUnitSummary>();
			for (AbstractActivityUnitSummary invokedUnitSummary: invokedFlowSummary) {
				if (invokedUnitSummary.getValueSource() == null) {
					continue;
				}
				AbstractActivityUnitSummary clonedSummary = (AbstractActivityUnitSummary) invokedUnitSummary.clone();
				AbstractValueSource originSource = clonedSummary.getValueSource();
				AbstractValueSource newValueSource = ValueSourceProcessor.v().updateValueSource(originSource, invokeExpr, unitList, this);
				clonedSummary.setValueSource(newValueSource);
				newFlowSummary.add(clonedSummary);
			}
			newSummaryList.add(newFlowSummary);
		}
		return newSummaryList;
	}

	
	/**
	 * Traverse the given unit under DFS, return the list of summary after this unit in sliced-CFG
	 * @param currentUnit
	 * @param node
	 * @param theGraph
	 * @return
	 */
	private List<List<AbstractActivityUnitSummary>> traverseUnit(Unit currentUnit, 
			SlicedBriefUnitGraph theGraph, Map<Unit, Integer> unitToFreq, List<Unit> currentUnitList) {		
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
		
		List<Unit> succeedUnits = new ArrayList<Unit>(theGraph.getSuccsOf(currentUnit));
		if (Configuration.DEBUG && succeedUnits.size() == 0) {
			DebugRecorder.recordDebugMessage("	path index: " + this.currentBranchNumber + ", path length: " + currentUnitList.size());
			System.out.println("	path index: " + this.currentBranchNumber + ", path length: " + currentUnitList.size());
		}
		for (int i = 0; i < succeedUnits.size(); i++) { //Merge the summary of current Unit and its successive unit respectively
			Unit unit = succeedUnits.get(i);
			if (Configuration.RESTRICT_BRANCH && this.currentBranchNumber >= Configuration.MAX_BRANCH_NUMBER && i > 0) {
				break;
			}
			this.currentBranchNumber += i > 0 ? 1 : 0;
			if (unitToFreq.getOrDefault(unit, 0) > 1) {
				if (Configuration.DEBUG) {
					DebugRecorder.recordDebugMessage("	path-cut index: " + this.currentBranchNumber + ", path length: " + currentUnitList.size());
					System.out.println("	path-cut index: " + this.currentBranchNumber + ", path length: " + currentUnitList.size());
				}
				continue;
			}			
			List<List<AbstractActivityUnitSummary>> asyncClassToSummariesAfterUnit = traverseUnit(unit, theGraph, unitToFreq, currentUnitList);
			this.mergeCurrentUnitAndSubsequentUnits(unitFlowSummaryList, asyncClassToSummariesAfterUnit, resultList);
		}
		if (succeedUnits.size() == 0) {
			this.summaryListMerge(resultList, unitFlowSummaryList);
		}
		unitToFreq.put(currentUnit, unitToFreq.get(currentUnit) - 1);
		currentUnitList.remove(currentUnitList.size()-1);
		return resultList;
	}
	
	public void mergeCurrentUnitAndSubsequentUnits(List<List<AbstractActivityUnitSummary>> unitFlowSummaryList, 
			List<List<AbstractActivityUnitSummary>> subsequentFlowSummaryList, List<List<AbstractActivityUnitSummary>> resultList) {
		if (unitFlowSummaryList.size() == 0) {
			this.summaryListMerge(resultList, subsequentFlowSummaryList);
		}
		else if (subsequentFlowSummaryList.size() == 0) {
			this.summaryListMerge(resultList, unitFlowSummaryList);
		}
		else {
			for (List<AbstractActivityUnitSummary> summaryOfUnit: unitFlowSummaryList) {
				for (List<AbstractActivityUnitSummary> summaryAfterUnit: subsequentFlowSummaryList) {
					List<AbstractActivityUnitSummary> newFlowSummary = new ArrayList<AbstractActivityUnitSummary>();
					this.summaryMerge(newFlowSummary, summaryOfUnit);
					this.summaryMerge(newFlowSummary, summaryAfterUnit);
					int insertIndex = this.containCancelOperation(newFlowSummary) ? 0 : resultList.size();
					resultList.add(insertIndex, newFlowSummary);
//					resultList.add(newFlowSummary);
					if (Configuration.RESTRICT_BRANCH && resultList.size() >= Configuration.MAX_VALUE_FLOW_NUMBER) {
						break;
					}
				}
				if (Configuration.RESTRICT_BRANCH && resultList.size() >= Configuration.MAX_VALUE_FLOW_NUMBER) {
					break;
				}
			}
		}
	}
	
	private void summaryListMerge(List<List<AbstractActivityUnitSummary>> mainList, List<List<AbstractActivityUnitSummary>> subList) {
		for (List<AbstractActivityUnitSummary> currentFlowSummary: subList) {
			List<AbstractActivityUnitSummary> newFlowSummary = new ArrayList<AbstractActivityUnitSummary>();
			this.summaryMerge(newFlowSummary, currentFlowSummary);
			// The summary list with cancel operation has higher priority 
			int insertIndex = this.containCancelOperation(newFlowSummary) ? 0 : mainList.size();
			mainList.add(insertIndex, newFlowSummary);
//			mainList.add(newFlowSummary);
			if (Configuration.RESTRICT_BRANCH && mainList.size() >= Configuration.MAX_VALUE_FLOW_NUMBER) {
				break;
			}
		}
	}
	
	private boolean containCancelOperation(List<AbstractActivityUnitSummary> flowSummary) {
		for (AbstractActivityUnitSummary unitSummary: flowSummary) {
			if (unitSummary instanceof CancelAsyncTaskUnitSummary) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Except NullActivitySummary, adding all unit summaries in subSummary to mainSummary
	 * @param mainSummary
	 * @param subSummary
	 */
	private void summaryMerge(List<AbstractActivityUnitSummary> mainSummary,List<AbstractActivityUnitSummary> subSummary){
		for (AbstractActivityUnitSummary unitSummary: subSummary) {
			mainSummary.add(unitSummary);
		}
	}
}
