package org.glycoinfo.ChemicalStructureUtility.util.stereochemistry;

import java.util.Collections;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;

public class HierarchicalDigraphNode {

	/** Connection to this graph */
	private Connection m_oConnection;
	/** Average of atomic number(s) (for conjucate system) */
	private double m_dAverageAtomicNumber;
	/** Chidren of this graph */
	private LinkedList<HierarchicalDigraphNode> m_aChildren = new LinkedList<HierarchicalDigraphNode>();

	public HierarchicalDigraphNode(Connection a_oConn, double a_dAveNum) {
		this.m_oConnection = a_oConn;
		this.m_dAverageAtomicNumber = a_dAveNum;
	}

	public Connection getConnection() {
		return this.m_oConnection;
	}

	public double getAverageAtomicNumber() {
		return this.m_dAverageAtomicNumber;
	}

	public void addChild(HierarchicalDigraphNode a_oChild) {
		this.m_aChildren.addLast(a_oChild);
	}

	public LinkedList<HierarchicalDigraphNode> getChildren() {
		return this.m_aChildren;
	}

	public void sortChildren( HierarchicalDigraphComparator a_oHDComp ) {
		if ( this.m_aChildren.isEmpty() ) return;
		for ( HierarchicalDigraphNode t_oChild : this.m_aChildren ) {
			t_oChild.sortChildren(a_oHDComp);
		}
		Collections.sort(this.m_aChildren, a_oHDComp);
	}
}
