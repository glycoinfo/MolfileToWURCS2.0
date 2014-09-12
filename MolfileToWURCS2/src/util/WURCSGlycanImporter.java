package util;

import glycan.Glycan;
import glycan.GlycanList;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import sugar.wurcs.WURCSGlycanObjectException;
import util.analytical.StructureAnalyzer;
import util.creator.AglyconeCreator;
import util.creator.BackboneCreator;
import util.creator.ModificationCreator;
import chemicalgraph.subgraph.aglycone.Aglycone;
import chemicalgraph.subgraph.backbone.Backbone;
import chemicalgraph.subgraph.backbone.BackboneList;
import chemicalgraph.subgraph.modification.Modification;
import chemicalgraph.subgraph.molecule.Molecule;
import chemicalgraph2.Atom;
import chemicalgraph2.Connection;

/**
 * WURCS Glycan Importer
 * @author Masaaki Matsubara
 *
 */
public class WURCSGlycanImporter {
	private Molecule m_objMolecule;

	private BackboneCreator   m_objBackboneCreator = new BackboneCreator();

	public  GlycanList        m_aGlycans        = new GlycanList();
	private BackboneList      m_aBackbones      = new BackboneList();
	private LinkedList<Modification>  m_aModifications  = new LinkedList<Modification>();
	private LinkedList<Aglycone>      m_aAglycones      = new LinkedList<Aglycone>();

	private HashMap<Atom, Backbone> m_hashAtomToBackbone = new HashMap<Atom, Backbone>();
	private HashSet<Atom> m_aBackboneAtoms     = new HashSet<Atom>();
	private HashSet<Atom> m_aModificationAtoms = new HashSet<Atom>();
	private HashSet<Atom> m_aAglyconeAtoms     = new HashSet<Atom>();

	public  LinkedList<LinkedList<Backbone>> m_aCandidateBackboneGroups = new LinkedList<LinkedList<Backbone>>();

	public WURCSGlycanImporter() {
	}

	/**
	 * Set parameters for BackboneCreator
	 * @param minNOS
	 * @param minO
	 * @param minBackboneLength
	 * @param maxBackboneLength
	 * @param ratioBackboneNOS
	 */
	public void setBackboneParameters(final int minNOS, final int minO, final int minBackboneLength, final int maxBackboneLength, final float ratioBackboneNOS) {
		this.m_objBackboneCreator.setParameters(minNOS, minO, minBackboneLength, maxBackboneLength, ratioBackboneNOS);
	}

	public void clear() {
//		m_objMolecule = null;

		this.m_aGlycans.clear();
		this.m_aBackbones.clear();
		this.m_aModifications.clear();
		this.m_aAglycones.clear();
		this.m_aCandidateBackboneGroups.clear();
	}

	public void importWURCSGlycan(Molecule a_objMolecule) throws WURCSGlycanObjectException {
		this.findBackbones();
		System.out.println(this.m_aBackbones.size());
		this.findAglycones();
		this.findModifications();
	}


	/**
	 * Generate WURCS
	 * @param a_objMolecule
	 * @throws WURCSGlycanObjectException
	 */
	public void generateWURCS(Molecule a_objMolecule) throws WURCSGlycanObjectException {
		StructureAnalyzer t_objAnalyzer = new StructureAnalyzer();
		t_objAnalyzer.analyze(a_objMolecule);
		this.m_objMolecule = t_objAnalyzer.getMolecule();
		this.clear();
/*
		StructureAnalizer t_objSA = new StructureAnalizer(this.m_objMolecule);
		t_objSA.omitIsotope();
		t_objSA.omitCharge();
		t_objSA.removeMetalAtoms();
		t_objSA.addHiddenHydrogens();
		t_objSA.setStereoMolecule();
		t_objSA.findAromaticRings();
		t_objSA.findPiRings();
		t_objSA.findCarbonRings();
*/
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

	/**
	 * Get Backbones
	 */
//	private void findBackbones(HashSet<Atom> a_aIgnoreAtoms){
	private void findBackbones(){
		// Get carbon chains, which was reduced length by C1 check
		LinkedList<Backbone> candidateBackbones = this.m_objBackboneCreator.create(this.m_objMolecule.atoms);

		// Select the most suitable carbon chains as main chain, and collect carbon chains which contain atoms of selected that.
		// Repeating that until no candidateBackbones.
		LinkedList<LinkedList<Backbone>> t_aCandidateBackboneGroups = new LinkedList<LinkedList<Backbone>>();
		while(candidateBackbones.size()>0){
			LinkedList<Backbone> backboneGroup = new LinkedList<Backbone>();
			backboneGroup.addFirst(candidateBackbones.removeFirst());
			for(int ii=0; ii<candidateBackbones.size(); ii++){
				Backbone checkBackbone = candidateBackbones.get(ii);
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
		for(LinkedList<Backbone> backbones : t_aCandidateBackboneGroups){
//			backbones.setBackboneFlag();
			for(Backbone backbone : backbones){
				backbone.isBackbone = true;
			}
			// 主鎖として採用される可能性の残っていないbackboneにbackbone.isBackbone=falseを立てる。
			// イテレータを使った処理に変更出来たら後で対応する。
			int num = backbones.size();
			for(int ii=0; ii<num-1; ii++){
				Backbone backbone1 = backbones.get(ii);
				Backbone backbone2 = backbones.get(ii+1);
				int result = backbone1.compareTo(backbone2);
				if(result == 0) continue;

				for(int jj=ii+1; jj<num; jj++){
					Backbone backbone3 = backbones.get(jj);
					backbone3.isBackbone = false;
				}
			}
		}

		// Make a choise top of carbon chains in each groups as main chain
		// TODO: There are cases that not be narrowed down.
		this.m_aBackbones = new BackboneList();
		for(LinkedList<Backbone> backbones : t_aCandidateBackboneGroups){
			if(backbones.get(0).isBackbone == false) continue;
			this.m_aBackbones.add(backbones.get(0));
		}
		this.m_aCandidateBackboneGroups = t_aCandidateBackboneGroups;

		// set informations of backbone to main chain carbons
		for(Backbone backbone : this.m_aBackbones){
			for(Atom atom : backbone){
				atom.backbone = backbone;
				this.m_aBackboneAtoms.add(atom);
				this.m_hashAtomToBackbone.put(atom, backbone);
			}
		}
//		HashSet<Atom> atomlist = (HashSet<Atom>) this.m_hashAtomToBackbone.keySet();
	}

	/**
	 * Get Aglycones
	 * @throws WURCSGlycanObjectException
	 */
	private void findAglycones() throws WURCSGlycanObjectException {
		// Use aglycone creator
		AglyconeCreator t_objCreator = new AglyconeCreator();
		t_objCreator.setBackbones(this.m_aBackbones);
		// Create Alycone list
		t_objCreator.create();
		this.m_aAglycones = t_objCreator.getAglycones();
		this.m_aAglyconeAtoms = t_objCreator.getAglyconeAtoms();
	}

	/**
	 * Get Modifications
	 * @throws WURCSGlycanObjectException
	 */
	private void findModifications() throws WURCSGlycanObjectException{
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
		*/
	}

	/**
	 * get glycans
	 */
	private void findGlycans() {
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

}
