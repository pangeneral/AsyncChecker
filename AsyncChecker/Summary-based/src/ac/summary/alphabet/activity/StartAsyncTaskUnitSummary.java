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

import java.util.ArrayList;
import java.util.List;


import ac.constant.ClassSignature;
import ac.summary.valuesource.AbstractValueSource;
import ac.util.InheritanceProcess;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;

/**
 * Start a AsyncTask by executing the "execute()" method of AsyncTask class
 * The field executeExpr represents the expr which executes the "execute" method 
 * @author Linjie Pan
 * @version 1.0
 */
public class StartAsyncTaskUnitSummary extends AbstractActivityUnitSummary {
	private InvokeExpr executeExpr;

	public InvokeExpr getExecuteExpr() {
		return executeExpr;
	}

	public void setExecuteExpr(InvokeExpr executeExpr) {
		this.executeExpr = executeExpr;
	}

	public void setExecuteExpr(Unit unit) {
		if (unit instanceof InvokeStmt)
			this.executeExpr = ((InvokeStmt) unit).getInvokeExpr();
		else if (unit instanceof AssignStmt)
			this.executeExpr = (InvokeExpr) ((AssignStmt) unit).getRightOp();
	}


	public StartAsyncTaskUnitSummary() {
		
	}
	
	public StartAsyncTaskUnitSummary(SootMethod method, Unit unit, List<Unit> unitList) {
		super(unit, method);
		this.setExecuteExpr(unit);
	}
	
	public StartAsyncTaskUnitSummary(SootMethod method, Unit unit, List<Unit> unitList, AbstractValueSource source) {
		super(unit, method, source);
		this.setExecuteExpr(unit);
	}
	
	@Override
	public String getSummary() {
		return SummaryAlphabet.START_ASYNC;
	}
	
	public List<SootField> getTaintedFieldList() {
		List<SootField> taintedFieldList = new ArrayList<SootField>();
		if (this.getValueSource() == null || this.getValueSource().getSourceClass() == null) {
			return taintedFieldList;
		}
		else {
			SootClass asyncClass = this.getValueSource().getSourceClass();
			for (SootField field: asyncClass.getFields()) {
				Type fieldType = field.getType();
				if (fieldType instanceof RefType) {
					SootClass fieldClass = ((RefType) fieldType).getSootClass();
					if (this.isInheritedFromView(fieldClass) || this.isInheritedFromFragment(fieldClass) 
							|| InheritanceProcess.isInheritedFromActivity(fieldClass)) {
						taintedFieldList.add(field);
					}
				}
			}
		}
		return taintedFieldList;
	}
	
	private boolean isInheritedFromView(SootClass sc) {
		return InheritanceProcess.isInheritedFromGivenClass(sc.getType(), Scene.v().getSootClassUnsafe(ClassSignature.VIEW).getType());
	}
	
	private boolean isInheritedFromFragment(SootClass sc) {
		return InheritanceProcess.isInheritedFromGivenClass(sc.getType(), Scene.v().getSootClassUnsafe(ClassSignature.FRAGMENT).getType()) ||
				InheritanceProcess.isInheritedFromGivenClass(sc.getType(), Scene.v().getSootClassUnsafe(ClassSignature.SUPPORT_FRAGMENT).getType());
	}		
	
	@Override
	public Object clone() {
		StartAsyncTaskUnitSummary newSummary = new StartAsyncTaskUnitSummary(this.currentMethod, this.currentUnit, null, this.valueSource);
		return newSummary;
	}
	
}
