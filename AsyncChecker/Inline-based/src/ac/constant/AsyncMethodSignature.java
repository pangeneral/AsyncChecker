package ac.constant;

public class AsyncMethodSignature {
	public final static String EXECUTE_SIGNATURE = "<android.os.AsyncTask: android.os.AsyncTask execute(java.lang.Object[])>";
	public final static String CANCEL_SIGNATURE = "<android.os.AsyncTask: boolean cancel(boolean)>";
	public final static String EXECUTE_ON_EXECUTOR = "<android.os.AsyncTask: android.os.AsyncTask executeOnExecutor(java.util.concurrent.Executor,java.lang.Object[])>";
	public final static String IS_CANCELLED_SIGNATURE = "<android.os.AsyncTask: boolean isCancelled()>";
	
	public final static String GET_STATUS_SUBSIG = "android.os.AsyncTask$Status getStatus()";
	public final static String EXECUTE_SUBSIG = "android.os.AsyncTask execute(java.lang.Object[])";
	public final static String CANCEL_SUBSIG = "boolean cancel(boolean)";
	public final static String EXECUTE_ON_EXECUTOR_SUBSIG = "android.os.AsyncTask executeOnExecutor(java.util.concurrent.Executor,java.lang.Object[])";
	public final static String IS_CANCELLED_SUBSIG = "boolean isCancelled()";
	
	public final static String DO_IN_BACKGROUND_NAME = "doInBackground";
	
	public final static String FIND_VIEW_BY_ID_SINGATURE = "<android.view.View findViewById(int)>";
}
