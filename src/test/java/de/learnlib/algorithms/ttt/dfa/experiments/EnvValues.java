/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib-TTT, https://github.com/LearnLib/learnlib-ttt/
 * 
 * LearnLib-TTT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * LearnLib-TTT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with LearnLib-TTT.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.learnlib.algorithms.ttt.dfa.experiments;

public class EnvValues {
	
	public static String getString(String name, String defaultValue) {
		String val = defaultValue;
		String strVal = System.getenv(name);
		if(strVal != null) {
			val = strVal;
		}
		return val;
	}
	
	public static String[] getStringArray(String name, String... defaultValue) {
		String[] val = defaultValue;
		String strVal = System.getenv(name);
		if(strVal != null) {
			val = strVal.trim().split("\\s*,\\s*");
		}
		return val;
	}
	
	
	public static int getInt(String name, int defaultValue) {
		int intVal = defaultValue;
		
		String strVal = System.getenv(name);
		
		if(strVal != null) {
			try {
				intVal = Integer.parseInt(strVal);
			}
			catch(NumberFormatException ex) {}
		}
		
		return intVal;
	}
	
	public static long getLong(String name, long defaultValue) {
		long longVal = defaultValue;
		
		String strVal = System.getenv(name);
		
		if(strVal != null) {
			try {
				longVal = Long.parseLong(strVal);
			}
			catch(NumberFormatException ex) {}
		}
		
		return longVal;
	}

}
