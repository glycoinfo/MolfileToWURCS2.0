package org.glycoinfo.ChemicalStructureUtility.util.stereochemistry;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.SubGraph;

public class StereochemistryAnalysisForSubGraph extends StereochemistryAnalysis {

	private SubGraph m_bSubGraph;

	public void start( SubGraph a_oGraph ) {
		this.m_bSubGraph = a_oGraph;
		super.start(a_oGraph);
	}

	protected HierarchicalDigraphCreator getHierarchicalDigraphCreator( Connection a_oConnection, int a_iDepth ) {
		return new HierarchicalDigraphCreatorForSubGraph( this.m_bSubGraph, a_oConnection, a_iDepth );
	}
}
