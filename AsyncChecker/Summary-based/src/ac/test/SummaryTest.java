package ac.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import jymbolic.android.entrypoint.activity.ActivityEntryPointWithoutFragment;

import org.junit.Before;
import org.junit.Test;

import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.toolkits.callgraph.CallGraph;

import com.android.Main;

import ac.config.AsyncCheckerParamProcess;
import ac.graph.CallGraphManager;
import ac.summary.AbstractMethodSummary;
import ac.summary.ActivityMethodListSummary;
import ac.summary.alphabet.activity.AbstractActivityUnitSummary;
import ac.summary.alphabet.activity.AssignAsyncTaskUnitSummary;
import ac.summary.alphabet.activity.CancelAsyncTaskUnitSummary;
import ac.summary.alphabet.activity.StartAsyncTaskUnitSummary;
import ac.topology.AbstractTopologyOperation;
import ac.topology.ActivityTopologyOperation;

public class SummaryTest {
	
	@Before
	public void initSoot() {
		String args[] = new String[] {"-configureFile", "configure\\configuration.txt"};
		Main main = new Main();
		main.getProcessManager().setParamProcess(new AsyncCheckerParamProcess());
		main.sootInit(args);
	}
	
	@Test
	public void testCallGraph() {
		try {
			SootClass theClass = Scene.v().getSootClassUnsafe("de.saschahlusiak.freebloks.game.FreebloksActivity");
			@SuppressWarnings("unused")
			SootMethod method = theClass.getMethodUnsafe("void onCreate(android.os.Bundle)");
			
			SootMethod dummyMainMethod = new ActivityEntryPointWithoutFragment(theClass).getMainMethod();
			CallGraph cg = new CallGraph();
			CallGraphManager manager = new CallGraphManager();
			manager.callGraphConstruction(cg, dummyMainMethod, new HashSet<SootMethod>());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void printSummaryMap(Map<SootClass, List<List<AbstractActivityUnitSummary>>> mainMap) {
		for (Map.Entry<SootClass, List<List<AbstractActivityUnitSummary>>> entry: mainMap.entrySet()) {
			SootClass asyncClass = entry.getKey();
			System.out.println("class " + asyncClass.getName());
			for (List<AbstractActivityUnitSummary> list: entry.getValue()) {
				for (int i = 0; i < list.size(); i++) {
					System.out.print(list.get(i).getSummary());
				}
				System.out.print("\n");
			}
			System.out.println("================================");
		}
	}
	
	private void printSummaryList(List<List<AbstractActivityUnitSummary>> mainList) {
		for (List<AbstractActivityUnitSummary> flowList: mainList) {
			for (int i = 0; i < flowList.size(); i++) {
				System.out.print(flowList.get(i).getSummary());
			}
			System.out.println("\n");
		}
	}
	
	
	private void printGeneratedSummary() {
		Map<String, AbstractMethodSummary> map = AbstractTopologyOperation.getsMethodKeyToSummary();
		for (Map.Entry<String,AbstractMethodSummary> entry: map.entrySet()) {
			ActivityMethodListSummary summary = (ActivityMethodListSummary) entry.getValue();
			System.out.println(summary.getMethodUnderAnalysis().getName());
			for (Map.Entry<SootField, AssignStmt> subentry: summary.getFieldToAssignStmt().entrySet()) {
				System.out.println(subentry.getKey().toString() + " <==== " + subentry.getValue().toString());
			}
			System.out.println("===============");
		}
	}
	
	@Test
	public void testSummary() {
		try {
			SootClass theClass = Scene.v().getSootClassUnsafe("com.example.repeatstart.MainActivity");
			SootMethod method = theClass.getMethodUnsafe("void onCreate(android.os.Bundle)");
			
			SootMethod dummyMainMethod = new ActivityEntryPointWithoutFragment(theClass).getMainMethod();
			AbstractTopologyOperation operation = new ActivityTopologyOperation(dummyMainMethod);
			operation.constructMainSummary();
//			this.printGeneratedSummary();
			ActivityMethodListSummary summary = (ActivityMethodListSummary) operation.getSourceMethodSummary();
			summary.printMethodSummary();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testSummaryMerge() {
		
//		AbstractActivityUnitSummary a1 = null;
//		AbstractActivityUnitSummary a2 = null;
//		AbstractActivityUnitSummary a3 = null;
		
		AbstractActivityUnitSummary a1 = new AssignAsyncTaskUnitSummary();
		AbstractActivityUnitSummary a2 = new AssignAsyncTaskUnitSummary();
		AbstractActivityUnitSummary a3 = new AssignAsyncTaskUnitSummary();
		
		AbstractActivityUnitSummary c1 = new CancelAsyncTaskUnitSummary();
		AbstractActivityUnitSummary c2 = new CancelAsyncTaskUnitSummary();
		AbstractActivityUnitSummary c3 = new CancelAsyncTaskUnitSummary();
		
		AbstractActivityUnitSummary e1 = new StartAsyncTaskUnitSummary();
		AbstractActivityUnitSummary e2 = new StartAsyncTaskUnitSummary();
		AbstractActivityUnitSummary e3 = new StartAsyncTaskUnitSummary();
		
		SootClass A1 = Scene.v().getSootClassUnsafe("com.example.callgraph.A1");
		SootClass B1 = Scene.v().getSootClassUnsafe("com.example.callgraph.B1");
		SootClass C1 = Scene.v().getSootClassUnsafe("com.example.callgraph.C1");
		
//		Map<SootClass, List<List<AbstractActivityUnitSummary>>> mainMap = 
//				new HashMap<SootClass, List<List<AbstractActivityUnitSummary>>>();
//		Map<SootClass, List<List<AbstractActivityUnitSummary>>> firstMap = 
//				new HashMap<SootClass, List<List<AbstractActivityUnitSummary>>>();
//		Map<SootClass, List<List<AbstractActivityUnitSummary>>> secondMap = 
//				new HashMap<SootClass, List<List<AbstractActivityUnitSummary>>>();
//		
//		firstMap.put(A1, new ArrayList<List<AbstractActivityUnitSummary>>());
//		firstMap.put(C1, new ArrayList<List<AbstractActivityUnitSummary>>());
//		
//		secondMap.put(B1, new ArrayList<List<AbstractActivityUnitSummary>>());
//		secondMap.put(C1, new ArrayList<List<AbstractActivityUnitSummary>>());
		List<List<AbstractActivityUnitSummary>> mainList = new ArrayList<List<AbstractActivityUnitSummary>>();
		List<List<AbstractActivityUnitSummary>> firstList = new ArrayList<List<AbstractActivityUnitSummary>>();
		List<List<AbstractActivityUnitSummary>> secondList = new ArrayList<List<AbstractActivityUnitSummary>>();
		
		List<AbstractActivityUnitSummary> l1 = Arrays.asList(a1, a2, a3);
		List<AbstractActivityUnitSummary> l2 = Arrays.asList(c1, c2, c3);
		
		List<AbstractActivityUnitSummary> l3 = Arrays.asList(e1, e2, e3);
		List<AbstractActivityUnitSummary> l4 = Arrays.asList(a1, c1, e1);
		
		List<AbstractActivityUnitSummary> l5 = Arrays.asList(a2, c2, e2);
		List<AbstractActivityUnitSummary> l6 = Arrays.asList(c3, e3, a3);
		
		List<AbstractActivityUnitSummary> l7 = Arrays.asList(a2, c2, e2);
		List<AbstractActivityUnitSummary> l8 = Arrays.asList(e3, a3, c3);
		
		List<AbstractActivityUnitSummary> l9 = Arrays.asList(e3, a3, c3, c1, c2);
		List<AbstractActivityUnitSummary> l10 = Arrays.asList(e3, a3, a3);
		List<AbstractActivityUnitSummary> l11 = Arrays.asList(e3, a3, a3);
		List<AbstractActivityUnitSummary> l12 = Arrays.asList(e3, a3, a3);
		List<AbstractActivityUnitSummary> l13 = Arrays.asList(e3, a3, a3);
		
		mainList.add(l1);
		mainList.add(l2);
		firstList.add(l3);
		firstList.add(l4);
		secondList.add(l5);
		secondList.add(l8);
		secondList.add(l9);
		secondList.add(l10);
		secondList.add(l11);
//		firstMap.get(A1).add(l1);
//		firstMap.get(A1).add(l2);
//		
//		firstMap.get(C1).add(l3);
//		firstMap.get(C1).add(l4);
//		
//		secondMap.get(B1).add(l5);
//		secondMap.get(B1).add(l6);
//		
//		secondMap.get(C1).add(l7);
//		secondMap.get(C1).add(l8);

//		SootMethod method = theClass.getMethodUnsafe("void testSummary()");
		SootClass theClass = Scene.v().getSootClassUnsafe("com.example.asynctasktest.AnotherActivity");
		SootMethod method = theClass.getMethodUnsafe("void onCreate(android.os.Bundle)");
		ActivityMethodListSummary summary = new ActivityMethodListSummary(method);
		summary.mergeCurrentUnitAndSubsequentUnits(firstList, secondList, mainList);
		this.printSummaryList(mainList);
//		ActivityMethodSummary summary = new ActivityMethodSummary(A1.getMethodUnsafe("void entrance()"));
//		summary.mergeCurrentUnitAndSubsequentUnits(firstMap, secondMap, mainMap);
		
//		this.printSummaryMap(mainMap);
//		this.printSummaryMap(firstMap);
//		this.printSummaryMap(secondMap);
	}
}
