package ac.entity;

import java.util.ArrayList;
import java.util.List;

import androlic.entity.AbstractTypeState;
import soot.Unit;

public class AsyncTypeState extends AbstractTypeState{
	private AsyncTaskStatus currentStatus = AsyncTaskStatus.PENDING;
    
    /**
     * AsyncTask can only be executed once
     */
    private boolean isExecuted = false;
    
    private boolean isCancelled = false;
    
    private List<Unit> executionUnitList = new ArrayList<Unit>();
    
	public List<Unit> getExecutionUnitList() {
		return executionUnitList;
	}

	public void setExecutionUnitList(List<Unit> executionUnitList) {
		this.executionUnitList = executionUnitList;
	}

	public AsyncTaskStatus getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(AsyncTaskStatus currentStatus) {
		this.currentStatus = currentStatus;
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	public boolean isExecuted() {
		return isExecuted;
	}

	public void setExecuted(boolean isExecuted) {
		this.isExecuted = isExecuted;
	}

	@Override
	public Object clone() {
		AsyncTypeState cloneObject = new AsyncTypeState();
		cloneObject.isCancelled = this.isCancelled;
		cloneObject.isExecuted = this.isExecuted;
		cloneObject.currentStatus = this.currentStatus;
		cloneObject.executionUnitList.addAll(this.executionUnitList);
		return cloneObject;
	}
}
