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
import ac.constant.MethodSignature;
import ac.summary.DoInBackgroundMethodSummary;
import ac.summary.valuesource.ExplicitAllocationSite;
import ac.topology.AbstractTopologyOperation;
import ac.topology.DoInBackgroundTopologyOperation;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.AssignStmt;

/**
 * @author Pan Linjie
 * @version 1.0
 */
public class AssignAsyncTaskUnitSummary extends AbstractActivityUnitSummary {
	private SootMethod doInBackgroundMethod;	
	
	public AssignAsyncTaskUnitSummary(SootMethod method, AssignStmt assignStmt) {
		super(assignStmt, method);
		this.valueSource = new ExplicitAllocationSite(assignStmt);
		this.setDoInBackgroundMethod();
	}

	public AssignAsyncTaskUnitSummary() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getSummary() {
		return SummaryAlphabet.ASSIGN_ASYNC_INSTANCE;
	}
	
	public void setDoInBackgroundMethod() {
		if (this.getValueSource() != null && this.getValueSource().getSourceClass() != null) {
			this.doInBackgroundMethod = this.initDoInBackgroundMethod();
			String key = DoInBackgroundTopologyOperation.getDoInBackgroundKey(this.doInBackgroundMethod);
			DoInBackgroundMethodSummary doInBackgroundSummary = (DoInBackgroundMethodSummary) AbstractTopologyOperation.getsMethodKeyToSummary().get(key);  
			if (doInBackgroundSummary == null) {
				AbstractTopologyOperation to = new DoInBackgroundTopologyOperation(this.doInBackgroundMethod);
				to.constructMainSummary();
			}
		}
	}
	
	public SootMethod getDoInBackgroundMethod() {
		return this.doInBackgroundMethod;
	}
	
	/**
	 * Get the doInBackground implemented by the class that extends AsyncTask
	 * @param sootClass
	 * @param methodName
	 * @return
	 */
	private SootMethod initDoInBackgroundMethod(SootClass asyncClass) {
		SootMethod resultMethod = null;
		for (SootMethod sootMethod : asyncClass.getMethods()) {
			if (sootMethod.getName().equals(MethodSignature.DO_IN_BACKGROUND_NAME)
					&& sootMethod.getDeclaration().contains("transient")
					&& !sootMethod.getDeclaration().contains("volatile")) {
				return sootMethod;
			}
			if (sootMethod.getName().equals(MethodSignature.DO_IN_BACKGROUND_NAME) 
					&& sootMethod.getDeclaration().contains("transient")) {
				resultMethod = sootMethod;
				continue;
			}
			if (sootMethod.getName().equals(MethodSignature.DO_IN_BACKGROUND_NAME) && resultMethod == null) {
				resultMethod = sootMethod;
			}
		}

		if (resultMethod == null && asyncClass.hasSuperclass()) {
			SootClass superclass = asyncClass.getSuperclass();
			return this.initDoInBackgroundMethod(superclass);
		}

		return resultMethod;
	}
	
	public SootMethod initDoInBackgroundMethod() {
		return this.initDoInBackgroundMethod(this.getValueSource().getSourceClass());
	}
	
	@Override
	public Object clone() {
		AssignAsyncTaskUnitSummary newSummary = new AssignAsyncTaskUnitSummary(this.currentMethod, (AssignStmt) this.currentUnit);
		newSummary.doInBackgroundMethod = this.doInBackgroundMethod;
		newSummary.valueSource = this.valueSource;
		return newSummary;
	}
}
