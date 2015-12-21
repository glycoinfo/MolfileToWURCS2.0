package org.glycoinfo.WURCSFramework.util.hierarchicaldigraph;

import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;

public class DigraphNode {

	private Atom m_oAtom;
	private DigraphEdge m_oParentEdge;
	private LinkedList<DigraphEdge> m_aChildEdges;

	public DigraphNode(Atom a_oAtom) {
		this.m_oAtom = a_oAtom;
	}

	public Atom getAtom() {
		return this.m_oAtom;
	}

	public DigraphEdge getParentEdge() {
		return this.m_oParentEdge;
	}

	public LinkedList<DigraphEdge> getChildEdges() {
		return this.m_aChildEdges;
	}
}
