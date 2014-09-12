package util.creator;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;

import sugar.wurcs.Backbone;
import sugar.wurcs.WURCSGlycanObject;
import sugar.wurcs.WURCSGlycanObjectException;
import util.analytical.CarbonIdentifier;
import chemicalgraph2.Atom;
import chemicalgraph2.Connection;

public class BackboneCreator extends WURCSGlycanObjectCreator{

	private int m_iMinNOS    = 0;
	private int m_iMinO      = 0;
	private int m_iMinLength = 3;
	private int m_iMaxLength = 3;
	private float m_fRatioNOS = 2.0f;

	private CarbonIdentifier m_objIdentifier = new CarbonIdentifier();

	private LinkedList<Backbone> m_aBackboneList = new LinkedList<Backbone>();
	private LinkedList<Backbone> m_aCandidateBackboneList = new LinkedList<Backbone>();
	private HashSet<Atom> m_aAromaticAtoms;
	private HashSet<Atom> m_aPiCyclicAtoms;
	private HashSet<Atom> m_aCarbonCyclicAtoms;
	private HashSet<Atom> m_aIgnoreAtoms;
	private HashSet<Atom> m_aTerminalAtoms;

	//----------------------------
	// Constructor
	//----------------------------
	public BackboneCreator(){
	}

	//----------------------------
	// Accessor
	//----------------------------
	public void setParameters(final int minNOS, final int minO, final int minBackboneLength, final int maxBackboneLength, final float ratioBackboneNOS){
		this.m_iMinNOS    = minNOS;
		this.m_iMinO      = minO;
		this.m_iMinLength = minBackboneLength;
		this.m_iMaxLength = maxBackboneLength;
		this.m_fRatioNOS  = ratioBackboneNOS;
	}

	public void setIgnoreAtoms(HashSet<Atom> atoms) {
		this.m_aIgnoreAtoms = atoms;
	}

	public void setTerminalAtoms(HashSet<Atom> atoms) {
		this.m_aTerminalAtoms = atoms;
	}

	public void clear() {
		this.m_aBackboneList = new LinkedList<Backbone>();
		this.m_aCandidateBackboneList = new LinkedList<Backbone>();
	}

	//----------------------------
	// Public method (void)
	//----------------------------
	public void create() throws WURCSGlycanObjectException {
		this.clear();
		Backbone candidateBackbone = new Backbone();
		for ( Atom atom : this.m_aTerminalAtoms ) {
//			if(!atom.isTerminalCarbon()) continue;
			if ( !this.checkTerminalCarbon(atom) ) continue;
			candidateBackbone.clear();
			candidateBackbone.add(atom);
			this.searchCandidateBackbone(candidateBackbone);
		}

		// Make short carbon chains if the first carbon is not suitable as C1.
		for(Backbone backbone : this.m_aCandidateBackboneList){
			this.convertCandidateBackbone(backbone);
		}

		// Sift through that not satisfy the chain length.
		for(Backbone backbone : this.m_aCandidateBackboneList){
			int backboneLength = backbone.size();
			if(this.m_iMinLength <= backboneLength && backboneLength <= this.m_iMaxLength){
				this.m_aBackboneList.add(backbone);
			}
		}

		// set each flag and code for backbones
		for ( Backbone backbone : this.m_aBackboneList ) {
			backbone.setCoOCOSequence(this.m_iMinLength);
			backbone.setOxidationSequence();
			backbone.skeletonCode = backbone.toSkeletonCode();
		}
		// sort by monosaccharide backbone likeness
		Collections.sort(this.m_aBackboneList, new Comparator<Backbone>() {
			public int compare(Backbone backbone1, Backbone backbone2) {
				return backbone1.compareTo(backbone2);
			}
		});

//		return this.m_aBackboneList;
	}

	public void searchCandidateBackbone(final Backbone candidateBackbone){
		if(candidateBackbone.size() == 0){
			System.err.println("please set start atom.");
			return;
		}
		Atom tailAtom = candidateBackbone.getLast();
		if(!tailAtom.getSymbol().equals("C")) return;
		if ( this.m_aIgnoreAtoms.contains(atom) ) return;
//		if( tailAtom.isAromatic) return;
//		if( tailAtom.isPiCyclic) return;
//		if( tailAtom.isCarbonCyclic) return;

		// Add backbone to backbones if tail atom is terminal carbon and backbone length is not one.
//		if(candidateBackbone.size() != 1 && this.checkTerminalCarbon(tailAtom)){
		if(candidateBackbone.size() != 1 && this.m_aTerminalAtoms.contains(tailAtom)){
			int numNOS = this.countCarbonsConnectedNOS(candidateBackbone);
			if( numNOS < this.m_iMinNOS) return;
			if(this.countCarbonsConnectedSingleBondOxygen(candidateBackbone) < this.m_iMinO) return;

			//2014/07/28 Issaku YAMADA m_ratoBackboneNOS
			// Backbone炭素数をBackboneに結合しているNOSの数で割った値が > 2 であればBackboneに追加しない。
//			System.err.println("candidateBackbone.size(): " + candidateBackbone.size());
//			System.err.println("candidateBackbone.countNOSconnected(): " + candidateBackbone.countNOSconnected());
			if (candidateBackbone.size() / numNOS  > this.m_fRatioNOS) return;


			Backbone newCandidateBackbone = new Backbone();
			for(Atom atom : candidateBackbone){
				newCandidateBackbone.addLast(atom);
			}
			this.m_aCandidateBackboneList.add(newCandidateBackbone);
			return;
		}

		// Depth search
		for(Connection connect : tailAtom.getConnections()){
			Atom nextAtom = connect.endAtom();
			if(candidateBackbone.contains(nextAtom)) continue;
			candidateBackbone.addLast(nextAtom);
			this.searchCandidateBackbone(candidateBackbone);
			candidateBackbone.removeLast();
		}
	}

	/**
	 * Return true if the atom is terminal carbon of chemical subgraph which is not contain aromatic rings, ring of atoms with pi electron, ring of carbon and atom of non carbon. Return false otherwise.<br>
	 * 入力化学構造から以下に含まれる原子を取り除くことで得られる部分構造に対して、末端炭素である場合にtrueを返す。<br>
	 * 除外する構造：芳香環、π電子を持つ原子で構成される環、炭素原子で構成される環、炭素原子以外の原子<br>
	 * @return true if the atom is terminal carbon of chemical subgraph which is not contain aromatic rings, ring of atoms with pi electron, ring of carbon and atom of non carbon. false otherwise.
	 */
	private boolean checkTerminalCarbon(Atom atom) {
		if ( !atom.getSymbol().equals("C") ) return false;
		if ( this.m_aIgnoreCarbons.contains(atom) ) return false;
		int numC = 0;
		for ( Connection con : atom.getConnections() ) {
			Atom conAtom = con.endAtom();
			if ( !conAtom.getSymbol().equals("C")) continue;
			if ( this.m_aIgnoreCarbons.contains(conAtom) ) continue;
			numC++;
		}
		return ( numC == 1 );

	}

	private void convertCandidateBackbone(Backbone backbone){
		// Using CarbonIdentifier
		CarbonIdentifier ident = this.m_objIdentifier;

		// Determination of C1 position
		Atom head = backbone.getFirst();
		Atom CyclicEtherCarbon = backbone.getFirstCyclicEtherCarbon();

		// Return if cyclic ether carbon is not found
		if ( CyclicEtherCarbon == null ) return;

		// Return if head carbon like aldehyde or connects two NOS
		ident.setAtom(head);
		if ( ident.isAldehydeLike() || ident.connectsTwoNOS() ) return;

		// Return if cyclic ether carbon like ketal
		ident.setAtom(CyclicEtherCarbon);
		if( !ident.isKetalLike()) return ;

		// Make branch from head atoms
		Backbone branch = new Backbone();
		for(Atom atom : backbone){
			if(atom == CyclicEtherCarbon) break;
			branch.add(atom);
		}
		// Return if the branch like monosaccharide
		if ( branch.size() - this.countCarbonsConnectedNOS(branch) < 2 ) return;

		// Eliminate the extra carbons from candidate backbone
		while(backbone.getFirst()!=CyclicEtherCarbon){
			backbone.removeFirst();
		}
	}

	//----------------------------
	// Public method (non void)
	//----------------------------
	private int countCarbonsConnectedNOS(Backbone backbone){
		int NOSNum = 0;
		for(Atom atom : backbone){
			for(Connection connect : atom.getConnections()){
				String symbol = connect.endAtom().getSymbol();
				if(symbol.equals("N") || symbol.equals("O") || symbol.equals("S")){
					NOSNum++;
					break;
				}
			}
		}
		return NOSNum;
	}

	private int countCarbonsConnectedSingleBondOxygen(Backbone backbone) {
		// Using CarbonIdentifier
		CarbonIdentifier ident = new CarbonIdentifier();
		int num = 0;
		for ( Atom atom : backbone ) {
			ident.setAtom(atom);
			if ( ident.isCarboxyLike() ) continue;
			for ( Connection connect : atom.getConnections() ) {
				if ( backbone.contains(connect.endAtom()) ) continue;
				if ( connect.endAtom().getSymbol().equals("O") && connect.getBond().getType()==1 ) {
					num++;
					break;
				}
			}
		}
		return num;
	}

	@Override
	protected WURCSGlycanObject createWURCSObject() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected boolean checkCandidateSubgraph(WURCSGlycanObject candidate) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

}
