package ac.test;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import androlic.Main;
import androlic.config.AndrolicConfigurationManager;
import androlic.config.BaseConfiguration;

public class UnitTest {
	
	@Test
	public void test() throws IOException {
//		String basePath = "E:\\restgroup\\AsyncBench\\Apks\\combination\\CombinationInheritance.apk";
//		File folder = new File(basePath);
//		if (folder.isFile()) {
//			System.out.println(folder.getName());
//			System.out.println(folder.getParent());
//			System.out.println(folder.getCanonicalPath());
//			System.out.println(folder.getParentFile().getName());
//		}
		String basePath = "E:\\restgroup\\AsyncBench\\Apks";
		processFile(new File(basePath));
	}
	
	public void processFile(File file) {
		if (file.isFile()) {
			AndrolicConfigurationManager.init(new String[]{
					"-configureFile", "configure\\configuration.txt",
					"-apkName", file.getName(),
					"-apkBasePath", file.getParent(),
					"-outputBasePath", "jimple\\apk" + File.separator + file.getParentFile().getName()});
			
			Main main = new Main();
			
			main.analyzeApk();
			return;
		}
		File fileList[] = file.listFiles();
		for (int i = 0; i < fileList.length; i++)
			processFile(fileList[i]);
	}
	
}
