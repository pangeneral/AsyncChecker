package ac.config;

import com.config.parameter.BaseConfiguration;

import jymbolic.android.config.AndroidBaseConfiguration;

public class AndroidConfigAdapter {
	
	/**
	 * Both android-soot-config-manager.jar and jymbolic-android.jar have config items for android analysis
	 * Currently, we apply the configuration of android-soot-config-manager.jar. 
	 * However, we still need to use jymbolic-android.jar.
	 * Therefore, we need to set the config items of jymbolic-android.jar manullay.
	 * Which means assigning the value of config item of android-soot-config-manager.jar to jymbolic-android.jar 
	 */
	public static void setJymbolicAndroidConfigItem() {
		AndroidBaseConfiguration.APK_NAME = BaseConfiguration.APK_NAME;
		AndroidBaseConfiguration.APK_BASE_PATH = BaseConfiguration.APK_BASE_PATH;
		AndroidBaseConfiguration.ANDROID_PLATFORM_PATH = BaseConfiguration.ANDROID_PLATFORM_PATH;
	}
}
