package chemicalgraph.subgraph.molecule;

import glycan.Glycan;
import glycan.GlycanList;

import java.util.HashMap;
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

/**
 * Class for molecule
 * @author KenichiTanaka
 */
public class Molecule extends ChemicalGraph{
	//----------------------------
	// Member variable
	//----------------------------
	public String filepath;
	public String filename;
	public String ID;
	public HashMap<String, LinkedList<String>> data = new HashMap<String, LinkedList<String>>();
	
	public LinkedList<BackboneList> candidateBackboneGroups;
	
	public BackboneList backbones;
	private AglyconeList aglycones;
	public ModificationList modifications;
	public GlycanList glycans;

	//----------------------------
	// Public method (void)
	//----------------------------
	/**
	 * Generate WURCS
	 * @param minNOS
	 * @param minO
	 * @param minBackboneLength
	 * @param maxBackboneLength
	 */
	public void generateWURCS(final int minNOS, final int minO, final int minBackboneLength, final int maxBackboneLength, final float ratioBackboneNOS) {
		this.omitIsotope();
		this.omitCharge();
		this.removeMetalAtoms();
		this.addHiddenHydrogens();
		this.setStereoMolecule();
		this.findAromaticRings();
		this.findPiRings();
		this.findCarbonRings();
		this.findBackbones(minNOS, minO, minBackboneLength, maxBackboneLength, ratioBackboneNOS); //Issaku YAMADA
		this.findAglycones();
		this.findModifications();
		for(Modification mod : this.modifications){
			mod.setStereoModification();
			mod.findCanonicalPaths();
			mod.findConnectedBackbones();
			mod.findAtomsOfBackbones();
			mod.findAtomsOfModification();
			mod.findCOLINs();
		}
		for(Backbone backbone : this.backbones){
			backbone.findCOLINs();
			backbone.connectsBackboneToModification.sortForCanonicalWURCS(this.backbones);
		}
		this.findGlycans();
		for(Glycan glycan : this.glycans){
			glycan.sortForCanonicalWURCS();
			glycan.generateWURCS(false);
		}
	}

	//----------------------------
	// Public method (non void)
	//----------------------------
	/**
	 * (For debug) Return true if generate skeleton code equals skeleton code part of ID.
	 * @return true if generate skeleton code equals skeleton code part of ID.
	 */
	public boolean hasSameSkeletoneCodeWithID(){
		if((this.candidateBackboneGroups.size() == 0) && (this.ID.length() == 0)){ return true; }
		if((this.candidateBackboneGroups.size() != 0) && (this.ID.length() != 0)){
			for(BackboneList backbones : this.candidateBackboneGroups){
				for(Backbone backbone : backbones){
					if(!backbone.isBackbone) continue;
					String skeletonCode = backbone.skeletonCode;
					if(this.ID.length() >= skeletonCode.length() && skeletonCode.equals(this.ID)){ return true; }
					String tmp = skeletonCode + "|";
					if(this.ID.length() >= tmp.length() && tmp.equals(this.ID.substring(0, tmp.length()))){ return true; }
					tmp = skeletonCode + "-";
					if(this.ID.length() >= tmp.length() && tmp.equals(this.ID.substring(0, tmp.length()))){ return true; }
				}
			}
		}
		return false;
	}
	
	//----------------------------
	// Private method (void)
	//----------------------------
	/**
	 * Remove metal atoms.
	 */
	private void removeMetalAtoms(){
		AtomList removeAtoms = new AtomList();
		for(Atom atom : this.atoms){
			if(atom.isMetal()){
				removeAtoms.add(atom);
			}
		}
		for(Atom atom : removeAtoms){
			this.remove(atom);
		}
	}
	
	/**
	 * Reset isotope information.
	 */
	private void omitIsotope(){
		for(Atom atom : this.atoms){
			atom.mass = 0;
		}
	}

	/**
	 * Reset charge information.
	 */
	private void omitCharge() {
		for(Atom atom : this.atoms){
			atom.charge = 0;
		}
	}

	/**
	 * Add hidden hydrogens.
	 */
	private void addHiddenHydrogens() {
		for(Atom atom : this.atoms){
			atom.addHiddenHydrogens();
		}
	}

	/**
	 * Calculate the stereo chemistry for each atom. target is whole molecule.
	 */
	private void setStereoMolecule() {
		super.setStereoTmp();
		for(Atom atom : this.atoms){
			atom.stereoMolecule = atom.stereoTmp;
			atom.stereoTmp = null;
		}
		for(Bond bond : this.bonds){
			bond.stereoMolecule = bond.stereoTmp;
			bond.stereoTmp = null;
		}
	}
	
	/**
	 * pi電子を一つ以上もつ原子で構成される環かつpi電子の総数が4n+2の環に対して、atom.isAromatic = trueを立てる。
	 * Backboneクラスを流用
	 */
	private void findAromaticRings(){
		Backbone chain = new Backbone();
		for(Atom atom : this.atoms){
			chain.clear();
			chain.addLast(atom);
			chain.aromatize();
		}
	}

	/**
	 * pi電子を一つ以上もつ原子で構成される環に対して、atom.isPiCyclic = trueを立てる
	 * Backboneクラスを流用
	 */
	private void findPiRings(){
		Backbone chain = new Backbone();
		for(Atom atom : this.atoms){
			chain.clear();
			chain.addLast(atom);
			chain.piCyclic();
		}
	}

	/**
	 * 炭素のみで構成される環に対して、atom.isCarbonCyclic = trueを立てる。
	 * Backboneクラスを流用
	 */
	private void findCarbonRings(){
		Backbone chain = new Backbone();
		for(Atom atom : this.atoms){
			chain.clear();
			chain.addLast(atom);
			chain.carbonCyclic();
		}
	}

	/**
	 * Backboneの取得を行う。
	 * @param minNOS
	 * @param minO
	 * @param minBackboneLength
	 * @param maxBackboneLength
	 */
	private void findBackbones(final int minNOS, final int minO, final int minBackboneLength, final int maxBackboneLength, final float ratioBackboneNOS){  //Issaku YAMADA
		// C1判定を行い短縮後の炭素鎖を取得
		BackboneList candidateBackbones = new BackboneList(this.atoms, minNOS, minO, minBackboneLength, maxBackboneLength, ratioBackboneNOS);  // Issaku YAMADA

		// 取得した炭素鎖から最も主鎖らしい炭素鎖を選択、選択した炭素鎖に含まれる原子を含む炭素鎖を同一グループにまとめる。残った炭素鎖から最も主鎖らしい炭素鎖を選択。。。
		this.candidateBackboneGroups = new LinkedList<BackboneList>();
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
			this.candidateBackboneGroups.add(backboneGroup);
		}

		// 各グループに含まれる炭素鎖に対して、最も主鎖らしい炭素鎖にFlagを立てる。
		// ※2つ以上最も主鎖らしい炭素鎖がある場合への対応
		for(BackboneList backbones : this.candidateBackboneGroups){
			backbones.setBackboneFlag();
		}
		
		// 各Groupのトップの炭素鎖を主鎖炭素として採用
		// 絞り切れていないケースがあるので本当は、その部分も対応すべき
		this.backbones = new BackboneList();
		for(BackboneList backbones : this.candidateBackboneGroups){
			if(backbones.get(0).isBackbone == false) continue;
			this.backbones.add(backbones.get(0));
		}
		
		// 主鎖炭素に、所属主鎖情報をセット、 ここは絞り込みが終わった後もそのままでOK
		for(Backbone backbone : this.backbones){
			for(Atom atom : backbone){
				atom.backbone = backbone;
			}
		}
	}
	
	/**
	 * Aglyconeの取得を行う。
	 */
	private void findAglycones() {
		this.aglycones = new AglyconeList();
		for(Backbone backbone : this.backbones){
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
					this.aglycones.add(candidate.toAglycone());
				}
			}
		}
	}
	
	/**
	 * 修飾の取得を行う。
	 */
	private void findModifications(){
		this.modifications = new ModificationList();
		for(Backbone backbone : this.backbones){
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
					
					this.modifications.add(candidate.toModification());
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
