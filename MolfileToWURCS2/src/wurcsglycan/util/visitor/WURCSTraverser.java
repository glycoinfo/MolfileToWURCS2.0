package wurcsglycan.util.visitor;

import wurcsglycan.Backbone;
import wurcsglycan.Modification;
import wurcsglycan.WURCSEdge;
import wurcsglycan.WURCSGlycan;

public abstract class WURCSTraverser {
	public static final int ENTER 	= 0;
	public static final int LEAVE 	= 1;
	public static final int RETURN 	= 2;

	protected WURCSVisitor m_objVisitor = null;
	protected int m_iState = 0;


	public WURCSTraverser ( WURCSVisitor a_objVisitor ) throws WURCSVisitorException
	{
		if ( a_objVisitor == null )
		{
			throw new WURCSVisitorException("Null visitor given to traverser");
		}
		this.m_objVisitor = a_objVisitor;
	}

	public abstract void traverse( Backbone     a_objBackbone     ) throws WURCSVisitorException;
	public abstract void traverse( Modification a_objModification ) throws WURCSVisitorException;
	public abstract void traverse( WURCSEdge    a_objEdge         ) throws WURCSVisitorException;

	public abstract void traverseGraph( WURCSGlycan a_objGlycan ) throws WURCSVisitorException;

	public int getState()
	{
		return this.m_iState;
	}

}
