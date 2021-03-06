package org.glycoinfo.ChemicalStructureUtility.util.visitor;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.SubGraphOld;

/**
 * Interface for visitor of SubGraph
 * @author MasaakiMatsubara
 *
 */
public interface AtomicVisitor {
	public void visit( Atom       a_objAtom       ) throws AtomicVisitorException;
	public void visit( Connection a_objConnection ) throws AtomicVisitorException;

	public void start( SubGraphOld   a_objGraph      ) throws AtomicVisitorException;

	public AtomicTraverser getTraverser( AtomicVisitor a_objVisitor ) throws AtomicVisitorException;

	public void clear();

}
