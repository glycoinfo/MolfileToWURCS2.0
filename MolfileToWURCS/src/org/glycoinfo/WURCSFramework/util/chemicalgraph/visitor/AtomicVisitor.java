package org.glycoinfo.WURCSFramework.util.chemicalgraph.visitor;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;
import org.glycoinfo.WURCSFramework.chemicalgraph.SubGraph;

/**
 * Interface for visitor of SubGraph
 * @author MasaakiMatsubara
 *
 */
public interface AtomicVisitor {
	public void visit( Atom       a_objAtom       ) throws AtomicVisitorException;
	public void visit( Connection a_objConnection ) throws AtomicVisitorException;

	public void start( SubGraph   a_objGraph      ) throws AtomicVisitorException;

	public AtomicTraverser getTraverser( AtomicVisitor a_objVisitor ) throws AtomicVisitorException;

	public void clear();

}
