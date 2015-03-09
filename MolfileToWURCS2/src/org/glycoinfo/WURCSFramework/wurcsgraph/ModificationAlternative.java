package org.glycoinfo.WURCSFramework.wurcsgraph;

import java.util.LinkedList;

public class ModificationAlternative extends Modification {

	private LinkedList<WURCSEdge> m_aLeadInEdges  = new LinkedList<WURCSEdge>();
	private LinkedList<WURCSEdge> m_aLeadOutEdges = new LinkedList<WURCSEdge>();

	public ModificationAlternative(String MAPCode) {
		super(MAPCode);
	}

	@Override
	public LinkedList<WURCSEdge> getEdges() {
		return this.m_aLeadOutEdges;
	}

	public void addLeadInEdge(WURCSEdge a_oInEdge) {
		this.m_aLeadInEdges.addLast(a_oInEdge);
	}

	public LinkedList<WURCSEdge> getLeadInEdges() {
		return this.m_aLeadInEdges;
	}

	public void addLeadOutEdge(WURCSEdge a_oOutEdge) {
		this.m_aLeadOutEdges.addLast(a_oOutEdge);
	}

	public LinkedList<WURCSEdge> getLeadOutEdges() {
		return this.m_aLeadOutEdges;
	}

}
