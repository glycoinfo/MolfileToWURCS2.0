package utility.visitor;

import chemicalgraph.Atom;
import chemicalgraph.ChemicalGraph;
import chemicalgraph.Connection;

/**
*
* @author Masaaki Matsubara
*
*/
public interface ChemicalGraphVisitor {

	public void visit( Atom       a_objAtom    ) throws ChemicalGraphVisitorException;
	public void visit( Connection a_objConnect ) throws ChemicalGraphVisitorException;

	public void start( ChemicalGraph a_objMolecule ) throws ChemicalGraphVisitorException;

	public ChemicalGraphVisitor getTraverser(ChemicalGraphVisitor a_objVisitor) throws ChemicalGraphVisitorException;

	public void clear();

}
