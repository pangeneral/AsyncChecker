package ac.checker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import ac.constant.MisuseType;
import ac.record.DefaultAsyncMisuseRecorder;
import ac.record.StrongReferenceCustomRecorder;
import ac.summary.DoInBackgroundMethodSummary;
import ac.summary.alphabet.activity.AbstractActivityUnitSummary;
import ac.summary.alphabet.activity.AssignAsyncTaskUnitSummary;
import ac.summary.alphabet.activity.CancelAsyncTaskUnitSummary;
import ac.summary.alphabet.activity.StartAsyncTaskUnitSummary;
import ac.summary.alphabet.activity.SummaryAlphabet;
import ac.topology.AbstractTopologyOperation;
import ac.topology.DoInBackgroundTopologyOperation;


public class ValueFlowChecker {
	enum Status{
		Pending,Running,Wrong
	}
	
	private static Map<Status,Map<String,Status>> sTransitionMatrix;
	
	private static void initTransitionMatrix(){ 
		sTransitionMatrix = new HashMap<Status,Map<String,Status>>();
		
		Map<String,Status> pendingTransition = new HashMap<String,Status>();
		pendingTransition.put(SummaryAlphabet.ASSIGN_ASYNC_INSTANCE, Status.Pending);
		pendingTransition.put(SummaryAlphabet.START_ASYNC, Status.Running);
		pendingTransition.put(SummaryAlphabet.CANCEL_ASYNC, Status.Wrong);
		sTransitionMatrix.put(Status.Pending, pendingTransition);
		
		Map<String,Status> runningTransition = new HashMap<String,Status>();
		runningTransition.put(SummaryAlphabet.ASSIGN_ASYNC_INSTANCE, Status.Pending);
		runningTransition.put(SummaryAlphabet.START_ASYNC, Status.Wrong);
		runningTransition.put(SummaryAlphabet.CANCEL_ASYNC, Status.Running);
		sTransitionMatrix.put(Status.Running, runningTransition);
	}
	
	/**
	 * This method detect whether a value flow contains misuse of AsyncTask field under analysis 
	 * @param controlFlowSummary is the current value flow 
	 * @param asyncTaskField is the AsyncTask field under analysis
	 * @return
	 */
	public static boolean check(List<AbstractActivityUnitSummary> controlFlowSummary, SootClass asyncClass, 
			AssignAsyncTaskUnitSummary beginAssignSummary, Set<String> recordedMisuseType) {
		if (sTransitionMatrix == null)
			initTransitionMatrix();
		DefaultAsyncMisuseRecorder proxyRecorder = new DefaultAsyncMisuseRecorder();
		AssignAsyncTaskUnitSummary currentAssignSummary = beginAssignSummary;
		boolean hasCanceled = false;
		Status currentStatus = Status.Pending;
		for (int i = 1; i < controlFlowSummary.size(); i++) {
			AbstractActivityUnitSummary unitSummary = controlFlowSummary.get(i);
			
			if (unitSummary instanceof CancelAsyncTaskUnitSummary) {
				hasCanceled = true;
			}
			
			Status transitionStatus = sTransitionMatrix.get(currentStatus).get(unitSummary.getSummary());
			if (transitionStatus == Status.Pending) {
				currentStatus = transitionStatus;
				currentAssignSummary = (AssignAsyncTaskUnitSummary) unitSummary;
			}
			else if (transitionStatus == Status.Running) {
				if (unitSummary instanceof StartAsyncTaskUnitSummary) {
					List<SootField> fieldList = ((StartAsyncTaskUnitSummary) unitSummary).getTaintedFieldList();
					if (fieldList.size() > 0) {		/*StrongReference*/
						StrongReferenceCustomRecorder customRecorder = new StrongReferenceCustomRecorder(fieldList);
						proxyRecorder.setRecorder(customRecorder);
						proxyRecorder.recordAsyncTaskMisuse(asyncClass, MisuseType.StrongReference);
						proxyRecorder.setRecorder(null);
					}
					
					SootMethod doInBackgroundMethod = currentAssignSummary.getDoInBackgroundMethod();
					String key = DoInBackgroundTopologyOperation.getDoInBackgroundKey(doInBackgroundMethod);
					DoInBackgroundMethodSummary doInBackgroundSummary = (DoInBackgroundMethodSummary) AbstractTopologyOperation.getsMethodKeyToSummary().get(key);  
					if (doInBackgroundSummary != null && !doInBackgroundSummary.isAllLoopCancelled()) {		/*NotTerminate*/
						proxyRecorder.recordAsyncTaskMisuse(controlFlowSummary, asyncClass, MisuseType.NotTerminate);
					}
				}
				currentStatus = transitionStatus;
			}
			else{
				if (currentStatus == Status.Running) {		/*RepeatStart*/
					proxyRecorder.recordAsyncTaskMisuse(controlFlowSummary, asyncClass, MisuseType.RepeatStart);
					break;
				}
				else if (currentStatus == Status.Pending) {		/*EarlyCancel*/
					proxyRecorder.recordAsyncTaskMisuse(controlFlowSummary, asyncClass, MisuseType.EarlyCancel);
					break;
				}
			}
		}
		return hasCanceled;
	}
}
