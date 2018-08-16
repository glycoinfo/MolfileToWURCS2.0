package org.glycoinfo.WURCSFramework.util.exchange;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.WURCSFramework.buildingblock.SubMolecule;
import org.glycoinfo.WURCSFramework.util.comparator.BackboneCarbonComparator;
import org.glycoinfo.WURCSFramework.util.comparator.ConnectionComparatorForMAPGraph;
import org.glycoinfo.WURCSFramework.wurcs.map.MAPAtom;
import org.glycoinfo.WURCSFramework.wurcs.map.MAPAtomAbstract;
import org.glycoinfo.WURCSFramework.wurcs.map.MAPAtomCyclic;
import org.glycoinfo.WURCSFramework.wurcs.map.MAPBondType;
import org.glycoinfo.WURCSFramework.wurcs.map.MAPConnection;
import org.glycoinfo.WURCSFramework.wurcs.map.MAPGraph;
import org.glycoinfo.WURCSFramework.wurcs.map.MAPStar;
import org.glycoinfo.WURCSFramework.wurcs.map.MAPStereo;

public class SubMoleculeToMAPGraph {

	private SubMolecule m_oSubMol;
	private MAPGraph m_oMAPGraph;
	private HashMap<Atom, Integer> m_mapBackboneCarbonToStarIndex;
	private StringBuffer m_sbLog = new StringBuffer();


	public SubMoleculeToMAPGraph(final SubMolecule a_oSubMol) {
		this.m_oSubMol = a_oSubMol;
		this.m_mapBackboneCarbonToStarIndex = new HashMap<Atom, Integer>();
		this.m_oMAPGraph = new MAPGraph();
	}

	public MAPGraph getMAPGraph() {
		return this.m_oMAPGraph;
	}

	/**
	 * Get Star Index from Backbone carbon
	 * @param a_oCarbon Backbone carbon
	 * @return Star Index (-1 if a_oCarbon is not Backbone carbon in the SubMolecule)
	 */
	public int getStarIndexFromBackboneCarbon(Atom a_oCarbon) {
		if ( !this.m_mapBackboneCarbonToStarIndex.containsKey(a_oCarbon) )
			return -1;
		return this.m_mapBackboneCarbonToStarIndex.get(a_oCarbon);
	}

	public HashMap<Atom, Integer> getBackboneCarbonToStarIndex() {
		return this.m_mapBackboneCarbonToStarIndex;
	}

	public void start() {
		// Calc Star Index
		LinkedList<Atom> t_aBackboneCarbons = this.orderBackboneCarbons();
		// Convert connections to MAPGraph
		MAPGraph t_oMAPGraph = new MAPGraph();

		if (t_aBackboneCarbons.size() > 0) {
			// Order connections by traverse SubMolecule
			Atom t_oGraphStart = t_aBackboneCarbons.getFirst();
			LinkedList<Connection> t_aOrderedConnections = this.orderConnections(t_oGraphStart);

			HashMap<Atom, MAPAtomAbstract> t_mapAtomToMAPAtom = new HashMap<Atom, MAPAtomAbstract>();

			// Convert connections to MAPGraph
			//MAPGraph t_oMAPGraph = new MAPGraph();

			// Set first atom to MAPGraph
			MAPStar t_oHeadStar = new MAPStar();
			t_oHeadStar.setStarIndex( this.m_mapBackboneCarbonToStarIndex.get(t_oGraphStart) );
			t_oMAPGraph.addAtom(t_oHeadStar);
			MAPAtomAbstract t_oPrevMAPAtom = t_oHeadStar;
			t_mapAtomToMAPAtom.put(t_oGraphStart, t_oHeadStar);

			for ( Connection t_oConn : t_aOrderedConnections ) {

				Atom t_oEnd = t_oConn.endAtom();

				MAPAtomAbstract t_oMAPAtom = null;

				// New default MAPAtom
				MAPAtom t_oDefault = new MAPAtom( t_oEnd.getSymbol() );
				// Set chiral
				String t_strChiral = t_oEnd.getChirality();
				MAPStereo t_enumChiral = ( "R".equals(t_strChiral) )? MAPStereo.RECTUS   :
										 ( "S".equals(t_strChiral) )? MAPStereo.SINISTER :
										 ( "X".equals(t_strChiral) )? MAPStereo.UNKNOWN  :
										 null;
				t_oDefault.setStereo(t_enumChiral);
				t_oMAPAtom = t_oDefault;

				// For cyclic
				if ( t_mapAtomToMAPAtom.containsKey(t_oEnd) ) {
					MAPAtomAbstract t_oCyclic = t_mapAtomToMAPAtom.get(t_oEnd);
					t_oMAPAtom = new MAPAtomCyclic( t_oCyclic );
				}

				// For Backbone carbons
				if ( t_aBackboneCarbons.contains(t_oEnd) ) {
					MAPStar t_oMAPStar = new MAPStar();
					t_oMAPStar.setStarIndex( this.m_mapBackboneCarbonToStarIndex.get(t_oEnd) );
					t_oMAPAtom = t_oMAPStar;
				}

				// For branch
				if ( !t_oPrevMAPAtom.equals( t_mapAtomToMAPAtom.get( t_oConn.startAtom() ) ) )
					t_oPrevMAPAtom = t_mapAtomToMAPAtom.get( t_oConn.startAtom() );

				// Set aromatic
				if ( t_oEnd.isAromatic() ) t_oMAPAtom.setAromatic();

				// Map Atom to MAPAtom
				if ( !( t_oMAPAtom instanceof MAPAtomCyclic ) )
					t_mapAtomToMAPAtom.put(t_oEnd, t_oMAPAtom);

				// For connections
				MAPBondType t_enumBondType = MAPBondType.SINGLE;
				if ( t_oConn.getBond().getType() == 2 )
					t_enumBondType = MAPBondType.DOUBLE;
				if ( t_oConn.getBond().getType() == 3 )
					t_enumBondType = MAPBondType.TRIPLE;
				if ( t_oConn.getBond().getType() == 4 )
					t_enumBondType = MAPBondType.AROMATIC;

				// For cis-trans
				String t_strGeometric = t_oConn.getBond().getGeometric();
				MAPStereo t_enumBondStereo = ( "Z".equals(t_strGeometric) )? MAPStereo.CIS     :
											 ( "E".equals(t_strGeometric) )? MAPStereo.TRANCE  :
											 ( "X".equals(t_strGeometric) )? MAPStereo.UNKNOWN :
											 null;

				// For connections
				MAPConnection t_oChildConn  = new MAPConnection(t_oMAPAtom);
				MAPConnection t_oParentConn = new MAPConnection(t_oPrevMAPAtom);

				t_oChildConn.setBondType(t_enumBondType);
				t_oParentConn.setBondType(t_enumBondType);
				t_oChildConn.setStereo(t_enumBondStereo);
				t_oParentConn.setStereo(t_enumBondStereo);

				t_oPrevMAPAtom.addChildConnection(t_oChildConn);
				t_oMAPAtom.setParentConnection(t_oParentConn);

				// Set atom to star
				if ( t_oPrevMAPAtom instanceof MAPStar )
					((MAPStar)t_oPrevMAPAtom).setConnection( t_oPrevMAPAtom.getConnections().getFirst() );

				// Set previous MAPAtom
				t_oPrevMAPAtom = t_oMAPAtom;

				// Add atom
				t_oMAPGraph.addAtom(t_oMAPAtom);
			}
		}

		this.m_oMAPGraph = t_oMAPGraph;
	}

	/**
	 * Order Backbone carbons and assign Star Index to Backbone carbons
	 * @return LinkedList of ordered Backbone carbons
	 */
	private LinkedList<Atom> orderBackboneCarbons() {

		// Sort Backbones
		LinkedList<Atom> t_aBackboneCarbons = this.m_oSubMol.getBackboneCarbons();

		if (t_aBackboneCarbons.size() > 0) {
			BackboneCarbonComparator t_oBCComp = new BackboneCarbonComparator(this.m_oSubMol);
			Collections.sort(t_aBackboneCarbons, t_oBCComp);

			// Calc Star Index
			int t_iStarIndex = 1;
			this.m_mapBackboneCarbonToStarIndex.put( t_aBackboneCarbons.getFirst(), t_iStarIndex );

			int t_nCarbons = t_aBackboneCarbons.size();
			for ( int i=0 ; i<t_nCarbons-1; i++ ) {

				Atom t_oCi = t_aBackboneCarbons.get(i);
				Atom t_oCj = t_aBackboneCarbons.get(i+1);

				int t_iComp = t_oBCComp.compare(t_oCi, t_oCj);
				if ( t_iComp != 0 ) t_iStarIndex++;
				this.m_mapBackboneCarbonToStarIndex.put(t_oCj, t_iStarIndex);
			}
			// Set Star Index 0 when all carbons are same order
			if ( t_iStarIndex == 1 )
				for ( Atom t_oC : t_aBackboneCarbons )
					this.m_mapBackboneCarbonToStarIndex.put(t_oC, 0);

			// Log result Star Index
			for ( Atom t_oC : t_aBackboneCarbons ) {
				t_iStarIndex =this.m_mapBackboneCarbonToStarIndex.get(t_oC);
				String t_strC = t_oC.getSymbol()+"("+t_oC.getAtomID()+")";
			this.m_sbLog.append( t_strC+": "+t_iStarIndex+"\n" );
			}
		}

		return t_aBackboneCarbons;
	}

	/**
	 * Order connections started from a_oStart
	 * @param a_oStart Start atom to traverse
	 * @return Ordered connections
	 */
	private LinkedList<Connection> orderConnections(Atom a_oStart) {
		// Traverse atoms

		LinkedList<Connection> t_aTraversedConnections = new LinkedList<Connection>();
		LinkedList<Atom> t_aTraversedAtoms = new LinkedList<Atom>();
		t_aTraversedAtoms.addLast(a_oStart);
		t_aTraversedConnections.addLast( this.m_oSubMol.getConnectionFromBackbone(a_oStart) );

		ConnectionComparatorForMAPGraph t_oConnComp = new ConnectionComparatorForMAPGraph(this.m_oSubMol);

		LinkedList<Connection> t_aSelectedConnections = new LinkedList<Connection>();
		while (true) {
			Connection t_oTailConn = t_aTraversedConnections.getLast();

			// Get neighbour connections
			for ( Connection t_oConn : t_oTailConn.endAtom().getConnections() ) {
				// Ignore stored connections
				if ( t_aTraversedConnections.contains(t_oConn) ) continue;
				if ( t_aTraversedConnections.contains(t_oConn.getReverse()) ) continue;
				if ( t_aSelectedConnections.contains(t_oConn) ) continue;
				if ( t_aSelectedConnections.contains(t_oConn.getReverse()) ) continue;
				// Ignore hydrogens
				if ( t_oConn.endAtom().getSymbol().equals("H") ) continue;

				t_aSelectedConnections.add(t_oConn);
			}

			// Break if no connections for searching
			if(t_aSelectedConnections.size()==0) break;

			// Reverse connection if the start atom is not nearer from tail atom than end atom
			for ( Connection t_oConn : t_aSelectedConnections ) {
				int t_iStartAtomNum = t_aTraversedAtoms.indexOf( t_oConn.startAtom() );
				int t_iEndAtomNum = t_aTraversedAtoms.indexOf( t_oConn.endAtom() );
				if ( t_iStartAtomNum==-1 || t_iEndAtomNum==-1 ) continue;
				if ( t_iStartAtomNum >= t_iEndAtomNum ) continue;
				// Reverse connection
				int t_iRevNum = t_aSelectedConnections.indexOf(t_oConn);
				t_aSelectedConnections.set(t_iRevNum, t_oConn.getReverse());
			}

			// Sort stored connections
			t_oConnComp.addTailConnection( t_oTailConn );
			Collections.sort( t_aSelectedConnections, t_oConnComp );

			// Store the most prioritize connection
			Connection t_oSelectedConn = t_aSelectedConnections.removeFirst();
			t_aTraversedConnections.addLast(t_oSelectedConn);
			t_aTraversedAtoms.addLast(t_oSelectedConn.endAtom());
		}

		return t_aTraversedConnections;
	}
}
