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
import ac.summary.DoInBackgroundMethodSummary;
import soot.SootMethod;

/**
 * 
 * @author Linjie Pan
 * @version 1.0
 */
public class DoInBackgroundTopologyOperation extends AbstractTopologyOperation {

	public DoInBackgroundTopologyOperation(SootMethod sourceMethod) {
		super(sourceMethod);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void printTopologyGraph() {
		// TODO Auto-generated method stub
	}
	
	/**
	 * key=methodSignature+"-DO_IN_BACKGROUND";
	 * @param theMethod
	 * @return
	 */
	public static String getDoInBackgroundKey(SootMethod theMethod){
		return theMethod.getSignature()+"-DO_IN_BACKGROUND";
	}
	
	
	@Override
	public AbstractMethodSummary getSourceMethodSummary() {
		// TODO Auto-generated method stub
		return AbstractTopologyOperation.sMethodKeyToSummary.get(getDoInBackgroundKey(this.mSourceMethod));
	}

	@Override
	public String getKey(SootMethod theMethod) {
		// TODO Auto-generated method stub
		return getDoInBackgroundKey(theMethod);
	}

	@Override
	public AbstractMethodSummary getMethodSummary(TopologyNode topNode) {
		// TODO Auto-generated method stub
		return new DoInBackgroundMethodSummary(topNode.mMethod);
	}
	
	

}
