package io.MDLMOL;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.wurcsgraph.Backbone;
import org.glycoinfo.WURCSFramework.wurcsgraph.BackboneCarbon;
import org.glycoinfo.WURCSFramework.wurcsgraph.LinkagePosition;
import org.glycoinfo.WURCSFramework.wurcsgraph.Modification;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSEdge;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSException;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSGraph;

import chemicalgraph.Atom;
import chemicalgraph.Connection;
import chemicalgraph.Molecule;
import chemicalgraph.SubGraph;
import chemicalgraph.util.analytical.CarbonChainAnalyzer;
import chemicalgraph.util.analytical.MoleculeNormalizer;
import chemicalgraph.util.analytical.StructureAnalyzer;
import chemicalgraph.util.forwurcsglycan.CarbonChainComparator;
import chemicalgraph.util.forwurcsglycan.CarbonChainFinder;
import chemicalgraph.util.forwurcsglycan.CarbonChainToBackbone;
import chemicalgraph.util.forwurcsglycan.ConnectionToLinkagePosition;
import chemicalgraph.util.forwurcsglycan.SubGraphCreator;
import chemicalgraph.util.forwurcsglycan.SubGraphToModification;

/**
 * Class of importer for Carbohydrate
 * @author Masaaki Matsubara
 *
 */
public class WURCSGraphImporterMolecule {
	private Molecule m_objMolecule;

//	private SubGraphCreator     m_objSubgraphCreator     = new SubGraphCreator();
	private CarbonChainFinder  m_objCCFinder  = new CarbonChainFinder();

//	private LinkedList<LinkedList<Atom>>  m_aBackboneChains      = new LinkedList<LinkedList<Atom>>();
	private LinkedList<SubGraph>          m_aAglyconGraphs       = new LinkedList<SubGraph>();
	private LinkedList<SubGraph>          m_aModificationGraphs  = new LinkedList<SubGraph>();

	private HashMap<Connection, LinkedList<Atom>> m_hashConnectionToBackboneChain     = new HashMap<Connection, LinkedList<Atom>>();
	private HashMap<Connection, SubGraph>         m_hashConnectionToModificationGraph = new HashMap<Connection, SubGraph>();
//	private HashSet<Atom> m_aAnomericCarbons   = new HashSet<Atom>();
//	private HashSet<Atom> m_aBackboneCarbons   = new HashSet<Atom>();
//	private HashSet<Atom> m_aModificationAtoms = new HashSet<Atom>();
//	private HashSet<Atom> m_aAglyconAtoms      = new HashSet<Atom>();

	public  LinkedList<LinkedList<LinkedList<Atom>>> m_aCandidateBackboneGroups = new LinkedList<LinkedList<LinkedList<Atom>>>();

	public WURCSGraphImporterMolecule() {
	}

	/**
	 * Get Backbone creator
	 * @return BackboneCreator
	 */
	public CarbonChainFinder getCarbonChainCreator() {
		return this.m_objCCFinder;
	}

	public void clear() {
//		m_objMolecule = null;

//		this.m_aBackboneChains.clear();
		this.m_aAglyconGraphs.clear();
		this.m_aModificationGraphs.clear();
		this.m_aCandidateBackboneGroups.clear();
//		this.m_hashIsBackbone.clear();
		this.m_hashConnectionToBackboneChain.clear();
		this.m_hashConnectionToModificationGraph.clear();

		this.m_objCCFinder.clear();
//		this.m_objSubgraphCreator.clear();
	}

	public WURCSGraph start(Molecule a_objMolecule) throws WURCSException {
		this.clear();
		this.m_objMolecule = a_objMolecule;

		// Normalize molecule
		MoleculeNormalizer normalyzer = new MoleculeNormalizer();
		normalyzer.normalize(this.m_objMolecule);

		// Structureral analyze molecule
		// Collect atoms which membered aromatic, pi cyclic and carbon cyclic rings
		StructureAnalyzer analSt = new StructureAnalyzer();
		analSt.analyze(this.m_objMolecule);

		// Set start atoms for carbon chain finder
		HashSet<Atom> terminalCarbons = analSt.getTerminalCarbons();
		// Set Ignore atoms for carbon chain finder
		HashSet<Atom> ignoreAtoms = new HashSet<Atom>();
		ignoreAtoms.addAll( analSt.getAromaticAtoms() );
		ignoreAtoms.addAll( analSt.getPiCyclicAtoms() );
		ignoreAtoms.addAll( analSt.getCarbonCyclicAtoms() );

		// Stereochemical analyze
		this.m_objMolecule.setStereo();
//		this.m_objStereochemicalAnalyzer.analyze(this.m_objMolecule);
/*
		for ( Atom atom : this.m_objMolecule.getAtoms() ) {
			if ( atom.getChirality() == null ) continue;
			System.err.println( this.m_objMolecule.getAtoms().indexOf(atom) + ":" + atom.getChirality() );
		}
		for ( Bond bond : this.m_objMolecule.getBonds() ) {
			if ( bond.getGeometric() == null ) continue;
			System.err.println( bond.getGeometric() );
		}
*/
		// Find and get carbon chains, which was reduced length by C1 check
		this.m_objCCFinder.find( terminalCarbons, ignoreAtoms );
		LinkedList<LinkedList<Atom>> candidateBackbones = this.m_objCCFinder.getCandidateCarbonChains();
//		this.printCarbonChains(candidateBackbones);

		// Find components for carbohydrate
		LinkedList<LinkedList<Atom>> aBackboneChains     = this.findCarbonChainsForBackbones(candidateBackbones);
		LinkedList<SubGraph>         aModificationGraphs = this.findModificationGraphs(aBackboneChains);
		HashSet<Connection>       aLinkageConnections = this.findLinkageConnections(aBackboneChains, aModificationGraphs);

//		this.printCarbonChains(aBackboneChains);

		// Make Backbones
		LinkedList<Backbone> backbones = new LinkedList<Backbone>();
		HashMap<Atom, BackboneCarbon> hashAtomToBackboneCarbon = new HashMap<Atom, BackboneCarbon>();
		HashMap<LinkedList<Atom>, Backbone> hashChainToBackbone = new HashMap<LinkedList<Atom>, Backbone>();
		CarbonChainToBackbone CC2B = new CarbonChainToBackbone();
		for ( LinkedList<Atom> chain : aBackboneChains ) {
			Backbone backbone = CC2B.convert(chain);
			hashChainToBackbone.put(chain, backbone);

			backbones.add(backbone);
			for ( int i=0; i<chain.size(); i++ ) {
				hashAtomToBackboneCarbon.put( chain.get(i), backbone.getBackboneCarbons().get(i) );
			}

			System.err.println( aBackboneChains.indexOf(chain) + ": " + backbone.getSkeletonCode() );
		}

		// Make Modifications
		LinkedList<Modification> modifications = new LinkedList<Modification>();
		HashMap<SubGraph, Modification> hashGraphToModification = new HashMap<SubGraph, Modification>();
		HashMap<SubGraph, LinkedList<Atom>> hashGraphToModificationCarbons = new HashMap<SubGraph, LinkedList<Atom>>();
		SubGraphToModification SG2M = new SubGraphToModification(analSt.getAromaticAtoms(), aBackboneChains);
		for ( SubGraph graph : aModificationGraphs ) {
			Modification modification = SG2M.convert(graph);
			hashGraphToModification.put(graph, modification);
			hashGraphToModificationCarbons.put(graph, SG2M.getBackboneAtoms());
			for ( Atom atom : SG2M.getBackboneAtoms() ) {
				BackboneCarbon bc = hashAtomToBackboneCarbon.get(atom);
//				modification.addBackboneCarbon(bc);
			}
			modifications.add(modification);
		}

		WURCSGraph objWURCSGlycan = new WURCSGraph();

		// Make Linkages and Edges
		ConnectionToLinkagePosition C2L = new ConnectionToLinkagePosition(hashGraphToModificationCarbons);
		LinkedList<WURCSEdge> edges = new LinkedList<WURCSEdge>();
		int count = 0;
		for ( Connection con : aLinkageConnections ) {
			// Make linkage
			LinkedList<Atom> chain = this.m_hashConnectionToBackboneChain.get(con);
			SubGraph graph         = this.m_hashConnectionToModificationGraph.get(con);

			LinkagePosition link = C2L.convert(con, chain, graph);

			// Make edge
			WURCSEdge edge = new WURCSEdge();
			edge.addLinkage(link);

			// Connect backbone and modification in graph
			Backbone backbone         = hashChainToBackbone.get(chain);
			Modification modification = hashGraphToModification.get(graph);

			objWURCSGlycan.addResidues(backbone, edge, modification);
			edges.add(edge);

			count++;
		}
		System.err.println("edge count:"+count);
		return objWURCSGlycan;
	}

	private void printCarbonChains(LinkedList<LinkedList<Atom>> chains) {
		System.err.println(chains.size());
		for ( LinkedList<Atom> backbone : chains ) {
			System.err.print( "Chain" + chains.indexOf(backbone)+": " );
			String chain = "";
			for ( int i=0; i<backbone.size(); i++ ) {
				if ( i > 0 ) chain += "-";
				int num = this.m_objMolecule.getAtoms().indexOf( backbone.get(i) );
				chain += num;
			}
			System.err.println( chain );
		}
	}

	/**
	 * Find and collect carbon chains for backbones
	 */
	private LinkedList<LinkedList<Atom>> findCarbonChainsForBackbones(final LinkedList<LinkedList<Atom>> candidateBackbones){

//		candidateBackbones.setcoOCOSequence(minBackboneLength);
//		candidateBackbones.setOxidationSequence();
//		candidateBackbones.setSkeletoneCode();
//		candidateBackbones.sortByMonoSaccharideBackboneLikeness(minBackboneLength);
		// Sort candidate backbones. Prioritize the backbone satisfying the conditions as monosaccharide.
		// TODO: compare skeletoneCode in CarbonChainComparator
		CarbonChainComparator t_objComp = new CarbonChainComparator();
		Collections.sort(candidateBackbones,  t_objComp);
//		this.printCarbonChains(candidateBackbones);

		// Select the most suitable carbon chains as main chain, and collect carbon chains which contain atoms of selected that.
		// Repeat for all candidateBackbones.
		LinkedList<LinkedList<LinkedList<Atom>>> t_aCandidateBackboneGroups = new LinkedList<LinkedList<LinkedList<Atom>>>();
		while(candidateBackbones.size()>0){
			LinkedList<LinkedList<Atom>> backboneGroup = new LinkedList<LinkedList<Atom>>();
			backboneGroup.addFirst(candidateBackbones.removeFirst());
			for(int ii=0; ii<candidateBackbones.size(); ii++){
				LinkedList<Atom> checkBackbone = candidateBackbones.get(ii);
				for(Atom atom : checkBackbone){
					if(!backboneGroup.getFirst().contains(atom)) continue;
					backboneGroup.addLast(candidateBackbones.remove(ii));
					ii--;
					break;
				}
			}
			t_aCandidateBackboneGroups.add(backboneGroup);
		}
		this.m_aCandidateBackboneGroups = t_aCandidateBackboneGroups;

		// Set backbone flag for the most suitable carbon chains in each groups.
		// # If there are two or more suitable one.
		HashMap<LinkedList<Atom>, Boolean> hashIsBackbone = new HashMap<LinkedList<Atom>, Boolean>();
		for(LinkedList<LinkedList<Atom>> backbones : t_aCandidateBackboneGroups){
//			System.err.println("Group" + t_aCandidateBackboneGroups.indexOf(backbones) + ":");
//			this.printCarbonChains(backbones);

			// Set true to backbone flag for most suitable carbon chains as backbone, and set false to remains
			// Initialize backbone flag
//			backbones.setBackboneFlag();
			for(LinkedList<Atom> backbone : backbones){
				hashIsBackbone.put(backbone, true);
//				backbone.isBackbone = true;
			}

			// Set false to remains
			int num = backbones.size();
			for(int ii=0; ii<num-1; ii++){
				LinkedList<Atom> backbone1 = backbones.get(ii);
				LinkedList<Atom> backbone2 = backbones.get(ii+1);
				int result = t_objComp.compare(backbone1, backbone2);
				if(result == 0) continue;

				for(int jj=ii+1; jj<num; jj++){
					LinkedList<Atom> backbone3 = backbones.get(jj);
					hashIsBackbone.put(backbone3, false);
//					backbone3.isBackbone = false;
				}
			}
		}

		// Make a choise top of carbon chains in each groups as main chain
		// TODO: There are cases that not be narrowed down.
		LinkedList<LinkedList<Atom>> aBackboneChains = new LinkedList<LinkedList<Atom>>();
		for ( LinkedList<LinkedList<Atom>> backbones : t_aCandidateBackboneGroups ) {
			if ( hashIsBackbone.get( backbones.get(0) ) == false) continue;
			aBackboneChains.add(backbones.get(0));
			// Link backbone carbon and backbone
		}

		return aBackboneChains;
	}

	/**
	 * Get Aglycones
	 * @throws WURCSGlycanObjectException
	 */
/*	private void findAglycones() throws WURCSGlycanObjectException {
		// Use aglycone creator
		AglyconeCreator t_objCreator = new AglyconeCreator();
		t_objCreator.setBackbones(this.m_aBackbones);
		// Create Alycone list
		t_objCreator.create();
		this.m_aAglycones = t_objCreator.getAglycones();
		this.m_aAglyconeAtoms = t_objCreator.getAglyconeAtoms();
	}
*/
	/**
	 * Find and collect SubGraphs for Modifications
	 */
	private LinkedList<SubGraph> findModificationGraphs(final LinkedList<LinkedList<Atom>> a_aBackboneChains) {
		// Collect carbons of backbones
		CarbonChainAnalyzer analCC = new CarbonChainAnalyzer();
		HashSet<Atom> aBackboneCarbons = new HashSet<Atom>();
		HashSet<Atom> aAnomericCarbons = new HashSet<Atom>();
		for ( LinkedList<Atom> backbone : a_aBackboneChains ) {
			aBackboneCarbons.addAll(backbone);
			aAnomericCarbons.add( analCC.setCarbonChain(backbone).getAnomericCarbon() );
		}

		// Collect start atoms for modification sub graphs
		HashSet<Atom> startAtoms = new HashSet<Atom>();
		for ( LinkedList<Atom> backbone : a_aBackboneChains ) {
			for ( Atom atom : backbone ) {
				for ( Connection con : atom.getConnections() ) {
					Atom conatom = con.endAtom();
					if ( aBackboneCarbons.contains(conatom) ) continue;
					if ( conatom.getSymbol().equals("H") ) continue;
					startAtoms.add(conatom);
				}
			}
		}

		// Create sub graph for candidate modifications
		SubGraphCreator creator = new SubGraphCreator(startAtoms, aBackboneCarbons);
		LinkedList<SubGraph> candidateModifications = creator.create();

		// Find aglycons from the candidate modifications
		for ( SubGraph graph : candidateModifications ) {
			boolean isAglycon = true;
			for ( Connection con : graph.getExternalConnections() ) {
				Atom conatom = con.endAtom();
				if ( !aBackboneCarbons.contains( conatom ) ) continue;
				if (  aAnomericCarbons.contains( conatom ) ) continue;
				isAglycon = false;
			}
			if ( isAglycon ) this.m_aAglyconGraphs.addLast( graph );
		}

		// Remove aglycons from candidate modifications
		// and add modifications which remade from removed aglycons
		for ( SubGraph aglycon : this.m_aAglyconGraphs ) {
			candidateModifications.remove( aglycon );

			for ( Connection con : aglycon.getExternalConnections() ) {
				Atom conatom = con.endAtom();
				if ( !aBackboneCarbons.contains( conatom ) ) continue;
				SubGraph newMod = new SubGraph();
				newMod.add(con.startAtom());
				candidateModifications.add(newMod);
			}
		}

		// Add backbone atoms to candidate modifications
		for ( SubGraph graph : candidateModifications ) {
			for ( Connection con : graph.getExternalConnections() ) {
				Atom conatom = con.endAtom();
				if ( !aBackboneCarbons.contains( conatom ) ) continue;
				graph.add( conatom );
				graph.add( con.getBond() );
			}
		}

		return candidateModifications;
	}

	/**
	 * Find and collect connections between backbone carbon chain and modification subgraph
	 * @param chains Backbone carbon chains
	 * @param graphs Modification subgraphs
	 * @return List of connections linked from backbone to modification
	 */
	private HashSet<Connection> findLinkageConnections(LinkedList<LinkedList<Atom>> chains, LinkedList<SubGraph> graphs) {
		// Collect connections from backbone to modification
		HashSet<Connection> aConB2M = new HashSet<Connection>();
		for ( LinkedList<Atom> chain : chains ) {
			for ( Atom atom : chain ) {
				for ( Connection con : atom.getConnections() ) {
					Atom conatom = con.endAtom();
					if ( chain.contains(conatom) ) continue;
					for ( SubGraph graph : graphs ) {
						if ( !graph.contains(conatom) ) continue;
//						System.err.print(conatom.getSymbol());
						aConB2M.add(con);
						this.m_hashConnectionToBackboneChain.put(con, chain);
						this.m_hashConnectionToModificationGraph.put(con, graph);
					}
				}
			}
		}
		return aConB2M;
	}
}
