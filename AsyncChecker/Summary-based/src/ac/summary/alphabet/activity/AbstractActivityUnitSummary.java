/* AsyncDetecotr - an Android async component misuse detection tool
 * Copyright (C) 2018 Linjie Pan
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package ac.summary.alphabet.activity;

import ac.summary.alphabet.AbstractUnitSummary;
import ac.summary.valuesource.AbstractValueSource;
import soot.SootMethod;
import soot.Unit;

/**
 * Base class for any external AsyncTask operation
 * @author Linjie Pan
 * @version 1.0
 */
public abstract class AbstractActivityUnitSummary extends AbstractUnitSummary implements Cloneable {
	
	protected AbstractValueSource valueSource;
	
	public AbstractValueSource getValueSource() {
		return valueSource;
	}

	public void setValueSource(AbstractValueSource allocSite) {
		this.valueSource = allocSite;
	}

	public AbstractActivityUnitSummary() {
		super(null, null);
	}
	
	public AbstractActivityUnitSummary(Unit currentUnit, SootMethod currentMethod, AbstractValueSource allocSite) {
		super(currentUnit, currentMethod);
		this.valueSource = allocSite;
	}
	
	public AbstractActivityUnitSummary(Unit currentUnit, SootMethod currentMethod) {
		super(currentUnit, currentMethod);
	}
	
//	public void setAsyncTaskClass() {
//		if (valueSource instanceof ParameterValueSource || valueSource instanceof FieldRefValueSource) {
//			this.asyncTaskClass = null;
//		}
//		else {
//			this.asyncTaskClass = valueSource.getSourceClass();
//		}
//	}
//
//	public SootClass getAsyncTaskClass() {
//		return asyncTaskClass;
//	}
	
	@Override
	public Object clone() {
		return this.clone();
	}
}
