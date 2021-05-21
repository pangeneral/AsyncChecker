package ac.config;

import com.config.parameter.process.AbstractParamProcess;

public class AsyncCheckerParamProcess extends AbstractParamProcess {

	@Override
	protected boolean setCustomOption(String option, String value) {
		switch (option) {
			case "DEBUG":
				Configuration.DEBUG = value.equals("1") ? true : false;
				break;
			case "maxRunningTime":
				Configuration.MAX_RUNNING_TIME = Long.parseLong(value);
				break;
			case "restrictBranch":
				Configuration.RESTRICT_BRANCH = value.equals("1") ? true : false;
				break;
			case "maxBranchNumber":
				Configuration.MAX_BRANCH_NUMBER = Long.parseLong(value);
				break;
			case "maxValueFlowNumber":
				Configuration.MAX_VALUE_FLOW_NUMBER = Long.parseLong(value);
				break;
			default:
				return false;
		}
		return true;
	}

	@Override
	protected void initCustomPath() {
		
	}

	@Override
	protected boolean isCustomAllRequiredConfigItemsSet() {
		// TODO Auto-generated method stub
		return false;
	}

}
