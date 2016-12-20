package org.glycoinfo.WURCSFramework.util.exchange;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Molecule;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.SubGraphOld;
import org.glycoinfo.ChemicalStructureUtility.util.analytical.MoleculeNormalizer;
import org.glycoinfo.ChemicalStructureUtility.util.analytical.StructureAnalyzer;
import org.glycoinfo.ChemicalStructureUtility.util.stereochemistry.StereochemistryAnalysis;
import org.glycoinfo.WURCSFramework.buildingblock.SubMolecule;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.graph.MAPGraphExporter;
import org.glycoinfo.WURCSFramework.wurcs.graph.Backbone;
import org.glycoinfo.WURCSFramework.wurcs.graph.BackboneCarbon;
import org.glycoinfo.WURCSFramework.wurcs.graph.DirectionDescriptor;
import org.glycoinfo.WURCSFramework.wurcs.graph.LinkagePosition;
import org.glycoinfo.WURCSFramework.wurcs.graph.Modification;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSEdge;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSGraph;

public class MoleculeToWURCSGraph {

	private CarbonChainFinder m_oCCFinder;

	private HashSet<Atom> m_aBackboneCarbons;
	private HashSet<Connection> m_aConnectionsB2M;

	// For LinkagePosition
	private HashMap<Connection, Backbone> m_mapConnectionToBackbone;
	private HashMap<Connection, Modification> m_mapConnectionToModification;
	private HashMap<Connection, Integer> m_mapConnectionToPosition;
	private HashMap<Connection, DirectionDescriptor> m_mapConnectionToDirectionDescriptor;
	private HashMap<Connection, Integer> m_mapConnectionToStarIndex;

	private WURCSGraph m_oGraph;

	public MoleculeToWURCSGraph() {
		this.m_oCCFinder = new CarbonChainFinder();
		this.clear();
	}

	public void clear() {
		this.m_aBackboneCarbons = new HashSet<Atom>();
		this.m_aConnectionsB2M = new HashSet<Connection>();

		this.m_mapConnectionToBackbone = new HashMap<Connection, Backbone>();
		this.m_mapConnectionToModification = new HashMap<Connection, Modification>();

		this.m_mapConnectionToPosition = new HashMap<Connection, Integer>();
		this.m_mapConnectionToDirectionDescriptor = new HashMap<Connection, DirectionDescriptor>();
		this.m_mapConnectionToStarIndex = new HashMap<Connection, Integer>();

		this.m_oGraph = new WURCSGraph();
	}

	/**
	 * Get converted WURCSGraph
	 * @return WURCSGraph
	 */
	public WURCSGraph getWURCSGraph() {
		return this.m_oGraph;
	}

	/**
	 * Get CarbonChainFinder to set parameters
	 * @return CarbonChainFinder
	 */
	public CarbonChainFinder getCarbonChainFinder() {
		return this.m_oCCFinder;
	}

	public void start(Molecule a_oMolecule) throws WURCSException {
		this.clear();

		// Find carbon chains for candidate backbone and prepare conditions for input molecule
		LinkedList<LinkedList<Atom>> t_aCandidateBackbones = this.findCandidateBackbones(a_oMolecule);

		// Create Backbones and associate connection with Backbone and its Position and DirectionDescriptor
		this.createBackbones(t_aCandidateBackbones);
		// Create Modifications and associate connection with Modification and Star Index
		this.createModifications(a_oMolecule);


		// Create WURCSGraph
		this.m_oGraph = new WURCSGraph();

		for ( Connection t_oConnB2M : this.m_aConnectionsB2M ) {
			// Create LinkagePosition
			int t_iPosition = this.m_mapConnectionToPosition.get(t_oConnB2M);
			DirectionDescriptor t_enumDD = this.m_mapConnectionToDirectionDescriptor.get(t_oConnB2M);
			int t_iStarIndex = this.m_mapConnectionToStarIndex.get(t_oConnB2M);
			boolean t_bCanOmitDirection = ( t_enumDD == DirectionDescriptor.N );
			boolean t_bCanOmitStarIndex = ( t_iStarIndex==0 );
			LinkagePosition t_oLinkPos = new LinkagePosition(t_iPosition, t_enumDD, t_bCanOmitDirection, t_iStarIndex, t_bCanOmitStarIndex);

			// Create WURCSEdge
			WURCSEdge t_oEdge = new WURCSEdge();
			t_oEdge.addLinkage(t_oLinkPos);

			// Add to WURCSGraph
			Backbone t_oBackbone         = this.m_mapConnectionToBackbone.get(t_oConnB2M);
			Modification t_oModification = this.m_mapConnectionToModification.get(t_oConnB2M);
			this.m_oGraph.addResidues(t_oBackbone, t_oEdge, t_oModification);
		}
	}

	/**
	 * Prepare conditions of Molecule before analysis and conversion
	 * @param a_oMolecule Target Molecule
	 * @throws WURCSException
	 */
	private LinkedList<LinkedList<Atom>> findCandidateBackbones(Molecule a_oMolecule) throws WURCSException {
		// Normalize molecule
		MoleculeNormalizer t_oMolNorm = new MoleculeNormalizer();
		t_oMolNorm.normalize(a_oMolecule);

		// Throw exeption if there is no carbon
		int t_nCarbon = 0;
		for ( Atom t_oAtom :a_oMolecule.getAtoms() ) {
			if ( t_oAtom.getSymbol().equals("C") ) t_nCarbon++;
		}
		if ( t_nCarbon == 0 )
			throw new WURCSException("There is no carbon in the molecule.");

		// Structureral analyze for molecule
		// Collect atoms which membered aromatic, pi cyclic and carbon cyclic rings
		StructureAnalyzer t_oStAnal = new StructureAnalyzer();
		t_oStAnal.analyze(a_oMolecule);

		// Stereochemical analyze
		StereochemistryAnalysis t_oStereo = new StereochemistryAnalysis();
		t_oStereo.setStereoTo(a_oMolecule);

		// Set start atoms for carbon chain finder
		HashSet<Atom> t_setTerminalCarbons = t_oStAnal.getTerminalCarbons();
		// Set Ignore atoms for carbon chain finder
		HashSet<Atom> t_setIgnoreAtoms = new HashSet<Atom>();
		t_setIgnoreAtoms.addAll( t_oStAnal.getAromaticAtoms() );
		t_setIgnoreAtoms.addAll( t_oStAnal.getPiCyclicAtoms() );
		t_setIgnoreAtoms.addAll( t_oStAnal.getCarbonCyclicAtoms() );

		// Search Backbone carbon chain
		this.m_oCCFinder.find( t_setTerminalCarbons, t_setIgnoreAtoms );
		LinkedList<LinkedList<Atom>> t_aCandidateBackbones = this.m_oCCFinder.getCandidateCarbonChains();
		if ( t_aCandidateBackbones.isEmpty() )
			throw new WURCSException("Cannot find a Backbone in the molecule.");

		return t_aCandidateBackbones;
	}

	/**
	 * Create Backbones and associate linkage informations
	 * @param a_aCandidateBackbones List of candidate backbones
	 * @throws WURCSException
	 */
	private void createBackbones( LinkedList<LinkedList<Atom>> a_aCandidateBackbones ) throws WURCSException {
		// Screen candidate Backbone carbon chain
		CarbonChainComparator t_oComp = new CarbonChainComparator();
		Collections.sort(a_aCandidateBackbones,  t_oComp);

		// Select the most suitable carbon chains as main chain, and collect carbon chains which contain atoms of selected that.
		// Repeat for all candidateBackbones.
		LinkedList<LinkedList<LinkedList<Atom>>> t_aCandidateBackboneGroups = new LinkedList<LinkedList<LinkedList<Atom>>>();
		while ( a_aCandidateBackbones.size()>0 ) {
			LinkedList<LinkedList<Atom>> t_aBackbones = new LinkedList<LinkedList<Atom>>();
			t_aBackbones.addFirst( a_aCandidateBackbones.removeFirst() );
			for ( int ii=0; ii<a_aCandidateBackbones.size(); ii++ ) {
				LinkedList<Atom> t_oBackbone = a_aCandidateBackbones.get(ii);
				for ( Atom atom : t_oBackbone ) {
					if ( !t_aBackbones.getFirst().contains(atom) ) continue;
					t_aBackbones.addLast( a_aCandidateBackbones.remove(ii) );
					ii--;
					break;
				}
			}
			t_aCandidateBackboneGroups.add(t_aBackbones);
		}

		// Set backbone flag for the most suitable carbon chains in each groups.
		// # If there are two or more suitable one.
		HashMap<LinkedList<Atom>, Boolean> t_mapChainIsBackbone = new HashMap<LinkedList<Atom>, Boolean>();
		for ( LinkedList<LinkedList<Atom>> t_aBackbones : t_aCandidateBackboneGroups ) {
			// TODO: remove print
//			System.err.println("Group" + t_aCandidateBackboneGroups.indexOf(backbones) + ":");
//			this.printCarbonChains(backbones);

			// Set true to backbone flag for most suitable carbon chains as backbone, and set false to remains
			// Initialize backbone flag
			for ( LinkedList<Atom> t_oBackbone : t_aBackbones )
				t_mapChainIsBackbone.put(t_oBackbone, true);

			// Set false to remains
			int t_nBackbones = t_aBackbones.size();
			for(int ii=0; ii<t_nBackbones-1; ii++){
				LinkedList<Atom> t_oB1 = t_aBackbones.get(ii);
				LinkedList<Atom> t_oB2 = t_aBackbones.get(ii+1);
				int t_iComp = t_oComp.compare(t_oB1, t_oB2);
				if(t_iComp == 0) continue;

				for(int jj=ii+1; jj<t_nBackbones; jj++){
					LinkedList<Atom> t_oB3 = t_aBackbones.get(jj);
					t_mapChainIsBackbone.put(t_oB3, false);
				}
			}
		}

		// Collect carbon chains with higher priority in each groups as main chain
		// TODO: There are cases that not be narrowed down.
		LinkedList<LinkedList<Atom>> t_aBackboneChains = new LinkedList<LinkedList<Atom>>();
		for ( LinkedList<LinkedList<Atom>> t_aCandidateChains : t_aCandidateBackboneGroups ) {
			if ( t_mapChainIsBackbone.get( t_aCandidateChains.getFirst() ) == false) continue;
			LinkedList<Atom> t_aBackboneChain = t_aCandidateChains.getFirst();
			t_aBackboneChains.addLast( t_aBackboneChain );
			// Add Backbone carbon to list of backbone carbon
			this.m_aBackboneCarbons.addAll( t_aBackboneChain );
		}


		// Create Backbones
		LinkedList<Backbone> t_aBackbones = new LinkedList<Backbone>();
		HashMap<LinkedList<Atom>, Backbone> t_mapChainToBackbone = new HashMap<LinkedList<Atom>, Backbone>();
		CarbonChainToBackbone_TBD CC2B = new CarbonChainToBackbone_TBD();
		for ( LinkedList<Atom> t_aChain : t_aBackboneChains ) {
			Backbone t_oBackbone = CC2B.convert(t_aChain);
			t_mapChainToBackbone.put(t_aChain, t_oBackbone);

			t_aBackbones.add(t_oBackbone);

			// Associate connection with Backbone and its position and direction
			for ( int i=0; i<t_aChain.size(); i++ ) {
				Atom t_oCarbon = t_aChain.get(i);
				BackboneCarbon t_oBC = t_oBackbone.getBackboneCarbons().get(i);
				int t_iPosition = i+1;
				for ( Connection t_oConnB2M : t_oCarbon.getConnections() ) {
					if ( t_aChain.contains( t_oConnB2M.endAtom() ) ) continue;
					if ( t_oConnB2M.endAtom().getSymbol().equals("H") ) continue;

					this.m_aConnectionsB2M.add(t_oConnB2M);

					// Associate connection to backbone
					this.m_mapConnectionToBackbone.put(t_oConnB2M, t_oBackbone);
					// Associate connection to position
					this.m_mapConnectionToPosition.put(t_oConnB2M, t_iPosition);

					// Calculate Direction and associate with connection
					ConnectionToDirection t_oC2D = new ConnectionToDirection(t_aChain);
					DirectionDescriptor t_enumDD = t_oC2D.convert(t_oConnB2M, t_oBC.getDesctriptor() );
					this.m_mapConnectionToDirectionDescriptor.put(t_oConnB2M, t_enumDD);
				}
			}
		}
	}

	/**
	 * Create Modifications and associate linkage informations
	 * @param a_oMolecule Target Molecule
	 */
	private void createModifications( Molecule a_oMolecule ) {
		// Create SubGraph for candidate modifications
		HashSet<Atom> t_aIgnoreAtoms = new HashSet<Atom>();
		t_aIgnoreAtoms.addAll(this.m_aBackboneCarbons);
		LinkedList<SubGraphOld> t_aSubGraphs = new LinkedList<SubGraphOld>();
		for ( Atom t_oStart : a_oMolecule.getAtoms() ) {
			if ( t_aIgnoreAtoms.contains(t_oStart) ) continue;
			if ( t_oStart.getSymbol().equals("H") ) continue;
			// Set start atom to candidate subgraph and expand
			SubGraphOld t_oSubGraph = new SubGraphOld();
			t_oSubGraph.expand(t_oStart, t_aIgnoreAtoms);

			// Add atoms of the candidate subgraph to ignore list
			t_aIgnoreAtoms.addAll(t_oSubGraph.getAtoms());

			t_aSubGraphs.add(t_oSubGraph);
		}

		// Convert SubGraph to Modification using SubMolecule
		SubGraphToSubMolecule t_oSG2SM = new SubGraphToSubMolecule(this.m_aBackboneCarbons);
		for ( SubGraphOld t_oSubGraph : t_aSubGraphs ) {
			// Convert SubGraph to SubMolecule
			SubMolecule t_oSubMol = t_oSG2SM.convert(t_oSubGraph);

			// XXX: remove print
//			t_oSG2SM.printLog();

			// Generate MAP string from SubMolecule
			SubMoleculeToMAPGraph t_oSM2MAP = new SubMoleculeToMAPGraph(t_oSubMol);
			t_oSM2MAP.start();
			MAPGraphExporter t_oMAPExport = new MAPGraphExporter();
			String t_strMAP = t_oMAPExport.getMAP( t_oSM2MAP.getMAPGraph() );
			// Create Modification
			Modification t_oModif = new Modification( t_strMAP );

			// Associate connection with Modification and star index
			for ( Atom t_oCarbon : t_oSubMol.getBackboneCarbons() ) {
				Connection t_oConn = t_oSubMol.getConnectionFromBackbone(t_oCarbon);
				Connection t_oOrigConn = t_oSubMol.getOriginalConnection(t_oConn);
				this.m_mapConnectionToModification.put(t_oOrigConn, t_oModif);
				int t_iStarIndex = t_oSM2MAP.getStarIndexFromBackboneCarbon(t_oCarbon);
				this.m_mapConnectionToStarIndex.put( t_oOrigConn, t_iStarIndex );
			}
		}
	}

	private void createLinkagePosition(Connection a_oConn) {
		// Create LinkagePosition
		int t_iPosition = this.m_mapConnectionToPosition.get(a_oConn);
		DirectionDescriptor t_enumDD = this.m_mapConnectionToDirectionDescriptor.get(a_oConn);
		int t_iStarIndex = this.m_mapConnectionToStarIndex.get(a_oConn);
		boolean t_bCanOmitDirection = ( t_enumDD == DirectionDescriptor.N );
		boolean t_bCanOmitStarIndex = ( t_iStarIndex==0 );
		LinkagePosition t_oLinkPos = new LinkagePosition(t_iPosition, t_enumDD, t_bCanOmitDirection, t_iStarIndex, t_bCanOmitStarIndex);

	}
}
