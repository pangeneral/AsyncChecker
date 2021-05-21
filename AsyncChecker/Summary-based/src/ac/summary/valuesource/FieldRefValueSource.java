package ac.summary.valuesource;

import soot.SootClass;
import soot.jimple.FieldRef;

public class FieldRefValueSource extends AbstractValueSource {

	private FieldRef fieldRef;

	public FieldRef getFieldRef() {
		return fieldRef;
	}

	public void setFieldRef(FieldRef fieldRef) {
		this.fieldRef = fieldRef;
	}

	public FieldRefValueSource(FieldRef fieldRef) {
		this.fieldRef = fieldRef;
	}
	
	@Override
	public ValueSourceType getType() {
		return ValueSourceType.FIELD_REF;
	}

	@Override
	public SootClass getSourceClass() {
		return null;
	}
}
