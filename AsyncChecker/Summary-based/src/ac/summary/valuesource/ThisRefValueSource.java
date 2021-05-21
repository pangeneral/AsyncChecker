package ac.summary.valuesource;

import soot.SootClass;

public class ThisRefValueSource extends AbstractValueSource {
	
	private SootClass thisClass;
	
	public SootClass getThisClass() {
		return thisClass;
	}

	public void setThisClass(SootClass thisClass) {
		this.thisClass = thisClass;
	}

	public ThisRefValueSource(SootClass theClass) {
		this.thisClass = theClass;
	}
	
	@Override
	public ValueSourceType getType() {
		return ValueSourceType.THIS_REF;
	}

	@Override
	public SootClass getSourceClass() {
		return thisClass;
	}
}
