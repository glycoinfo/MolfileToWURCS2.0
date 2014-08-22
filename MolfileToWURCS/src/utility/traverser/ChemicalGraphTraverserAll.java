package utility.traverser;

import utility.visitor.ChemicalGraphVisitor;
import utility.visitor.ChemicalGraphVisitorException;
import chemicalgraph.Atom;
import chemicalgraph.AtomList;
import chemicalgraph.ChemicalGraph;
import chemicalgraph.Connection;

public class ChemicalGraphTraverserAll extends ChemicalGraphTraverser {
	private AtomList m_aVisitedAtoms = new AtomList();

	public ChemicalGraphTraverserAll(ChemicalGraphVisitor a_objVisitor) throws ChemicalGraphVisitorException {
		super(a_objVisitor);
	}

	public void traverse(Atom a_objAtom) throws ChemicalGraphVisitorException
	{
		this.m_iState = ChemicalGraphTraverser.ENTER;
		a_objAtom.accept(this.m_objVisitor);
		for ( Connection connect : a_objAtom.connections ) {
			this.traverse(connect);
		}
		this.m_aVisitedAtoms.add(a_objAtom);
	}

	public void traverse(Connection a_objConnect) throws ChemicalGraphVisitorException
	{
		this.m_iState = ChemicalGraphTraverser.ENTER;
		a_objConnect.accept(this.m_objVisitor);
		if ( this.m_aVisitedAtoms.contains(a_objConnect.atom) ) return;
		this.traverse(a_objConnect.atom);
	}

	public void traverseGraph(ChemicalGraph a_objMolecule) throws ChemicalGraphVisitorException
	{
		for ( Atom t_objAtom : a_objMolecule.atoms ) {
			if ( this.m_aVisitedAtoms.contains(t_objAtom) ) continue;
			this.traverse(t_objAtom);
			
		}
	}

}
