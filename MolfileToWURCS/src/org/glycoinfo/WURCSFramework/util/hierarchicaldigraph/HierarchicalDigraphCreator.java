package org.glycoinfo.WURCSFramework.util.hierarchicaldigraph;

import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.ChemicalGraph;
import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;
import org.glycoinfo.WURCSFramework.util.chemicalgraph.Chemical;


public class HierarchicalDigraphCreator {

	private ChemicalGraph m_oGraph;
	private HierarchicalDigraph m_oRootHD;
	private boolean m_bIsCompletedFullSearch = true;

	public HierarchicalDigraphCreator( ChemicalGraph a_oGraph ) {
		this.m_oGraph = a_oGraph;
	}

	public HierarchicalDigraph getHierarchicalDigraph() {
		return this.m_oRootHD;
	}

	public boolean isCompletedFullSearch() {
		return this.m_bIsCompletedFullSearch;
	}

	public void start(Connection a_oStart, int a_iDepth) {
		this.m_oRootHD = new HierarchicalDigraph( null, a_oStart, Chemical.getAtomicNumber(a_oStart.endAtom().getSymbol()) );
		this.m_bIsCompletedFullSearch = this.depthSearch( this.m_oRootHD, a_iDepth, new LinkedList<Atom>() );
	}

	/**
	 * Construct HierarchicalDigraph using depth search
	 * @param a_oHD Current HierarchicalDigraph
	 * @param a_iDepth Depth of search limit
	 * @param a_aAncestors list of ancestor atoms
	 * @return
	 */
	private boolean depthSearch(HierarchicalDigraph a_oHD, int a_iDepth, LinkedList<Atom> a_aAncestors) {
		Atom t_oAtom = a_oHD.getConnection().endAtom();
		if ( !t_oAtom.getSymbol().equals("H") && !this.m_oGraph.contains(t_oAtom) ) return true;
		if ( a_aAncestors.contains( t_oAtom ) ) return true;
		if ( a_iDepth == 0 ) return false;

		a_aAncestors.addLast(t_oAtom);

		boolean t_bIsCompletedFullSearch = true;

		// Add children
		int t_nAromaticConnection = 0;
		double t_nSumAtomicNumber = 0;
		for ( Connection t_oConn : t_oAtom.getConnections() ) {
			// Ignore out of subgraph except for hydrogen
			if ( !t_oConn.endAtom().getSymbol().equals("H") && !this.m_oGraph.contains( t_oConn.endAtom() ) ) return true;

			double t_iAtomicNumber = (double)Chemical.getAtomicNumber(t_oConn.endAtom().getSymbol());

			// For aromatic atoms
			if ( t_oAtom.isAromatic() || t_oConn.endAtom().isAromatic() ) {
				t_nAromaticConnection++;
				t_nSumAtomicNumber += t_iAtomicNumber;
			}

			// Add child digraph as duplicated atoms for multiple connection
			int t_iBondType = t_oConn.getBond().getType();
			if ( t_iBondType > 3 || t_iBondType < 2 ) continue;
			a_oHD.addChild( new HierarchicalDigraph( a_oHD, null, t_iAtomicNumber ) );
			if ( t_iBondType < 3 ) continue;
			a_oHD.addChild( new HierarchicalDigraph( a_oHD, null, t_iAtomicNumber ) );

			// Depth search for child digraph except for reverse connection
			if ( t_oConn.equals( a_oHD.getConnectionToParent() ) ) continue;
			HierarchicalDigraph t_oChildHD = new HierarchicalDigraph( a_oHD, t_oConn, t_iAtomicNumber );
			// Set false to full search flag reaching maximum depth
			if ( !this.depthSearch(t_oChildHD, a_iDepth - 1, a_aAncestors) )
				t_bIsCompletedFullSearch = false;
		}
		// Add duplicated atom for
		if ( t_nAromaticConnection != 0 )
			a_oHD.addChild( new HierarchicalDigraph( a_oHD, null, t_nSumAtomicNumber/(double)t_nAromaticConnection ) );

		a_aAncestors.removeLast();

		return t_bIsCompletedFullSearch;
	}

	public void sortDigraph( ) {
//		if (  )
	}
}
