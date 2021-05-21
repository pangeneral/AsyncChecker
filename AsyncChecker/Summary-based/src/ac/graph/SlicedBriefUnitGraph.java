package ac.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import soot.Unit;

public class SlicedBriefUnitGraph {
	private List<Unit> headUnit = new ArrayList<Unit>();
	private Map<Unit, Set<Unit>> unitToSuccs = new HashMap<Unit, Set<Unit>>();
	private Set<Unit> unitChain = new HashSet<Unit>();
	
	public List<Unit> getHeadUnit() {
		return headUnit;
	}
	public void setHeadUnit(List<Unit> headUnit) {
		this.headUnit = headUnit;
	}
	public Map<Unit, Set<Unit>> getUnitToSuccs() {
		return unitToSuccs;
	}
	public void setUnitToSuccs(Map<Unit, Set<Unit>> unitToSuccs) {
		this.unitToSuccs = unitToSuccs;
	}
	public Set<Unit> getUnitChain() {
		return unitChain;
	}
	public void setUnitChain(Set<Unit> unitChain) {
		this.unitChain = unitChain;
	}
	
	public Set<Unit> getSuccsOf(Unit unit) {
		return this.unitToSuccs.getOrDefault(unit, new HashSet<Unit>());
	}
	
	
}
