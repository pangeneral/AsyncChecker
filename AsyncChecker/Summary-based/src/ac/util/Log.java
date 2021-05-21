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

package ac.util;

import ac.config.Configuration;

/**
 * 
 * @author Baoquan Cui
 * @version 1.0
 */
public class Log {

	private static final boolean DEBUG = Configuration.DEBUG;
	
	private static final boolean ERROR_DEBUG = true;

	public static void i(Object... objects) {
		if (DEBUG) {
			StringBuffer sb = new StringBuffer();
			if (objects == null) {
				sb.append("null");
			} else {
				for (Object obj : objects) {
					sb.append(obj);
				}
			}
			System.out.println(sb.toString());
		}
	}

	public static void e(Object... objects) {
		if (ERROR_DEBUG) {
			StringBuffer sb = new StringBuffer();
			if (objects == null) {
				sb.append("null");
			} else {
				for (Object obj : objects) {
					sb.append(obj);
				}
			}
			System.err.println(sb.toString());
		}
	}
}
