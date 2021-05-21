package ac.summary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ac.summary.analysis.ValueSourceProcessor;
import ac.summary.valuesource.AbstractValueSource;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.ReturnStmt;
import soot.toolkits.graph.UnitGraph;

public class ReturnValueMethodSummary extends AbstractMethodSummary {
	
	private AbstractValueSource sourceOfReturnValue;
	
	public AbstractValueSource getSourceOfReturnValue() {
		return sourceOfReturnValue;
	}

	public void setSourceOfReturnValue(AbstractValueSource sourceOfReturnValue) {
		this.sourceOfReturnValue = sourceOfReturnValue;
	}

	public ReturnValueMethodSummary(SootMethod methodUnderAnalysis) {
		super(methodUnderAnalysis);
	}

	@Override
	public void printMethodSummary() {
		
	}

	@Override
	protected void generation(UnitGraph theGraph) {
		List<Unit> tailUnitList = theGraph.getTails();
		for (int i = 0; i < tailUnitList.size(); i++) {
			Unit currentUnit = tailUnitList.get(i);
			if (currentUnit instanceof ReturnStmt) {
				List<Unit> headToTail = new ArrayList<Unit>();
				this.generateHeadToTail(theGraph, currentUnit, headToTail, new HashSet<Unit>());
				this.sourceOfReturnValue = ValueSourceProcessor.v().findValueSourceOfReturnValue(methodUnderAnalysis, ((ReturnStmt) currentUnit).getOp(), headToTail);
			}
		}
		
	}
	
	private void generateHeadToTail(UnitGraph theGraph, Unit currentUnit, List<Unit> resultList, Set<Unit> visitedSet) {
		resultList.add(0, currentUnit);
		visitedSet.add(currentUnit);
		List<Unit> prevUnitList = theGraph.getPredsOf(currentUnit);
		if (prevUnitList.size() > 0 && !visitedSet.contains(prevUnitList.get(0))) {
			this.generateHeadToTail(theGraph, prevUnitList.get(0), resultList, visitedSet);
		}
	}

}
