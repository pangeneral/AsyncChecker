package ac.exception;

import soot.Local;
import ac.entity.AsyncTaskRefObject;
import androlic.exception.AbstractAndrolicException;

@SuppressWarnings("serial")
public class AsyncTaskRepeatStartException extends AbstractAndrolicException {

	public AsyncTaskRepeatStartException(Local variable,
			AsyncTaskRefObject asyncObject) {
		super(variable + " repeat starting");
	}

}
