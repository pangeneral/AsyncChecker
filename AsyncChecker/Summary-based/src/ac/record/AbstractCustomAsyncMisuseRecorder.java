package ac.record;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import soot.SootClass;
import ac.summary.alphabet.activity.AbstractActivityUnitSummary;

public abstract class AbstractCustomAsyncMisuseRecorder {
	public abstract void recordCustomMessage(FileWriter writer, SootClass asyncClass) throws IOException;
	
	public abstract void recordCustomMessage(FileWriter writer, List<AbstractActivityUnitSummary> controlFlowSummary, SootClass asyncClass) throws IOException;
}
