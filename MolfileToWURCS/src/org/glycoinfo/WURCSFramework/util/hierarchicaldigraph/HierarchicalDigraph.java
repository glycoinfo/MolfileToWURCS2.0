package org.glycoinfo.WURCSFramework.util.hierarchicaldigraph;

import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;

public class HierarchicalDigraph {

	/** Connection to this graph */
	private Connection m_oConnection;
	/** Atom of this graph */
//	private Atom m_oAtom;
	/** Average of atomic number(s) (for conjucate system) */
	private double m_dAverageAtomicNumber;
	/** Parent of this graph (null if this graph is root) */
	private HierarchicalDigraph m_oParentHD = null;
	/** Chidren of this graph */
	private LinkedList<HierarchicalDigraph> m_aChildren = new LinkedList<HierarchicalDigraph>();

	public HierarchicalDigraph(HierarchicalDigraph a_oParent, Connection a_oConn, double a_dAveNum) {
		this.m_oParentHD = a_oParent;
		this.m_oConnection = a_oConn;
		this.m_dAverageAtomicNumber = a_dAveNum;
//		this.m_oAtom = a_oConn.endAtom();
	}

	public Connection getConnection() {
		return this.m_oConnection;
	}

	public Connection getConnectionToParent() {
		if ( this.m_oConnection == null ) return null;
		return this.m_oConnection.getReverse();
	}

	public double getAverageAtomicNumber() {
		return this.m_dAverageAtomicNumber;
	}

	public void addChild(HierarchicalDigraph a_oChild) {
		this.m_aChildren.addLast(a_oChild);
	}

	public LinkedList<HierarchicalDigraph> getChildren() {
		return this.m_aChildren;
	}
}
