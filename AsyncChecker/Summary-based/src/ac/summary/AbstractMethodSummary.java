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

package ac.summary;

import soot.SootMethod;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

/**
 * Base class of method summary
 * @author Linjie Pan
 * @version 1.0
 */
public abstract class AbstractMethodSummary {
	protected SootMethod methodUnderAnalysis;
	
	public SootMethod getMethodUnderAnalysis() {
		return methodUnderAnalysis;
	}

	public void setMethodUnderAnalysis(SootMethod methodUnderAnalysis) {
		this.methodUnderAnalysis = methodUnderAnalysis;
	}

	public void generateMethodSummary(){
		UnitGraph theGraph = new BriefUnitGraph(this.methodUnderAnalysis.getActiveBody());
		generation(theGraph);
	}
	
	public abstract void printMethodSummary();
	
	/**
	 * Abstract method to generate method summary.
	 * @param theGraph
	 */
	protected abstract void generation(UnitGraph theGraph);
	
	public AbstractMethodSummary(SootMethod methodUnderAnalysis){
		this.methodUnderAnalysis = methodUnderAnalysis;
	}
}
