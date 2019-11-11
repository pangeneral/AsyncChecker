/* AsyncDetecotr - an Android async component misuse detection tool
 * Copyright (C) 2018 Baoquan Cui
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
package ac.summary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ac.constant.AsyncClassSignature;
import androlic.util.ClassInheritanceProcess;
import androlic.util.ClassInheritanceProcess.MatchType;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

/**
 * The summary of doInBackground method of AsyncTask
 * @author Baoquan Cui
 * @version 1.0
 */
public class DoInBackgroundMethodSummary {

	private List<UnitInfo> mUnitInfos = new ArrayList<UnitInfo>();

	private Set<Unit> mLoopHeaderList = new HashSet<>();

	private Set<Unit> mCancelUnitList = new HashSet<>();

	private UnitGraph mUnitGraph = null;

	private SootMethod methodUnderAnalysis = null;
	
	public DoInBackgroundMethodSummary(SootMethod analysisMethod) {
		this.methodUnderAnalysis = analysisMethod;
		mUnitGraph = new BriefUnitGraph(this.methodUnderAnalysis.getActiveBody());
	}

	public Set<Unit> getLoopStartUnits() {
		return mLoopHeaderList;
	}

	public Set<Unit> getCancelledUnits() {
		return mCancelUnitList;
	}

	public SootMethod getMethodUnderAnalysis() {
		return methodUnderAnalysis;
	}

	public void setMethodUnderAnalysis(SootMethod methodUnderAnalysis) {
		this.methodUnderAnalysis = methodUnderAnalysis;
	}

	public boolean isAllLoopCancelled() {
		for (Unit unit : mCancelUnitList) {
			UnitInfo unitInfo = getUnitInfo(unit);
			if (unitInfo.mLoopHeaderUnit != null) {
				UnitInfo headerUnitInfo = getUnitInfo(unitInfo.mLoopHeaderUnit);
				headerUnitInfo.isCancelled = true;
			}
			if( mLoopHeaderList.contains(unit) ){
				UnitInfo headerUnitInfo = getUnitInfo(unit);
				headerUnitInfo.isCancelled = true;
			}
		}
		for (Unit unit : mLoopHeaderList) {
			UnitInfo unitInfo = getUnitInfo(unit);
			if (!unitInfo.isCancelled) {
				return false;
			}
		}
		return true;
	}

	public void generation(Set<SootMethod> visitedMethod) {
		traverUnitGraph(mUnitGraph.getHeads().get(0), 0);

		for (UnitInfo unitInfo : mUnitInfos) {
			if (unitInfo.mLoopHeaderUnit == null) {
				continue;
			}
			mLoopHeaderList.add(unitInfo.mLoopHeaderUnit);
		}

		for (Unit unit : mUnitGraph) {
			InvokeExpr theExpr = null;
			if (((Stmt) unit).containsInvokeExpr()) 
				theExpr = ((Stmt) unit).getInvokeExpr();

			if (theExpr != null && theExpr.getMethod().hasActiveBody() && !visitedMethod.contains(theExpr.getMethod())) {
				visitedMethod.add(theExpr.getMethod());
				DoInBackgroundMethodSummary subSummary = new DoInBackgroundMethodSummary(theExpr.getMethod());
				subSummary.generation(visitedMethod);
				this.mLoopHeaderList.addAll(subSummary.mLoopHeaderList);
				this.mCancelUnitList.addAll(subSummary.mCancelUnitList);
				this.mUnitInfos.addAll(subSummary.mUnitInfos);
			}
			
			if (unit instanceof JAssignStmt) {
				JAssignStmt jAssignStmt = (JAssignStmt) unit;
				if (jAssignStmt.containsInvokeExpr()) {
					InvokeExpr invokeExpr = jAssignStmt.getInvokeExpr();

					SootMethod sootMethod = invokeExpr.getMethod();
					if (ClassInheritanceProcess.isInheritedFromGivenClass(sootMethod.getDeclaringClass(), AsyncClassSignature.ASYNC_TASK, MatchType.equal)
							&& sootMethod.getName().contains("isCancelled")) {
						// cancelledCount++;
						mCancelUnitList.add(unit);
					}
				}
			}
		}

		mLoopHeaderList.remove(null);
	}

	private Unit traverUnitGraph(Unit b0, int deepFirstSearchPathPosition) {
		// return: innermost loop header of b0
		UnitInfo unitInfo = getUnitInfo(b0);
		unitInfo.visited = true;
		unitInfo.mDeepFirstSearchPathPosition = deepFirstSearchPathPosition;
		for (Unit b : mUnitGraph.getSuccsOf(b0)) {
			UnitInfo unitInfob = getUnitInfo(b);
			if (!unitInfob.visited) {
				// case(A)
				Unit nh = traverUnitGraph(b, deepFirstSearchPathPosition + 1);
				tagLoopHeader(b0, nh);
			} else {
				if (unitInfob.mDeepFirstSearchPathPosition > 0) {
					// case(B)
					unitInfob.visited = true;
					tagLoopHeader(b0, b);
				} else if (unitInfob.mLoopHeaderUnit == null) {
					// case(C)

				} else {
					Unit h = unitInfob.mLoopHeaderUnit;
					UnitInfo unitInfoH = getUnitInfo(h);
					if (unitInfoH.mDeepFirstSearchPathPosition > 0) {
						// case(D)
						tagLoopHeader(b0, h);
					} else {
						// case(E) re-entry
						while (unitInfoH.mLoopHeaderUnit != null) {
							unitInfoH = getUnitInfo(unitInfoH.mLoopHeaderUnit);
							if (unitInfoH.mDeepFirstSearchPathPosition > 0) {
								tagLoopHeader(b0, h);
								break;
							}
						}
					}
				}
			}
		}
		unitInfo.mDeepFirstSearchPathPosition = 0;
		return unitInfo.mLoopHeaderUnit;
	}

	private void tagLoopHeader(Unit b, Unit h) {
		if (b == h || h == null) {
			return;
		}
		UnitInfo cur1 = getUnitInfo(b);
		UnitInfo cur2 = getUnitInfo(h);
		while (cur1.mLoopHeaderUnit != null) {
			UnitInfo ih = getUnitInfo(cur1.mLoopHeaderUnit);
			if (ih == cur2) {
				return;
			}
			if (ih.mDeepFirstSearchPathPosition < cur2.mDeepFirstSearchPathPosition) {
				cur1.mLoopHeaderUnit = cur2.mUnit;
				cur1 = cur2;
				cur2 = ih;
			} else {
				cur1 = ih;
			}
		}
		cur1.mLoopHeaderUnit = cur2.mUnit;

	}

	private UnitInfo getUnitInfo(Unit unit) {
		for (UnitInfo unitInfo : mUnitInfos) {
			if (unitInfo.mUnit.equals(unit)) {
				return unitInfo;
			}
		}
		UnitInfo unitInfo = new UnitInfo();
		unitInfo.mUnit = unit;
		mUnitInfos.add(unitInfo);
		return unitInfo;
	}
	
	/**
	 *  
	 */
	static class UnitInfo {
		private Unit mUnit = null;
		private boolean visited = false;
		private int mDeepFirstSearchPathPosition = 0;
		private Unit mLoopHeaderUnit = null;
		private boolean isCancelled = false;

		@Override
		public boolean equals(Object obj) {
			boolean result = mUnit
					.equals(obj instanceof UnitInfo ? ((UnitInfo) obj).mUnit
							: obj);
			return result;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("\n******************\n");
			sb.append("unit-->" + mUnit);
			sb.append("\n");
			sb.append("iloop_header-->" + mLoopHeaderUnit);
			sb.append("\n");
			sb.append("DFEP_pos-->" + mDeepFirstSearchPathPosition);
			return sb.toString();
		}

	}

}
