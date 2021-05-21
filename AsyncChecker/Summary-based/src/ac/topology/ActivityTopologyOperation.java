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

package ac.topology;

import ac.summary.AbstractMethodSummary;
import ac.summary.ActivityMethodListSummary;
import soot.SootClass;
import soot.SootMethod;

/**
 * 
 * @author Linjie Pan
 * @version 1.0
 */
public class ActivityTopologyOperation extends AbstractTopologyOperation {
	private SootClass mActivityClass;

	public SootClass getActivityClass() {
		return mActivityClass;
	}

	public void setActivityClass(SootClass activityClass) {
		this.mActivityClass = activityClass;
	}

	public ActivityTopologyOperation(SootMethod sourceMethod) {
		super(sourceMethod);
	}
	
	/**
	 * key=methodSignature+"-"+external
	 * @param methodUnderAnalysis
	 * @return
	 */
	public static String getActivityOperationKey(SootMethod methodUnderAnalysis){
		return methodUnderAnalysis.getSignature() + "-ExternalOperation";
	}
	
	@Override
	public String getKey(SootMethod theMethod) {
		return getActivityOperationKey(theMethod);
	}

	@Override
	public AbstractMethodSummary getSourceMethodSummary() {
		return AbstractTopologyOperation.sMethodKeyToSummary.get(ActivityTopologyOperation.getActivityOperationKey(this.mSourceMethod));
	}

	@Override
	public AbstractMethodSummary getMethodSummary(TopologyNode topNode) {
		return new ActivityMethodListSummary(topNode.mMethod);
	}
}
