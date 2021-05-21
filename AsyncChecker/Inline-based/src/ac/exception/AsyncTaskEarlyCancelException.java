package ac.exception;

import soot.Local;
import ac.entity.AsyncTaskRefObject;
import androlic.exception.AbstractAndrolicException;

@SuppressWarnings("serial")
public class AsyncTaskEarlyCancelException extends AbstractAndrolicException{

	public AsyncTaskEarlyCancelException(Local variable,
			AsyncTaskRefObject asyncObject) {
		super(variable + " is cancelled before executing");
	}
}
