package ac.summary.valuesource;

import soot.SootClass;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.NewExpr;

/**
 * 
 * @author Pan Linjie
 *
 */
public class ExplicitAllocationSite extends AbstractValueSource implements InterfaceAllocationSite {
	private SootClass initClass;
	private AssignStmt initStmt;
	
	public SootClass getInitClass() {
		return initClass;
	}

	public void setInitClass(SootClass initClass) {
		this.initClass = initClass;
	}

	public ExplicitAllocationSite(AssignStmt initStmt) {
		this.initStmt = initStmt;
		this.initClass = ((NewExpr) initStmt.getRightOp()).getBaseType().getSootClass();
	}

	@Override
	public ValueSourceType getType() {
		return ValueSourceType.INIT;
	}

	@Override
	public Unit getAllocationSite() {
		return this.initStmt;
	}

	@Override
	public SootClass getSourceClass() {
		return this.initClass;
	}
}
