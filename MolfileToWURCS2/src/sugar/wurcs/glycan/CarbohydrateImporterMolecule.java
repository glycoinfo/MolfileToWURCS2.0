package sugar.wurcs.glycan;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import sugar.chemicalgraph.Atom;
import sugar.chemicalgraph.Bond;
import sugar.chemicalgraph.Connection;
import sugar.chemicalgraph.Molecule;
import sugar.chemicalgraph.SubGraph;
import sugar.chemicalgraph.SubGraphCreator;
import util.analytical.CarbonChainAnalyzer;
import util.analytical.CarbonChainComparator;
import util.analytical.MoleculeNormalizer;
import util.analytical.StereochemicalAnalyzer;
import util.analytical.StructureAnalyzer;
import util.creator.CarbonChainCreator;

/**
 * Class of importer for Carbohydrate
 * @author Masaaki Matsubara
 *
 */
public class CarbohydrateImporterMolecule {
	private Molecule m_objMolecule;

	private MoleculeNormalizer     m_objMoleculeNormalyzer     = new MoleculeNormalizer();
	private StructureAnalyzer      m_objStructureAnalyzer      = new StructureAnalyzer();
	private StereochemicalAnalyzer m_objStereochemicalAnalyzer = new StereochemicalAnalyzer();

	private SubGraphCreator     m_objSubgraphCreator     = new SubGraphCreator();
	private CarbonChainCreator  m_objCarbonChainCreator  = new CarbonChainCreator();

	private LinkedList<LinkedList<Atom>>  m_aBackbones      = new LinkedList<LinkedList<Atom>>();
	private LinkedList<SubGraph>          m_aAglycones      = new LinkedList<SubGraph>();
	private LinkedList<SubGraph>          m_aModifications  = new LinkedList<SubGraph>();

	private HashMap<LinkedList<Atom>, Boolean> m_hashIsBackbone = new HashMap<LinkedList<Atom>, Boolean>();
	private HashMap<Atom, LinkedList<Atom>> m_hashAtomToBackbone     = new HashMap<Atom, LinkedList<Atom>>();
	private HashMap<Atom, SubGraph>         m_hashAtomToModificaiton = new HashMap<Atom, SubGraph>();
	private HashSet<Atom> m_aAnomericCarbons   = new HashSet<Atom>();
	private HashSet<Atom> m_aBackboneCarbons   = new HashSet<Atom>();
	private HashSet<Atom> m_aModificationAtoms = new HashSet<Atom>();
	private HashSet<Atom> m_aAglyconAtoms      = new HashSet<Atom>();

	public  LinkedList<LinkedList<LinkedList<Atom>>> m_aCandidateBackboneGroups = new LinkedList<LinkedList<LinkedList<Atom>>>();

	public CarbohydrateImporterMolecule() {
	}

	/**
	 * Get Backbone creator
	 * @return BackboneCreator
	 */
	public CarbonChainCreator getCarbonChainCreator() {
		return this.m_objCarbonChainCreator;
	}

	public void clear() {
//		m_objMolecule = null;

		this.m_aBackbones.clear();
		this.m_aAglycones.clear();
		this.m_aModifications.clear();
		this.m_aCandidateBackboneGroups.clear();
		this.m_hashIsBackbone.clear();
		this.m_hashAtomToBackbone.clear();

		this.m_objCarbonChainCreator.clear();
		this.m_objSubgraphCreator.clear();
	}

	public void start(Molecule a_objMolecule) throws WURCSException {
		this.clear();
		this.m_objMolecule = a_objMolecule;

		// Normalize
		this.m_objMoleculeNormalyzer.normalize(this.m_objMolecule);

		// Structureral analyze
		// Collect atoms which membered aromatic, pi cyclic and carbon cyclic rings
		this.m_objStructureAnalyzer.analyze(this.m_objMolecule);

		// Set start atoms for backbone creator
		this.m_objCarbonChainCreator.setTerminalCarbons( this.m_objStructureAnalyzer.getTerminalCarbons() );
		// Set Ignore atoms for backbone creator
		this.m_objCarbonChainCreator.setIgnoreAtoms( this.m_objStructureAnalyzer.getAromaticAtoms() );
		this.m_objCarbonChainCreator.setIgnoreAtoms( this.m_objStructureAnalyzer.getPiCyclicAtoms() );
		this.m_objCarbonChainCreator.setIgnoreAtoms( this.m_objStructureAnalyzer.getCarbonCyclicAtoms() );

		// Stereochemical analyze
		this.m_objMolecule.setStereo();
//		this.m_objStereochemicalAnalyzer.analyze(this.m_objMolecule);
		for ( Atom atom : this.m_objMolecule.getAtoms() ) {
			if ( atom.getChirality() == null ) continue;
			System.err.println( this.m_objMolecule.getAtoms().indexOf(atom) + ":" + atom.getChirality() );
		}
		for ( Bond bond : this.m_objMolecule.getBonds() ) {
			if ( bond.getGeometric() == null ) continue;
			System.err.println( bond.getGeometric() );
		}

		this.findCarbonChainsForBackbones();
		this.printCarbonChains(this.m_aBackbones);
		this.findModifications();
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
	 * Get Backbones
	 */
//	private void findBackbones(HashSet<Atom> a_aIgnoreAtoms){
	private void findCarbonChainsForBackbones(){
		// Get carbon chains, which was reduced length by C1 check
		LinkedList<LinkedList<Atom>> candidateBackbones = this.m_objCarbonChainCreator.create();
		this.printCarbonChains(candidateBackbones);

//		candidateBackbones.setcoOCOSequence(minBackboneLength);
//		candidateBackbones.setOxidationSequence();
//		candidateBackbones.setSkeletoneCode();
//		candidateBackbones.sortByMonoSaccharideBackboneLikeness(minBackboneLength);
		// Sort candidate backbones. Prioritize the backbone satisfying the conditions as monosaccharide.
		// TODO: compare skeletoneCode in CarbonChainComparator
		CarbonChainComparator t_objComp = new CarbonChainComparator();
		Collections.sort(candidateBackbones,  t_objComp);
		this.printCarbonChains(candidateBackbones);

		// Select the most suitable carbon chains as main chain, and collect carbon chains which contain atoms of selected that.
		// Repeating that until no candidateBackbones.
		LinkedList<LinkedList<LinkedList<Atom>>> t_aCandidateBackboneGroups = new LinkedList<LinkedList<LinkedList<Atom>>>();
		while(candidateBackbones.size()>0){
			LinkedList<LinkedList<Atom>> backboneGroup = new LinkedList<LinkedList<Atom>>();
			backboneGroup.addFirst(candidateBackbones.removeFirst());
			for(int ii=0; ii<candidateBackbones.size(); ii++){
				LinkedList<Atom> checkBackbone = candidateBackbones.get(ii);
				for(Atom atom : checkBackbone){
					if(backboneGroup.getFirst().contains(atom)){
						backboneGroup.addLast(candidateBackbones.remove(ii));
						ii--;
						break;
					}
				}
			}
			t_aCandidateBackboneGroups.add(backboneGroup);
		}

		// Set backbone flag for the most suitable carbon chains in each groups.
		// # For the case which there are two or more suitable one.
		for(LinkedList<LinkedList<Atom>> backbones : t_aCandidateBackboneGroups){
			System.err.println("Group" + t_aCandidateBackboneGroups.indexOf(backbones) + ":");
			this.printCarbonChains(backbones);

//			backbones.setBackboneFlag();
			for(LinkedList<Atom> backbone : backbones){
				this.m_hashIsBackbone.put(backbone, true);
//				backbone.isBackbone = true;
			}
			// 主鎖として採用される可能性の残っていないbackboneにbackbone.isBackbone=falseを立てる。
			// イテレータを使った処理に変更出来たら後で対応する。
			//
			int num = backbones.size();
			for(int ii=0; ii<num-1; ii++){
				LinkedList<Atom> backbone1 = backbones.get(ii);
				LinkedList<Atom> backbone2 = backbones.get(ii+1);
				int result = t_objComp.compare(backbone1, backbone2);
				if(result == 0) continue;

				for(int jj=ii+1; jj<num; jj++){
					LinkedList<Atom> backbone3 = backbones.get(jj);
					this.m_hashIsBackbone.put(backbone3, false);
//					backbone3.isBackbone = false;
				}
			}
		}

		CarbonChainAnalyzer analCC = new CarbonChainAnalyzer();
		// Make a choise top of carbon chains in each groups as main chain
		// TODO: There are cases that not be narrowed down.
		this.m_aBackbones = new LinkedList<LinkedList<Atom>>();
		for ( LinkedList<LinkedList<Atom>> backbones : t_aCandidateBackboneGroups ) {
			if ( this.m_hashIsBackbone.get( backbones.get(0) ) == false) continue;
			this.m_aBackbones.add(backbones.get(0));
		}
		this.m_aCandidateBackboneGroups = t_aCandidateBackboneGroups;

		// set informations of backbone to main chain carbons
		for ( LinkedList<Atom> backbone : this.m_aBackbones ) {
			this.m_aBackboneCarbons.addAll(backbone);
			for ( Atom atom : backbone ) {
//				atom.backbone = backbone;
				this.m_hashAtomToBackbone.put(atom, backbone);
			}
			analCC.setCarbonChain(backbone);
			this.m_aAnomericCarbons.add( analCC.getAnomericCarbon() );
		}
//		HashSet<Atom> atomlist = (HashSet<Atom>) this.m_hashAtomToBackbone.keySet();
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
	private void findModifications() {
		// Collect start atoms for modification sub graphs
		HashSet<Atom> startAtoms = new HashSet<Atom>();
		for ( LinkedList<Atom> backbone : this.m_aBackbones ) {
			for ( Atom atom : backbone ) {
				for ( Connection con : atom.getConnections() ) {
					Atom conatom = con.endAtom();
					if ( this.m_aBackboneCarbons.contains(conatom) ) continue;
					if ( conatom.getSymbol().equals("H") ) continue;
					startAtoms.add(conatom);
				}
			}
		}

		// Create sub graphs
		this.m_objSubgraphCreator.addStartAtoms(startAtoms);
		this.m_objSubgraphCreator.addIgnoreAtoms(this.m_aBackboneCarbons);
		LinkedList<SubGraph> candidateModifications = this.m_objSubgraphCreator.create();

		// Sort out the aglycons from the candidate modifications
		for ( SubGraph graph : candidateModifications ) {
			boolean isAglycon = true;
			for ( Connection con : graph.getExternalConnections() ) {
				Atom conatom = con.endAtom();
				if ( !this.m_aBackboneCarbons.contains( conatom ) ) continue;
				if (  this.m_aAnomericCarbons.contains( conatom ) ) continue;
				isAglycon = false;
			}
			if ( isAglycon ) this.m_aAglycones.addLast(graph);
		}
		// Remove aglycons from candidate modifications
		for ( SubGraph aglycon : this.m_aAglycones ) {
			candidateModifications.remove(aglycon);
		}

		// Add backbone atoms to candidate modifications
		for ( SubGraph graph : candidateModifications ) {
			HashSet<Connection> ModToBackbone = new HashSet<Connection>();
			for ( Connection con : graph.getExternalConnections() ) {
				Atom conatom = con.endAtom();
				if ( !this.m_aBackboneCarbons.contains( conatom ) ) continue;
				ModToBackbone.add(con);
				graph.add( conatom );
				graph.add( con.getBond() );
				this.m_hashAtomToModificaiton.put( con.startAtom(), graph );
			}
		}
	}

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
