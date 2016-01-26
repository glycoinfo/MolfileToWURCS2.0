package org.glycoinfo.ChemicalStructureUtility.util.visitor;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.SubGraph;

public class AtomicVisitorStereoAnalyze implements AtomicVisitor {

	@Override
	public void visit(Atom a_objAtom) throws AtomicVisitorException {

	}

	@Override
	public void visit(Connection a_objConnection) throws AtomicVisitorException {

	}

	@Override
	public void start(SubGraph a_objGraph) throws AtomicVisitorException {

	}

	@Override
	public AtomicTraverser getTraverser(AtomicVisitor a_objVisitor) throws AtomicVisitorException {
		return null;
	}

	@Override
	public void clear() {

	}

}
