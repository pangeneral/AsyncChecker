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

import java.util.ArrayList;
import java.util.List;
import soot.SootMethod;
import soot.jimple.Stmt;

/**
 * Graph node for topology sort
 * @author Linjie Pan
 * @version 1.0
 */
public class TopologyNode{
	SootMethod mMethod;//the SootMethod represented by the node
	Stmt mStmt;//the statement that invokes the SootMethod
	
	int mOutDegree;
	boolean mEverInStack;
	List<TopologyNode> mPointingNodes;
	
	public List<TopologyNode> getPointingNodes(){
		return mPointingNodes;
	}
	
	public TopologyNode(SootMethod method,Stmt stmt){
		this.mMethod = method;
		this.mStmt = stmt;
		this.mOutDegree = 0;
		this.mEverInStack = false;
		this.mPointingNodes = new ArrayList<TopologyNode>();
	}
}
