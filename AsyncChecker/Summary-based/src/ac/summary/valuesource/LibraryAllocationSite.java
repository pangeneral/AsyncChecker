package ac.summary.valuesource;

import soot.RefType;
import soot.SootClass;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;

public class LibraryAllocationSite extends AbstractValueSource implements InterfaceAllocationSite {
	
	private Unit invokeUnit;
	private InvokeExpr theExpr;
	
	public LibraryAllocationSite(Unit invokeUnit) {
		this.invokeUnit = invokeUnit;
		this.theExpr = ((AssignStmt) invokeUnit).getInvokeExpr();
	}
	
	public Unit getInvokeUnit() {
		return invokeUnit;
	}

	public void setInvokeUnit(Unit invokeUnit) {
		this.invokeUnit = invokeUnit;
	}

	public InvokeExpr getTheExpr() {
		return theExpr;
	}

	public void setTheExpr(InvokeExpr theExpr) {
		this.theExpr = theExpr;
	}

	@Override
	public ValueSourceType getType() {
		return ValueSourceType.LIBRARY;
	}

	@Override
	public Unit getAllocationSite() {
		return this.invokeUnit;
	}

	@Override
	public SootClass getSourceClass() {
		return ((RefType) theExpr.getMethod().getReturnType()).getSootClass();
	}

}
