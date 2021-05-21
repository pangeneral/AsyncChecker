package ac.record;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.config.parameter.BaseConfiguration;

import soot.SootClass;
import ac.checker.SummaryBasedChecker;
import ac.summary.alphabet.activity.AbstractActivityUnitSummary;

public class DefaultAsyncMisuseRecorder {	
	/**
	 * Please instantiate the field if you want to record custom error message
	 */
	private AbstractCustomAsyncMisuseRecorder recorder;
	
	public AbstractCustomAsyncMisuseRecorder getRecorder() {
		return recorder;
	}

	public void setRecorder(AbstractCustomAsyncMisuseRecorder recorder) {
		this.recorder = recorder;
	}

	private String getErrorBasePath(){
		return BaseConfiguration.ERROR_FOLDER;
	}
	
	/**
	 * Record base misuse message for AsyncTask
	 * @param controlFlowSummary
	 * @param asyncClass
	 * @param misuseType
	 */
	public void recordAsyncTaskMisuse(List<AbstractActivityUnitSummary> controlFlowSummary, SootClass asyncClass, String misuseType) {
		try {
			if (!SummaryBasedChecker.asyncClassToRecordError.get(asyncClass).contains(misuseType)) {
				FileWriter sumWriter = new FileWriter(getErrorBasePath()+File.separator+ misuseType + "-sum.txt",true);
				sumWriter.write(asyncClass.getName()+"\n");
				sumWriter.close();
				
				FileWriter writer=new FileWriter(getErrorBasePath()+File.separator+misuseType+".txt",true);
				writer.write(misuseType + " "+asyncClass.getName()+"\n");
				this.recordExecutionPath(writer, controlFlowSummary);
				
				if (recorder != null) {
					recorder.recordCustomMessage(writer, controlFlowSummary, asyncClass);
				}
				
				writer.write("\n");
				writer.close();
				SummaryBasedChecker.asyncClassToRecordError.get(asyncClass).add(misuseType);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Record base misuse message for AsyncTask
	 * @param controlFlowSummary
	 * @param asyncClass
	 * @param misuseType
	 */
	public void recordAsyncTaskMisuse(SootClass asyncClass, String misuseType) {
		try {
			if (!SummaryBasedChecker.asyncClassToRecordError.get(asyncClass).contains(misuseType)) {
				FileWriter sumWriter = new FileWriter(getErrorBasePath()+File.separator+ misuseType + "-sum.txt",true);
				sumWriter.write(asyncClass.getName()+"\n");
				sumWriter.close();
				
				FileWriter writer=new FileWriter(getErrorBasePath()+File.separator+misuseType+".txt",true);
				writer.write(misuseType + " "+asyncClass.getName()+"\n");
				
				if (recorder != null) {
					recorder.recordCustomMessage(writer, asyncClass);
				}
				
				writer.write("\n");
				writer.close();
				SummaryBasedChecker.asyncClassToRecordError.get(asyncClass).add(misuseType);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void recordExecutionPath(FileWriter writer,List<AbstractActivityUnitSummary> controlFlowSummary) throws IOException {
		for (int i = 0; i < controlFlowSummary.size(); i++) {
			writer.write(i + 1 + " " + controlFlowSummary.get(i).getSummary() + " " + 
					controlFlowSummary.get(i).getCurrentMethod().getSignature() + " " +
					controlFlowSummary.get(i).getCurrentUnit().toString() + "\n");
		}
	}
}
