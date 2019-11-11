package ac.processor;

import java.lang.reflect.InvocationTargetException;
import soot.Local;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import ac.constant.AsyncMethodSignature;
import ac.entity.AsyncTaskOperation;
import ac.entity.AsyncTaskRefObject;
import ac.entity.AsyncTaskStatus;
import ac.entity.AsyncTypeState;
import ac.util.AsyncTaskOperationChecker;
import ac.util.AsyncTaskStatusChecker;
import androlic.entity.ContextMessage;
import androlic.entity.GlobalMessage;
import androlic.entity.value.IBasicValue;
import androlic.entity.value.numeric.BasicNumericConstant;
import androlic.exception.AbstractAndrolicException;
import androlic.execution.processor.library.proxy.ILibraryInvocationProcessor;

public class AsyncLibraryInvocationProcessor implements ILibraryInvocationProcessor {

	private static AsyncLibraryInvocationProcessor processor;

	private AsyncLibraryInvocationProcessor() {
	}

	public static AsyncLibraryInvocationProcessor v() {
		if (processor == null) {
			processor = new AsyncLibraryInvocationProcessor();
		}
		return processor;
	}

	public IBasicValue getLibraryInvocationReturnValue(AssignStmt stmt, InvokeExpr libraryInvokeExpr, ContextMessage context,
			GlobalMessage globalMessage) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ClassNotFoundException, CloneNotSupportedException, 
			AbstractAndrolicException {
		return this.processLibraryInvokeExpr(stmt, libraryInvokeExpr, context, globalMessage);
	}

	public boolean processLibraryInvocation(InvokeStmt stmt, InvokeExpr libraryInvokeExpr, ContextMessage context,
			GlobalMessage globalMessage) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, AbstractAndrolicException {
		if (this.processLibraryInvokeExpr(stmt, libraryInvokeExpr, context, globalMessage) != null)
			return true;
		else
			return false;
	}
	
	private IBasicValue processLibraryInvokeExpr(Unit unit, InvokeExpr libraryInvokeExpr, ContextMessage context, GlobalMessage globalMessage) 
			throws AbstractAndrolicException {
		if (libraryInvokeExpr instanceof InstanceInvokeExpr) {
			IBasicValue asyncObject = context.getLocalToObject().get(((InstanceInvokeExpr) libraryInvokeExpr).getBase());
			if (asyncObject instanceof AsyncTaskRefObject) {
				AsyncTypeState state = ((AsyncTaskRefObject) asyncObject).getTypeState(globalMessage);
				String subSignature = libraryInvokeExpr.getMethod().getSubSignature();
				Local base = (Local) ((InstanceInvokeExpr) libraryInvokeExpr).getBase();
				switch(subSignature) {
				case AsyncMethodSignature.EXECUTE_SUBSIG:
				case AsyncMethodSignature.EXECUTE_ON_EXECUTOR_SUBSIG:
					return this.executeAsyncTask((AsyncTaskRefObject) asyncObject, base, unit, context, globalMessage);
				case AsyncMethodSignature.CANCEL_SUBSIG:
					this.cancelAsyncTask((AsyncTaskRefObject) asyncObject, base, unit, context, globalMessage);
					return new BasicNumericConstant(IntConstant.v(1));
				case AsyncMethodSignature.IS_CANCELLED_SUBSIG:
					return state.isCancelled() ? 
							new BasicNumericConstant(IntConstant.v(1)) : 
							new BasicNumericConstant(IntConstant.v(0));
//				case AsyncMethodSignature.GET_STATUS_SUBSIG:
				default:
					return null;
				}
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	private AsyncTaskRefObject executeAsyncTask(AsyncTaskRefObject asyncObject, Local base, Unit unit, ContextMessage context, GlobalMessage globalMessage) throws AbstractAndrolicException {
		AsyncTypeState state = asyncObject.getTypeState(globalMessage);
		state.getExecutionUnitList().add(unit);
		AsyncTaskStatusChecker.checkAsyncTaskOperation(base, asyncObject, AsyncTaskOperation.Execute, unit, globalMessage);
		state.setCurrentStatus(AsyncTaskStatus.RUNNING);
		state.setExecuted(true);
		
		AsyncTaskOperationChecker.checkStrongReference(asyncObject, unit, globalMessage);
		AsyncTaskOperationChecker.checkNotTerminate(asyncObject, unit, globalMessage);
		
		return asyncObject;
	}
	
	
	
	private boolean cancelAsyncTask(AsyncTaskRefObject asyncObject, Local base, Unit unit, ContextMessage context, GlobalMessage globalMessage) throws AbstractAndrolicException {
		AsyncTypeState state = asyncObject.getTypeState(globalMessage);
		state.getExecutionUnitList().add(unit);
		AsyncTaskStatusChecker.checkAsyncTaskOperation(base, asyncObject, AsyncTaskOperation.Cancel, unit, globalMessage);
		state.setCurrentStatus(AsyncTaskStatus.CANCEL);
		state.setCancelled(true);
		return true;
	}
}
