package utility.traverser;

import utility.visitor.ChemicalGraphVisitor;
import utility.visitor.ChemicalGraphVisitorException;
import chemicalgraph.Atom;
import chemicalgraph.ChemicalGraph;
import chemicalgraph.Connection;

/**
 *
 * @author Masaaki Matsubara
 *
 */
public abstract class ChemicalGraphTraverser {
	public static final int ENTER 	= 0;
	public static final int LEAVE 	= 1;
	public static final int RETURN 	= 2;

	protected ChemicalGraphVisitor m_objVisitor = null;
	protected int m_iState = 0;


	public ChemicalGraphTraverser ( ChemicalGraphVisitor a_objVisitor ) throws ChemicalGraphVisitorException
	{
        if ( a_objVisitor == null )
        {
            throw new ChemicalGraphVisitorException("Null visitor given to traverser");
        }
        this.m_objVisitor = a_objVisitor;
	}

    public abstract void traverse( Atom a_objAtom ) throws ChemicalGraphVisitorException;
	public abstract void traverse( Connection a_objConnect ) throws ChemicalGraphVisitorException;

	public abstract void traverseGraph( ChemicalGraph a_objSugar ) throws ChemicalGraphVisitorException;

	public int getState()
	{
		return this.m_iState;
	}

}
