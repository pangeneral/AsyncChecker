package ac.graph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import ac.util.PolymorphismProcess;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NewExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class CallGraphManager {
	
	/**
	 * Record all instantiated classes in the APK
	 */
	private static Set<SootClass> allInstantiationClassSet = new HashSet<SootClass>();
	
	public static Set<SootClass> getAllInstantiationClassSet() {
		return CallGraphManager.allInstantiationClassSet;
	}
	
	/**
	 *	Traverse all methods with active body and record classes which are instantiated
	 */
	public static void initAllInstantiationClassSet() {
		// traverse all methods
		for (SootClass currentClass: Scene.v().getApplicationClasses()) {
			for (SootMethod currentMethod: currentClass.getMethods()) {
				if (currentMethod.hasActiveBody()) {
					UnitGraph theGraph = new BriefUnitGraph(currentMethod.getActiveBody());
					Iterator<Unit> it = theGraph.iterator();
					while (it.hasNext()) {
						Stmt currentStmt = (Stmt) it.next();
						if (currentStmt instanceof AssignStmt && ((AssignStmt) currentStmt).getRightOp() instanceof NewExpr) {
							SootClass instClass = ((NewExpr) ((AssignStmt) currentStmt).getRightOp()).getBaseType().getSootClass();
							CallGraphManager.getAllInstantiationClassSet().add(instClass); // record instantiated class
						}
					}
				}
			}
		}
	}
	
	/**
	 * Analyze the invoked method in InvokeExpr
	 * @param expr
	 * @param currentUnit
	 * @param currentMethod
	 * @return
	 */
	public SootMethod getPossibleInvokedMethod(InvokeExpr expr, Unit currentUnit, SootMethod currentMethod) {
		if (expr instanceof StaticInvokeExpr) {
			return expr.getMethod();
		}
		else if (expr instanceof InstanceInvokeExpr) {
			Type baseType = ((InstanceInvokeExpr) expr).getBase().getType();
			if (baseType instanceof RefType) {
				SootMethod invokedMethod = PolymorphismProcess.getInterfaceOrVirtualInvokedMethod(((RefType) baseType).getSootClass(), expr.getMethod().getSubSignature());
				return invokedMethod != null ? invokedMethod : expr.getMethod();
			}
			else {
				return expr.getMethod();
			}
		}
		else {
			return expr.getMethod();
		}
	}
	
	public void callGraphConstruction(CallGraph callGraph, SootMethod currentMethod, Set<SootMethod> visitedMethod) {
		if (visitedMethod.contains(currentMethod)) {
			return;
		}
		visitedMethod.add(currentMethod);
		UnitGraph theGraph = new BriefUnitGraph(currentMethod.getActiveBody());
		Iterator<Unit> it = theGraph.iterator();
		
//		try {
//			FileWriter writer = new FileWriter("edge.txt", true);
//			FileWriter allStmtWriter = new FileWriter("all-stmt.txt", true);
//			FileWriter stmtWriter = new FileWriter("stmt.txt", true); 
		while (it.hasNext()) {
			Stmt currentStmt = (Stmt)it.next();
//				allStmtWriter.write(currentStmt.toString() + "; method: " + currentMethod.getSignature() + "\n");
			if (currentStmt.containsInvokeExpr()) {
//					stmtWriter.write("before " + currentStmt.toString() + "\n");
				SootMethod invokedMethod = getPossibleInvokedMethod(currentStmt.getInvokeExpr(), currentStmt, currentMethod);
//					stmtWriter.write("after " + currentStmt.toString() + " " + currentMethod.getSignature() + "\n");
				if (!invokedMethod.equals(currentMethod) && invokedMethod.hasActiveBody()) {
					callGraph.addEdge(new Edge(currentMethod, currentStmt, invokedMethod));
//						writer.write(currentMethod.getSignature() + " -> " + invokedMethod.getSignature() + "\n");
					
					this.callGraphConstruction(callGraph, invokedMethod, visitedMethod);
				}
			}
		}
//			stmtWriter.close();
//			allStmtWriter.close();
//			writer.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		
	}
	
	/**
	 * Construct a virtual call graph for test
	 * @param graph
	 */
	public void constructForTest(CallGraph graph, SootMethod sourceMethod) {
		SootMethod b = new SootMethod("b", null, VoidType.v());
		SootMethod c = new SootMethod("c", null, VoidType.v());
		SootMethod d = new SootMethod("d", null, VoidType.v());
		SootMethod e = new SootMethod("e", null, VoidType.v());
		SootMethod f = new SootMethod("f", null, VoidType.v());
		SootMethod g = new SootMethod("g", null, VoidType.v());		
		graph.addEdge(new Edge(sourceMethod, Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(sourceMethod.makeRef())), b));
		graph.addEdge(new Edge(sourceMethod, Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(sourceMethod.makeRef())), c));
		graph.addEdge(new Edge(b, Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(sourceMethod.makeRef())), d));
		graph.addEdge(new Edge(c, Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(sourceMethod.makeRef())), d));
		graph.addEdge(new Edge(d, Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(sourceMethod.makeRef())), e));
		graph.addEdge(new Edge(d, Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(sourceMethod.makeRef())), f));
		graph.addEdge(new Edge(e, Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(sourceMethod.makeRef())), b));
		graph.addEdge(new Edge(f, Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(sourceMethod.makeRef())), sourceMethod));
		graph.addEdge(new Edge(e, Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(sourceMethod.makeRef())), g));
		graph.addEdge(new Edge(f, Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(sourceMethod.makeRef())), g));
		
	}
	
	
}
