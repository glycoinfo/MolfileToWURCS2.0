package io.MDLMOL;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.wurcsglycan.Backbone;
import org.glycoinfo.WURCSFramework.wurcsglycan.BackboneCarbon;
import org.glycoinfo.WURCSFramework.wurcsglycan.LinkagePosition;
import org.glycoinfo.WURCSFramework.wurcsglycan.Modification;
import org.glycoinfo.WURCSFramework.wurcsglycan.WURCSEdge;
import org.glycoinfo.WURCSFramework.wurcsglycan.WURCSException;
import org.glycoinfo.WURCSFramework.wurcsglycan.WURCSGlycan;

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
public class WURCSGlycanImporterMolecule {
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

	public WURCSGlycanImporterMolecule() {
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

	public WURCSGlycan start(Molecule a_objMolecule) throws WURCSException {
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
		LinkedList<Connection>       aLinkageConnections = this.findLinkageConnections(aBackboneChains, aModificationGraphs);

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

		WURCSGlycan objWURCSGlycan = new WURCSGlycan();

		// Make Linkages and Edges
		ConnectionToLinkagePosition C2L = new ConnectionToLinkagePosition(hashGraphToModificationCarbons);
		LinkedList<WURCSEdge> edges = new LinkedList<WURCSEdge>();
		int count = 0;
		for ( Connection con : aLinkageConnections ) {
			LinkedList<Atom> chain = this.m_hashConnectionToBackboneChain.get(con);
			SubGraph graph         = this.m_hashConnectionToModificationGraph.get(con);

			Backbone backbone         = hashChainToBackbone.get(chain);
			Modification modification = hashGraphToModification.get(graph);

			WURCSEdge edge = new WURCSEdge();
			for ( WURCSEdge oldedge : edges ) {
				if ( oldedge.getBackbone().equals(backbone) && oldedge.getModification().equals(modification) ) {
					edge = oldedge;
					break;
				}
			}
			// Make linkages if new edge is found
			if ( !edges.contains(edge) ) {
				objWURCSGlycan.addResidues(backbone, edge, modification);
				count++;
/*				edge.setBackbone(backbone);
				edge.setModification(modification);
				backbone.addEdge(edge);
				modification.addEdge(edge);
*/
				edges.add(edge);
			}

			LinkagePosition link = C2L.convert(con, chain, graph);
			edge.addLinkage(link);
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
	private LinkedList<Connection> findLinkageConnections(LinkedList<LinkedList<Atom>> chains, LinkedList<SubGraph> graphs) {
		// Collect connections from backbone to modification
		LinkedList<Connection> aConB2M = new LinkedList<Connection>();
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

	/**
	 * Generate WURCS
	 * @param a_objMolecule
	 * @throws WURCSGlycanObjectException
	 */
/*	public void generateWURCS(Molecule a_objMolecule) throws WURCSException {
		StructureAnalyzer t_objAnalyzer = new StructureAnalyzer();
		t_objAnalyzer.analyze(a_objMolecule);
		this.m_objMolecule = t_objAnalyzer.getMolecule();
		this.clear();

		StructureAnalizer t_objSA = new StructureAnalizer(this.m_objMolecule);
		t_objSA.omitIsotope();
		t_objSA.omitCharge();
		t_objSA.removeMetalAtoms();
		t_objSA.addHiddenHydrogens();
		t_objSA.setStereoMolecule();
		t_objSA.findAromaticRings();
		t_objSA.findPiRings();
		t_objSA.findCarbonRings();

		this.findBackbones();
		System.out.println(this.m_aBackbones.size());
		this.findAglycones();
		this.findModifications();
		for(Modification mod : this.m_aModifications){
			mod.setStereoModification();
			mod.findCanonicalPaths();
			mod.findConnectedBackbones();
			mod.findAtomsOfBackbones();
			mod.findAtomsOfModification();
			mod.findCOLINs();



		}
		final BackboneList backbones0 = this.m_aBackbones;
		for(Backbone backbone : this.m_aBackbones){
			backbone.findCOLINs();
//			backbone.connectsBackboneToModification.sortForCanonicalWURCS(this.m_aBackbones);
			// XXX: From ConnectionList.sortForCanonicalWURCS(BackboneList)
			Collections.sort(backbone.connectsBackboneToModification, new Comparator<Connection>() {
				public int compare(Connection connection1, Connection connection2) {
					// 1. Number of backbone
					// backbones.indexOf(connection.start().backbone)
					int backboneNo1 = backbones0.indexOf(connection1.start().backbone);
					int backboneNo2 = backbones0.indexOf(connection2.start().backbone);
					if(backboneNo1 != backboneNo2) return backboneNo1 - backboneNo2;

					// 2. Nmuber of position
					// connection.start().backbone.indexOf(connection.start())
					// ２．主鎖の第何位の炭素か？connection.start().backbone.indexOf(connection.start())
					int backboneAtomNo1 = connection1.start().backbone.indexOf(connection1.start());
					int backboneAtomNo2 = connection2.start().backbone.indexOf(connection2.start());
					if(backboneAtomNo1!=backboneAtomNo2) return backboneAtomNo1 - backboneAtomNo2;

					// 3. CIP order of connection viewed from backbone
					// connection.CIPOrder??????
					// ３．主鎖からみたconnectionのCIP順位connection.CIPorder
					// CIPで順序を付けた後、主鎖炭素鎖の順位が低い炭素を手前に持ってきた時、0, 1, 2, 3, e, z, x, ?のいずれかが入っている
					String stereoForWURCS1 = connection1.stereoForWURCS;
					String stereoForWURCS2 = connection2.stereoForWURCS;
					if(!stereoForWURCS1.equals(stereoForWURCS2))
						return stereoForWURCS1.compareTo(stereoForWURCS2);

					// 4．修飾第何位の原子か？modAtoms.indexOf(connection.atom)
					int modAtomNo1 = connection1.atom.modification.atomsOfModification.indexOf(connection1.atom);
					int modAtomNo2 = connection2.atom.modification.atomsOfModification.indexOf(connection2.atom);
					if(modAtomNo1!=modAtomNo2) return modAtomNo1 - modAtomNo2;

					return 0;
				}
			});
		}
		this.findGlycans();

		// Sort glycans for canonical WURCS
		BackboneSorterForCanonicalWURCS t_objBackboneSorter = new BackboneSorterForCanonicalWURCS();
		for(Glycan glycan : this.m_aGlycans){
//			glycan.sortForCanonicalWURCS();
			// Sort backbones
//			glycan.backbones.sortForCanonicalWURCS();
			t_objBackboneSorter.sort(glycan.backbones);
			final BackboneList backbones = glycan.backbones;

			// Sort ConnectionList in each modification
			for(Modification mod : glycan.modifications){
//				mod.connectionsFromBackboneToModification.sortForCanonicalWURCS(glycan.backbones);
				// XXX: From ConnectionList.sortForCanonicalWURCS(BackboneList)
				Collections.sort(mod.connectionsFromBackboneToModification, new Comparator<Connection>() {
					public int compare(Connection connection1, Connection connection2) {
						// 1. Number of backbone
						// backbones.indexOf(connection.start().backbone)
						int backboneNo1 = backbones.indexOf(connection1.start().backbone);
						int backboneNo2 = backbones.indexOf(connection2.start().backbone);
						if(backboneNo1 != backboneNo2) return backboneNo1 - backboneNo2;

						// 2. Nmuber of position
						// connection.start().backbone.indexOf(connection.start())
						// ２．主鎖の第何位の炭素か？connection.start().backbone.indexOf(connection.start())
						int backboneAtomNo1 = connection1.start().backbone.indexOf(connection1.start());
						int backboneAtomNo2 = connection2.start().backbone.indexOf(connection2.start());
						if(backboneAtomNo1!=backboneAtomNo2) return backboneAtomNo1 - backboneAtomNo2;

						// 3. CIP order of connection viewed from backbone
						// connection.CIPOrder??????
						// ３．主鎖からみたconnectionのCIP順位connection.CIPorder
						// CIPで順序を付けた後、主鎖炭素鎖の順位が低い炭素を手前に持ってきた時、0, 1, 2, 3, e, z, x, ?のいずれかが入っている
						String stereoForWURCS1 = connection1.stereoForWURCS;
						String stereoForWURCS2 = connection2.stereoForWURCS;
						if(!stereoForWURCS1.equals(stereoForWURCS2)) return stereoForWURCS1.compareTo(stereoForWURCS2);

						// 4．修飾第何位の原子か？modAtoms.indexOf(connection.atom)
						int modAtomNo1 = connection1.atom.modification.atomsOfModification.indexOf(connection1.atom);
						int modAtomNo2 = connection2.atom.modification.atomsOfModification.indexOf(connection2.atom);
						if(modAtomNo1!=modAtomNo2) return modAtomNo1 - modAtomNo2;

						return 0;
					}
				});
			}

			//Sort modificaitons
			// glycan.modifications.sortForCanonicalWURCS(glycan.backbones);
			// XXX: From ModificationList.sortForCanonicalWURCS()
			// mod.connectedChains以外にも並べ替える対象があるかどうかチェック
			for(Modification mod : glycan.modifications){
				Collections.sort(mod.connectedBackbones, new Comparator<Backbone>() {
					public int compare(Backbone backbone1, Backbone backbone2) {
						return backbones.indexOf(backbone1) - backbones.indexOf(backbone2);
					}
				});
			}
			Collections.sort(glycan.modifications, new Comparator<Modification>() {
				public int compare(Modification mod1, Modification mod2) {
					return mod1.compareTo(mod2, backbones);
				}
			});

			// Generate WURCS
			glycan.generateWURCS(false);
		}
	}
*/

	/**
	 * Get Modifications
	 * @throws WURCSGlycanObjectException
	 */
/*	private void findModifications() throws WURCSGlycanObjectException{
		ModificationCreator t_objCreator = new ModificationCreator();
		t_objCreator.setBackbones(this.m_aBackbones);
		t_objCreator.setAglycones(this.m_aAglycones);
		t_objCreator.create();
		this.m_aModifications = t_objCreator.getModifications();
		this.m_aModificationAtoms = t_objCreator.getModificationAtoms();

/*
		for(Backbone backbone : this.m_aBackbones){
			for(Atom Cn : backbone){
				for(Connection connect : Cn.connections){
					Atom atom = connect.atom;
					if(atom.isBackbone()) continue;
					if(atom.isModification()) continue;
					if(atom.symbol.equals("H")) continue;

					// Generate candidate modification and set start atom
					// atomを起点としてサブグラフを取得
					ChemicalGraph candidate = new ChemicalGraph();
					candidate.atoms.add(atom);

					// Expand modification
//					candidate.expandModification();
					// XXX: From ChemicalGraph.expandModification()
					for(int ii=0; ii<candidate.atoms.size(); ii++){
						Atom atomn = candidate.atoms.get(ii);
						if(atomn.isBackbone()) continue;
						for(Connection connection : atomn.connections){
							if(connection.atom.isAglycone()) continue;
							if(connection.atom.symbol.equals("H")) continue;
							if(!candidate.atoms.contains(connection.atom)){
								candidate.atoms.add(connection.atom);
							}
							if(!candidate.bonds.contains(connection.bond)){
								candidate.bonds.add(connection.bond);
							}
						}
					}

					// Convert candidate subgraph to modification
//					this.m_aModifications.add(candidate.toModification());
					// XXX: From ChemicalGraph.toModification()
					Modification mod = new Modification();
					mod.atoms = candidate.atoms;
					mod.bonds = candidate.bonds;
					for(Atom atomn : mod.atoms){
						atomn.modification = mod;
					}

					this.m_aModifications.add(mod);
				}
			}
		}
	}
*/

	/**
	 * get glycans
	 */
/*	private void findGlycans() {
		for(Backbone backbone1 : this.m_aBackbones){
			if(this.m_aGlycans.contains(backbone1)) continue;
			Glycan glycan = new Glycan();
			glycan.add(backbone1);
			for(int ii=0; ii<glycan.backbones.size(); ii++){
				Backbone backbone2 = glycan.backbones.get(ii);
				for(Connection connect : backbone2.connectsBackboneToModification){
					Modification mod = connect.atom.modification;
					if(glycan.contains(mod)) continue;
					glycan.add(mod);
					for(Backbone backbone3 : mod.connectedBackbones){
						if(glycan.contains(backbone3)) continue;
						glycan.add(backbone3);
					}
				}
			}
			this.m_aGlycans.add(glycan);
		}
	}
*/
}
