package utility;

import glycan.Glycan;
import glycan.GlycanList;

import java.util.LinkedList;

import chemicalgraph.Atom;
import chemicalgraph.AtomList;
import chemicalgraph.Bond;
import chemicalgraph.ChemicalGraph;
import chemicalgraph.Connection;
import chemicalgraph.subgraph.aglycone.AglyconeList;
import chemicalgraph.subgraph.backbone.Backbone;
import chemicalgraph.subgraph.backbone.BackboneList;
import chemicalgraph.subgraph.modification.Modification;
import chemicalgraph.subgraph.modification.ModificationList;
import chemicalgraph.subgraph.molecule.Molecule;

/**
 *
 * @author Masaaki Matsubara
 *
 */
public class StructureAnalizer {

	private Molecule m_objMolecule;

	public StructureAnalizer(Molecule a_objMol) {
		this.m_objMolecule = a_objMol;
	}

	public void setMolecule(Molecule a_objMol) {
		this.m_objMolecule = a_objMol;
	}
	public Molecule getMolecule() {
		return this.m_objMolecule;
	}

	public void clear() {
		this.m_objMolecule = null;
	}

	//----------------------------
	// Public method (void)
	//----------------------------
	/**
	 * Remove metal atoms.
	 */
	public void removeMetalAtoms(){
		AtomList removeAtoms = new AtomList();
		for(Atom atom : this.m_objMolecule.atoms){
			if(atom.isMetal()){
				removeAtoms.add(atom);
			}
		}
		for(Atom atom : removeAtoms){
			this.m_objMolecule.remove(atom);
		}
	}

	/**
	 * Reset isotope information.
	 */
	public void omitIsotope(){
		for(Atom atom : this.m_objMolecule.atoms){
			atom.mass = 0;
		}
	}

	/**
	 * Reset charge information.
	 */
	public void omitCharge() {
		for(Atom atom : this.m_objMolecule.atoms){
			atom.charge = 0;
		}
	}

	/**
	 * Add hidden hydrogens.
	 */
	public void addHiddenHydrogens() {
		for(Atom atom : this.m_objMolecule.atoms){
			atom.addHiddenHydrogens();
		}
	}

	/**
	 * Calculate the stereo chemistry for each atom. target is whole molecule.
	 */
	public void setStereoMolecule() {
		this.m_objMolecule.setStereoTmp();
		for(Atom atom : this.m_objMolecule.atoms){
			atom.stereoMolecule = atom.stereoTmp;
			atom.stereoTmp = null;
		}
		for(Bond bond : this.m_objMolecule.bonds){
			bond.stereoMolecule = bond.stereoTmp;
			bond.stereoTmp = null;
		}
	}

	/**
	 * Set true for atom.isAromatic. The atom is member of an aromatic ring,
	 *  which all atoms have pi electorn(s) and total number of pi electorons is 4n+2.
	 * using class Backbone
	 */
	public void findAromaticRings(){
		Backbone chain = new Backbone();
		for(Atom atom : this.m_objMolecule.atoms){
			chain.clear();
			chain.addLast(atom);
			chain.aromatize();
		}
	}

	/**
	 * Set true for atom.isPiCyclic. The atom is member of a ring which all atoms have pi electorn(s).
	 * using class Backbone
	 */
	public void findPiRings(){
		Backbone chain = new Backbone();
		for(Atom atom : this.m_objMolecule.atoms){
			chain.clear();
			chain.addLast(atom);
			chain.piCyclic();
		}
	}

	/**
	 * Set true for atom.isCarbonCyclic. The atom is member of a ring which all atoms are carbon.
	 * using class Backbone
	 */
	public void findCarbonRings(){
		Backbone chain = new Backbone();
		for(Atom atom : this.m_objMolecule.atoms){
			chain.clear();
			chain.addLast(atom);
			chain.carbonCyclic();
		}
	}

	/**
	 * Get Backbones
	 * @param minNOS
	 * @param minO
	 * @param minBackboneLength
	 * @param maxBackboneLength
	 */
	public void findBackbones(final int minNOS, final int minO, final int minBackboneLength, final int maxBackboneLength, final float ratioBackboneNOS){  //Issaku YAMADA
		// Get carbon chains, which was reduced length by C1 check
		BackboneList candidateBackbones = new BackboneList(this.m_objMolecule.atoms, minNOS, minO, minBackboneLength, maxBackboneLength, ratioBackboneNOS);  // Issaku YAMADA

		// Select the most suitable carbon chains as main chain, and collect carbon chains which contain atoms of selected that.
		// Repeating that until no candidateBackbones.
		this.m_objMolecule.candidateBackboneGroups = new LinkedList<BackboneList>();
		candidateBackbones.setcoOCOSequence(minBackboneLength);
		candidateBackbones.setOxidationSequence();
		candidateBackbones.setSkeletoneCode();
		candidateBackbones.sortByMonoSaccharideBackboneLikeness(minBackboneLength);
		while(candidateBackbones.size()>0){
			BackboneList backboneGroup = new BackboneList();
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
			this.m_objMolecule.candidateBackboneGroups.add(backboneGroup);
		}

		// 各グループに含まれる炭素鎖に対して、最も主鎖らしい炭素鎖にFlagを立てる。
		// ※2つ以上最も主鎖らしい炭素鎖がある場合への対応
		for(BackboneList backbones : this.m_objMolecule.candidateBackboneGroups){
			backbones.setBackboneFlag();
		}

		// 各Groupのトップの炭素鎖を主鎖炭素として採用
		// 絞り切れていないケースがあるので本当は、その部分も対応すべき
		this.m_objMolecule.backbones = new BackboneList();
		for(BackboneList backbones : this.m_objMolecule.candidateBackboneGroups){
			if(backbones.get(0).isBackbone == false) continue;
			this.m_objMolecule.backbones.add(backbones.get(0));
		}

		// 主鎖炭素に、所属主鎖情報をセット、 ここは絞り込みが終わった後もそのままでOK
		for(Backbone backbone : this.m_objMolecule.backbones){
			for(Atom atom : backbone){
				atom.backbone = backbone;
			}
		}
		return
	}

	/**
	 * Aglyconeの取得を行う。
	 */
	private void findAglycones() {
		this.m_objMolecule.aglycones = new AglyconeList();
		for(Backbone backbone : this.m_objMolecule.backbones){
			Atom Canomer = backbone.getAnomer();
			for(Connection connect : Canomer.connections){
				Atom atom = connect.atom;
				if(atom.isBackbone()) continue;
				if(atom.isAglycone()) continue;
				if(atom.symbol.equals("H")) continue;

				// atomを起点としてサブグラフを取得
				ChemicalGraph candidate = new ChemicalGraph();
				candidate.atoms.add(atom);
				candidate.expandCandidateAglycone();

				// 取得したサブグラフがaglyconであればリストに追加
				if(candidate.isAglycone()){
					this.m_objMolecule.aglycones.add(candidate.toAglycone());
				}
			}
		}
	}

	/**
	 * 修飾の取得を行う。
	 */
	private void findModifications(){
		this.m_objMolecule.modifications = new ModificationList();
		for(Backbone backbone : this.m_objMolecule.backbones){
			for(Atom Cn : backbone){
				for(Connection connect : Cn.connections){
					Atom atom = connect.atom;
					if(atom.isBackbone()) continue;
					if(atom.isModification()) continue;
					if(atom.symbol.equals("H")) continue;

					// atomを起点としてサブグラフを取得
					ChemicalGraph candidate = new ChemicalGraph();
					candidate.atoms.add(atom);
					candidate.expandModification();

					this.m_objMolecule.modifications.add(candidate.toModification());
				}
			}
		}
	}

	/**
	 * get glycans
	 */
	private void findGlycans() {
		this.glycans = new GlycanList();
		for(Backbone backbone1 : this.backbones){
			if(this.glycans.contains(backbone1)) continue;
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
			this.glycans.add(glycan);
		}
	}
}
