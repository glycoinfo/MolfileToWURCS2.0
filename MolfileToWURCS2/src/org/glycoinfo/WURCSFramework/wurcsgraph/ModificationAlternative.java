package org.glycoinfo.WURCSFramework.wurcsgraph;

import java.util.LinkedList;

public class ModificationAlternative extends Modification {

	private LinkedList<WURCSEdge> m_aReadInEdges  = new LinkedList<WURCSEdge>();
	private LinkedList<WURCSEdge> m_aReadOutEdges = new LinkedList<WURCSEdge>();

	public ModificationAlternative(String MAPCode) {
		super(MAPCode);
	}

	public void addReadInEdge(WURCSEdge a_oInEdge) {
		this.m_aReadInEdges.addLast(a_oInEdge);
	}

	public LinkedList<WURCSEdge> getReadInEdges() {
		return this.m_aReadInEdges;
	}

	public void addReadOutEdge(WURCSEdge a_oOutEdge) {
		this.m_aReadOutEdges.addLast(a_oOutEdge);
	}

	public LinkedList<WURCSEdge> getReadOutEdges() {
		return this.m_aReadOutEdges;
	}

}
