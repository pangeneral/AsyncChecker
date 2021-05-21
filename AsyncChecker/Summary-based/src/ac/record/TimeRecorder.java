package ac.record;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.config.parameter.BaseConfiguration;

public class TimeRecorder {
	public static void record(long preprocessTime, long analysisTime) throws IOException {
		String path = BaseConfiguration.OUTPUT_BASE_PATH + File.separator + "time.txt";
		FileWriter writer = new FileWriter(new File(path), true);
		writer.write(BaseConfiguration.APK_NAME + ", " + preprocessTime + ", " + analysisTime);
		writer.close();
	}
}
