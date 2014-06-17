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
package de.learnlib.algorithms.ttt.dfa;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.google.common.base.Joiner;

import de.learnlib.algorithms.ttt.dfa.experiments.EnvValues;

public class TTTExperiment {

	public static class Config {
		public String targetSystem;
		public int ceLengthMin;
		public int ceLengthMax;
		public int ceLengthStep;
		public int repeatCount;
		
		public int numThreads;
		public String[] learners;
		public String outputName;
		public long seed;
		
		public void readValues() {
			targetSystem = EnvValues.getString("TARGET_SYSTEM", "sched4");
			ceLengthMin = EnvValues.getInt("CE_LENGTH_MIN", 50);
			ceLengthMax = EnvValues.getInt("CE_LENGTH_MAX", 3000);
			ceLengthStep = EnvValues.getInt("CE_LENGTH_STEP", 50);
			repeatCount = EnvValues.getInt("REPEAT_COUNT", 10);
			
			numThreads = EnvValues.getInt("NUM_THREADS", Runtime.getRuntime().availableProcessors());
			
			learners = EnvValues.getStringArray("LEARNERS", "TTT");
			
			outputName = EnvValues.getString("OUTPUT_NAME", new SimpleDateFormat("YYYY-MM-dd-HHmmss").format(new Date()));
			
			seed = EnvValues.getLong("RANDOM_SEED", System.currentTimeMillis());
		}
		
		public void print(PrintStream ps) {
			ps.println("TARGET_SYSTEM = " + targetSystem);
			ps.println("CE_LENGTH_MIN = " + ceLengthMin);
			ps.println("CE_LENGTH_MAX = " + ceLengthMax);
			ps.println("CE_LENGTH_STEP = " + ceLengthStep);
			ps.println("REPEAT_COUNT = " + repeatCount);
			ps.println("NUM_THREADS = " + numThreads);
			ps.println("LEARNERS = " + Joiner.on(',').join(Arrays.asList(learners)));
			ps.println("OUTPUT_NAME = " + outputName);
			ps.println("RANDOM_SEED = " + seed);
		}
	}
	
	public static void main(String[] args) throws Exception {
		Config config = new Config();
		config.readValues();
		System.out.println("Configuration:");
		config.print(System.out);
		
		ExperimentRunner runner = new ExperimentRunner(config);
		
		runner.run();
		
	}
	
}
