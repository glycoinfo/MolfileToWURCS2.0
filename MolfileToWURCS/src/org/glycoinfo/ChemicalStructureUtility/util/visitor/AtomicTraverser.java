package org.glycoinfo.ChemicalStructureUtility.util.visitor;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.SubGraph;

/**
 * Abstract class for traverser of SubGraph
 * @author matsubara
 *
 */
public abstract class AtomicTraverser {

	public static final int ENTER 	= 0;
	public static final int LEAVE 	= 1;
	public static final int RETURN 	= 2;

	protected AtomicVisitor m_objVisitor = null;
	protected int m_iState = 0;

	public AtomicTraverser ( AtomicVisitor a_objVisitor ) throws AtomicVisitorException
	{
		if ( a_objVisitor == null )
		{
			throw new AtomicVisitorException("Null visitor given to traverser");
		}
		this.m_objVisitor = a_objVisitor;
	}

	public abstract void traverse( Atom a_objAtom ) throws AtomicVisitorException;
	public abstract void traverse( Connection a_objConnection ) throws AtomicVisitorException;

	public abstract void traverseGraph( SubGraph a_objGraph ) throws AtomicVisitorException;

	public int getState()
	{
		return this.m_iState;
	}
}
