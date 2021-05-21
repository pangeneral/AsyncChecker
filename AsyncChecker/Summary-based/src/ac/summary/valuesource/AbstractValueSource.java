package ac.summary.valuesource;

import soot.SootClass;

/**
 * The source of a local variable
 * @author Pan Linjie
 *
 */
public abstract class AbstractValueSource {
	public abstract ValueSourceType getType();
	
	public abstract SootClass getSourceClass();
	
	public Object clone() {
		return this.clone();
	}
}
