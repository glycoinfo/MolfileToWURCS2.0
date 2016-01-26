package org.glycoinfo.ChemicalStructureUtility.util.stereochemistry;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.ChemicalGraph;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;

public class HierarchicalDigraphCreatorForSubGraph extends HierarchicalDigraphCreator {

	private ChemicalGraph m_oGraph;

	public HierarchicalDigraphCreatorForSubGraph(ChemicalGraph a_oGraph, Connection a_oStart, int a_iDepth) {
		super(a_oStart, a_iDepth);
		this.m_oGraph = a_oGraph;
		// TODO 自動生成されたコンストラクター・スタブ
	}

	protected boolean isIgnoreAtom(Atom a_oAtom) {
		return (!a_oAtom.getSymbol().equals("H") && !this.m_oGraph.contains(a_oAtom));
	}
}
