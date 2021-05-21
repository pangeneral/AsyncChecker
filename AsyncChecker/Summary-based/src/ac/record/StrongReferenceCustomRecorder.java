package ac.record;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import soot.SootClass;
import soot.SootField;
import ac.summary.alphabet.activity.AbstractActivityUnitSummary;

public class StrongReferenceCustomRecorder extends AbstractCustomAsyncMisuseRecorder {
	
	private List<SootField> taintedFieldList;
	
	public List<SootField> getTaintedFieldList() {
		return taintedFieldList;
	}

	public void setTaintedFieldList(List<SootField> taintedFieldList) {
		this.taintedFieldList = taintedFieldList;
	}

	public StrongReferenceCustomRecorder(List<SootField> taintedList) {
		this.taintedFieldList = taintedList;
	}
	
	@Override
	public void recordCustomMessage(FileWriter writer, List<AbstractActivityUnitSummary> controlFlowSummary, SootClass asyncClass) throws IOException {
		writer.write("Tainted fields: ");
		for (int i = 0; i < taintedFieldList.size() - 1; i++) {
			writer.write(taintedFieldList.get(i).getSubSignature() + ", ");
		}
		writer.write(taintedFieldList.get(taintedFieldList.size() - 1) + "\n");
	}

	@Override
	public void recordCustomMessage(FileWriter writer, SootClass asyncClass) throws IOException {
		
	}
}
