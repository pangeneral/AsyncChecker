package ac.processor;

import soot.jimple.AssignStmt;
import soot.jimple.NewArrayExpr;
import androlic.entity.ContextMessage;
import androlic.entity.GlobalMessage;
import androlic.entity.value.heap.array.NewArrayHeapObject;
import androlic.execution.processor.rightop.expr.newex.INewArrayExprProcessor;

public class AsyncNewArrayExprProcessor implements INewArrayExprProcessor{

	private static AsyncNewArrayExprProcessor processor;

	private AsyncNewArrayExprProcessor() {
	}

	public static AsyncNewArrayExprProcessor v() {
		if (processor == null) {
			processor = new AsyncNewArrayExprProcessor();
		}
		return processor;
	}
	
	public NewArrayHeapObject getNewArrayHeapObject(AssignStmt stmt, NewArrayExpr newArrayExpr,
			ContextMessage context, GlobalMessage globalMessage) {
		return null;
	}

}
