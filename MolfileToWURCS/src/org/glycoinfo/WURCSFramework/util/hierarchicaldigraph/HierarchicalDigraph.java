package org.glycoinfo.WURCSFramework.util.hierarchicaldigraph;

import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;

public class HierarchicalDigraph {

	/** Atom of this graph */
	private Atom m_oAtom;
	/** Average of atomic number(s) (for conjucate system) */
	private double m_dAverageAtomicNumber;
	/** Parent of this graph (null if this graph is root) */
	private HierarchicalDigraph m_oParentHD = null;
	/** Chidren of this graph */
	private LinkedList<HierarchicalDigraph> m_aChildren = new LinkedList<HierarchicalDigraph>();

}
