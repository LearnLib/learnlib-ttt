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
package de.learnlib.importers.aut;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import javax.annotation.WillClose;
import javax.annotation.WillCloseWhenClosed;
import javax.annotation.WillNotClose;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * Utility methods to import (gzipped) AUT files, as were used in the old version
 * of LearnLib.
 * 
 * @author Malte Isberner
 *
 */
public class AUTImporter {
	
	@WillCloseWhenClosed 
	public static InputStream uncompressed(@WillNotClose InputStream is) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(is);
		bis.mark(2);
		byte[] header = new byte[2];
		int read = bis.read(header);
		bis.reset();
		if(read >= 2) {
			int head = ((int)header[0] & 0xff) | ((header[1] << 8) & 0x0000ff00);
			if(head == GZIPInputStream.GZIP_MAGIC) {
				return new GZIPInputStream(bis);
			}
		}
		return bis;
	}
	
	
	public static CompactDFA<Integer> read(@WillClose InputStream is) throws IOException {
		try(Scanner sc = new Scanner(uncompressed(is))) {
			int numStates = sc.nextInt();
			int numSymbols = sc.nextInt();
			
			Alphabet<Integer> alphabet = Alphabets.integers(0, numSymbols - 1);
			
			CompactDFA<Integer> result = new CompactDFA<>(alphabet, numStates);
			
			// This is redundant in practice, but it is in fact not specified by CompactDFA
			// how state IDs are assigned
			int[] states = new int[numStates];
			
			
			// Parse states
			states[0] = result.addIntInitialState(sc.nextInt() != 0);
			
			for(int i = 1; i < numStates; i++) {
				states[i] = result.addIntState(sc.nextInt() != 0);
			}
			
			// Parse transitions
			for(int i = 0; i < numStates; i++) {
				int state = states[i];
				for(int j = 0; j < numSymbols; j++) {
					int succ = states[sc.nextInt()];
					result.setTransition(state, j, succ);
				}
			}
			
			return result;
		}
	}
}
