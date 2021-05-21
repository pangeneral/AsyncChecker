package ac.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import androlic.Main;
import androlic.config.AndrolicConfigurationManager;

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
	
	@Test
	public void testAsyncChecker() {
		
	}
	
	@Test
	public void mapTest() {
		class A {
			int value;
		}
		
		class B {
			int value;
		}
		
		class C {
			int value;
		}
		
		Map<A, Map<B, C>> formerMap = new HashMap<A, Map<B, C>>();
		A a1 = new A();
		a1.value = -1;
		B b1 = new B();
		b1.value = -1;
		C c1 = new C();
		c1.value = -1;
		C c2 = new C();
		c2.value = 1200;
		
		formerMap.put(a1, new HashMap<B, C>());
		formerMap.get(a1).put(b1, c1);
		
		Map<A, Map<B, C>> secondMap = new HashMap<A, Map<B, C>>();
		for (Map.Entry<A, Map<B, C>> entry: formerMap.entrySet()) {
			Map<B, C> newMap = new HashMap<B, C>();
			newMap.putAll(entry.getValue());
			secondMap.put(entry.getKey(), newMap);
		}
		secondMap.get(a1).put(b1, c2);
		System.out.println(formerMap.get(a1).get(b1).value);
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
