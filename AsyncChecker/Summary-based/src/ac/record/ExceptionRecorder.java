/* AsyncDetecotr - an Android async component misuse detection tool
 * Copyright (C) 2018 Baoquan Cui
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package ac.record;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

import ac.config.Configuration;

import com.config.parameter.BaseConfiguration;

/**
 * 
 * @author Baoquan Cui
 * @version 1.0
 */
public class ExceptionRecorder {

	public static void saveException(Exception exception) {
		String dataSet = "";
		String filePath = BaseConfiguration.OUTPUT_BASE_PATH + dataSet 
				+ Configuration.EXCEPTION_FILE_NAME;
		File file = new File(filePath);
		
		exception.printStackTrace();

		BufferedWriter out = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true)));// true,进行追加写。
			out.write(new Date(System.currentTimeMillis()).toString() + "\r\n");
			out.write(dataSet + "\r\n");
			out.write(BaseConfiguration.APK_BASE_PATH + "\r\n");
			out.write(exception + "\r\n");
			for(StackTraceElement element: exception.getStackTrace()){
				out.write("	"+ element + "\r\n");
			}
			
			out.write( "------------------------------\r\n\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
