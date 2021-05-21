package ac.summary.valuesource;

import soot.SootClass;
import soot.jimple.ParameterRef;

public class ParameterValueSource extends AbstractValueSource {
	
	private int parameterIndex;
	
	private ParameterRef pr;
	
	public int getParameterIndex() {
		return parameterIndex;
	}

	public void setParameterIndex(int parameterIndex) {
		this.parameterIndex = parameterIndex;
	}

	public ParameterRef getPr() {
		return pr;
	}

	public void setPr(ParameterRef pr) {
		this.pr = pr;
	}

	public ParameterValueSource(int index, ParameterRef theRef) {
		this.parameterIndex = index;
		this.pr = theRef;
	}
	
	@Override
	public ValueSourceType getType() {
		return ValueSourceType.PARAMETER;
	}

	@Override
	public SootClass getSourceClass() {
		return null;
	}
}
