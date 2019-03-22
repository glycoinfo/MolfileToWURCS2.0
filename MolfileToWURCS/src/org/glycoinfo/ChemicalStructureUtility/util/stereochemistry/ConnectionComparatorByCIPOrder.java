package org.glycoinfo.ChemicalStructureUtility.util.stereochemistry;

import java.util.Comparator;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;

/**
 * Comparator class for coonections by CIP order
 * @author matsubara
 *
 */
public class ConnectionComparatorByCIPOrder implements Comparator<Connection> {

	protected HierarchicalDigraphComparator m_oHDComp;
	private AtomicNumberCalculator m_oANumCalc;

	public ConnectionComparatorByCIPOrder(HierarchicalDigraphComparator a_oHDComp, AtomicNumberCalculator a_oANumCalc) {
		this.m_oHDComp = a_oHDComp;
		this.m_oANumCalc = a_oANumCalc;
	}

	public ConnectionComparatorByCIPOrder(HierarchicalDigraphComparator a_oHDComp) {
		this.m_oHDComp = a_oHDComp;
		this.m_oANumCalc = new AtomicNumberCalculator();
	}

	public int compare(Connection a_oConnection1, Connection a_oConnection2) {

		// Compare by CIP order using HierarchicalDigraph with iterative deepening depth-first search
		int t_iDepth = 1;
		while ( true ) {
			// Create hierarchical digraph starting from connection
			HierarchicalDigraphCreator t_oHDCreate1 = this.getHDCreator(a_oConnection1, t_iDepth);
			HierarchicalDigraphCreator t_oHDCreate2 = this.getHDCreator(a_oConnection2, t_iDepth);

			HierarchicalDigraphNode t_oHD1 = t_oHDCreate1.getHierarchicalDigraph();
			HierarchicalDigraphNode t_oHD2 = t_oHDCreate2.getHierarchicalDigraph();
			t_oHD1.sortChildren(this.m_oHDComp);
			t_oHD2.sortChildren(this.m_oHDComp);
			// Compare CIP orders using HierarchicalDigraph
//			int t_iComp = this.compareHierarchicalDigraph(t_oHD1, t_oHD2);
			int t_iComp = this.m_oHDComp.compare(t_oHD1, t_oHD2);
			if ( t_iComp != 0 ) return t_iComp;
			if ( this.m_oHDComp.foundSameBranch() ) return 0;

			// Return if full search has completed
			if ( t_oHDCreate1.isCompletedFullSearch() && t_oHDCreate2.isCompletedFullSearch() ) return 0;

			t_iDepth++;
		}

	}
/*
	protected int compareHierarchicalDigraph( HierarchicalDigraphNode a_oHD1, HierarchicalDigraphNode a_oHD2 ) {
		return this.m_oHDComp.compare(a_oHD1, a_oHD2);
	}
*/
	private HierarchicalDigraphCreator getHDCreator(Connection a_oConnection, int a_iDepth) {
		HierarchicalDigraphCreator t_oHDCreator = new HierarchicalDigraphCreator(this.m_oANumCalc);
		t_oHDCreator.start(a_oConnection, a_iDepth);
		return t_oHDCreator;
	}
}
