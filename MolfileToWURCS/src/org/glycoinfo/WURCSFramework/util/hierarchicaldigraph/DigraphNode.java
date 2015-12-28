package org.glycoinfo.WURCSFramework.util.hierarchicaldigraph;

import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;

public class DigraphNode {

	private Atom m_oAtom;
	private DigraphEdge m_oParentEdge = null;
	private LinkedList<DigraphEdge> m_aChildEdges = new LinkedList<DigraphEdge>();

	public DigraphNode(Atom a_oAtom) {
		this.m_oAtom = a_oAtom;
	}

	public Atom getAtom() {
		return this.m_oAtom;
	}

	public void setParentEdge(DigraphEdge a_oEdge) {
		this.m_oParentEdge = a_oEdge;
	}

	public DigraphEdge getParentEdge() {
		return this.m_oParentEdge;
	}

	public void addChildEdge(DigraphEdge a_oEdge) {
		this.m_aChildEdges.add(a_oEdge);
	}

	public LinkedList<DigraphEdge> getChildEdges() {
		return this.m_aChildEdges;
	}
}
