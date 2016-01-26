package org.glycoinfo.ChemicalStructureUtility.util.visitor;

import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.SubGraph;

/**
 * Class for ALIN code generator from SubGraph using visitor pattern
 * @author MasaakiMatsubara
 *
 */
public class SubGraphToAtomicPath implements AtomicVisitor {
	private SubGraph m_objGraph;
	private LinkedList<Connection> m_aConnections = new LinkedList<Connection>();

	private HashSet<Atom> m_aBackboneCarbons;
	private HashSet<Atom> m_aAromaticAtoms;

	/**
	 * Constructor
	 * @param carbons Backbone carbons
	 * @param aromatics Aromatic atoms
	 */
	public SubGraphToAtomicPath(final HashSet<Atom> carbons, final HashSet<Atom> aromatics) {
		this.m_aBackboneCarbons = carbons;
		this.m_aAromaticAtoms = aromatics;
	}

	@Override
	public void visit( Atom a_objAtom ) throws AtomicVisitorException {
		if ( !this.m_objGraph.contains(a_objAtom) ) return;


	}

	@Override
	public void visit( Connection a_objConnection ) throws AtomicVisitorException {
		if ( !this.m_objGraph.contains(a_objConnection.getBond()) ) return;

	}

	@Override
	public void start(SubGraph a_objGraph) throws AtomicVisitorException {
		this.clear();

		this.m_objGraph = a_objGraph;
		AtomicTraverser t_objTraverser = this.getTraverser(this);
		t_objTraverser.traverseGraph(this.m_objGraph);
	}

	@Override
	public AtomicTraverser getTraverser(AtomicVisitor a_objVisitor) throws AtomicVisitorException {
		return new AtomicTraverserConnection(a_objVisitor);
	}

	@Override
	public void clear() {
		this.m_aConnections.clear();
	}

}
