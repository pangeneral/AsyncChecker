package ac.test;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import ac.checker.SummaryBasedChecker;
import ac.config.AsyncCheckerParamProcess;
import ac.graph.CallGraphManager;
import ac.topology.AbstractTopologyOperation;
import ac.topology.ActivityTopologyOperation;
import ac.topology.TopologyNode;

import com.android.Main;

public class AsyncTaskDetectorTest {
	
	private SootMethod testMethod = null;
	private CallGraph callGraph = null;
	
	@Before
	public void initSoot() {
		String args[] = new String[] {"-configureFile", "configure\\configuration.txt"};
		Main main = new Main();
		main.getProcessManager().setParamProcess(new AsyncCheckerParamProcess());
		main.sootInit(args);
	}
	
	@Test
	public void testCallGraph() {
		String className = "com.example.callgraphtest.circle.CircleTest";
		String methodName = "A";
		SootClass testClient = Scene.v().getSootClassUnsafe(className);
		this.callGraph = new CallGraph();
		SummaryBasedChecker detector = new SummaryBasedChecker();
		CallGraphManager manager = new CallGraphManager();
		this.testMethod = testClient.getMethodByNameUnsafe(methodName);
		manager.callGraphConstruction(callGraph, testMethod, new HashSet<SootMethod>());
		detector.printCallGraph(callGraph);
	}
	
	@Test
	public void testTopologyGraph() {
		testCallGraph();
//		CallGraph callGraph = new CallGraph();
//		CallGraphManager manager = new CallGraphManager();
//		SootMethod testMethod = new SootMethod("a", null, VoidType.v());
//		manager.constructForTest(callGraph, testMethod);
		
		AbstractTopologyOperation topologyOperation = new ActivityTopologyOperation(testMethod);
		TopologyNode sourceNode = new TopologyNode(testMethod,null);
		topologyOperation.getMethodToNode().put(testMethod, sourceNode);
//		this.mMethodToNode = new HashMap<SootMethod,TopologyNode>();
//		this.mMethodToNode.put(mSourceMethod, sourceNode);
		topologyOperation.constructTopologyGraph(callGraph, testMethod);
		topologyOperation.printTopologyGraph();
	}
}
