package ac.config;

public class Configuration {	
	public static boolean DEBUG = false;
	
	public static long MAX_RUNNING_TIME = 1800000;
	
	/**
	 * The file to record the exception information of this program.
	 */
	public static final String EXCEPTION_FILE_NAME = "_Exception.txt";
	
	public static boolean RESTRICT_BRANCH = false;
	
	public static long MAX_BRANCH_NUMBER = 10;
	
	public static long MAX_VALUE_FLOW_NUMBER = 30;
}
