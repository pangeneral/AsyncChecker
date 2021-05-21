package ac.record;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.config.parameter.BaseConfiguration;

public class DebugRecorder {
	public static void recordDebugMessage(String message)  {
		try {
			String path = BaseConfiguration.OUTPUT_BASE_PATH + File.separator + "debug.txt";
			FileWriter writer = new FileWriter(new File(path), true);
			writer.write(message + "\n");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
