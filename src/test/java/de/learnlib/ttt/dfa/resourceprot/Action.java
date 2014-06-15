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
package de.learnlib.ttt.dfa.resourceprot;

import net.automatalib.commons.util.nid.AbstractMutableNumericID;

public class Action extends AbstractMutableNumericID {
	
	public static enum Type {
		OPEN,
		CLOSE,
		READ,
		WRITE,
		CHMOD_RO,
		CHMOD_RW,
		;
		
	}
	
	public final Type type;
	public final int resourceId;
	
	public Action(Type type, int rid) {
		this.type = type;
		this.resourceId = rid;
	}
	
	public String toString() {
		return type.toString().toLowerCase() + "(" + resourceId + ")";
	}

}
