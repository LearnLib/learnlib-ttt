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
