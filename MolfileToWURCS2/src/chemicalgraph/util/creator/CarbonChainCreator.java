package chemicalgraph.util.creator;

import java.util.HashSet;
import java.util.LinkedList;

import chemicalgraph.Atom;
import chemicalgraph.Connection;
import chemicalgraph.util.analytical.CarbonChainAnalyzer;
import chemicalgraph.util.analytical.CarbonIdentifier;

/**
 * Class for create carbon chain which candidate for backbone
 * @author MasaakiMatsubara
 *
 */
public class CarbonChainCreator {

	/** Minimam number of N, O, or S connected to carbon chain */
	private int m_iMinNOS    = 0;
	/** Minimam number of O connected to carbon chain with single bond */
	private int m_iMinO      = 0;
	/** Minimam number of carbon */
	private int m_iMinLength = 3;
	/** Maximam number of carbon */
	private int m_iMaxLength = 3;
	/** Ratio of number of N, O, S connected to carbon chain per length of carbon chain */
	private float m_fRatioNOS = 2.0f;

	/** Identifier for carbon */
	private CarbonIdentifier m_objIdentifier = new CarbonIdentifier();

	private LinkedList<LinkedList<Atom>> m_aCarbonChainList = new LinkedList<LinkedList<Atom>>();
	private HashSet<Atom> m_aIgnoreAtoms     = new HashSet<Atom>();
	private HashSet<Atom> m_aTerminalCarbons = new HashSet<Atom>();

	//----------------------------
	// Constructor
	//----------------------------
	public CarbonChainCreator(){
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

	public void setTerminalCarbons(HashSet<Atom> carbons) {
		this.m_aTerminalCarbons.addAll(carbons);
	}

	public void setIgnoreAtoms(HashSet<Atom> atoms) {
		this.m_aIgnoreAtoms.addAll(atoms);
	}

	public void clear() {
		this.m_aCarbonChainList.clear();
		this.m_aTerminalCarbons.clear();
		this.m_aIgnoreAtoms.clear();
	}

	//----------------------------
	// Public method (void)
	//----------------------------
	public LinkedList<LinkedList<Atom>> create() {
		LinkedList<Atom> chain = new LinkedList<Atom>();
		for ( Atom atom : this.m_aTerminalCarbons ) {
			chain.clear();
			chain.add(atom);
			this.searchCandidateBackbone(chain);
		}

		// Make short carbon chains if the first carbon is not suitable as anomeric,
		// then screening carbon chains by some conditions
		LinkedList<LinkedList<Atom>> candidates = new LinkedList<LinkedList<Atom>>();
		for(LinkedList<Atom> backbone : this.m_aCarbonChainList){
			this.convertCandidateBackbone(backbone);

			// Number of connected N, O, or S
			int numNOS = this.countCarbonsConnectedNOS(backbone);
			if ( numNOS < this.m_iMinNOS ) continue;

			// Number of connected O with single bond
			if(this.countCarbonsConnectedSingleBondOxygen(backbone) < this.m_iMinO) continue;

			// Ratio of N, O, and S which connected carbon chain
			if (backbone.size() / numNOS  > this.m_fRatioNOS) continue;

			// Length of carbon chain
			int backboneLength = backbone.size();
			if ( this.m_iMinLength > backboneLength && backboneLength > this.m_iMaxLength ) continue;
			candidates.add(backbone);
		}

		this.m_aCarbonChainList.clear();
		return candidates;
	}

	@SuppressWarnings("unchecked")
	public void searchCandidateBackbone(final LinkedList<Atom> chain){
		Atom tailAtom = chain.getLast();
		if(!tailAtom.getSymbol().equals("C")) return;
		if ( this.m_aIgnoreAtoms.contains( tailAtom ) ) return;

		// Add backbone to candidate backbones if tail atom is terminal carbon and backbone not contains only one carbon.
		if(chain.size() != 1 && this.m_aTerminalCarbons.contains(tailAtom)){
//			LinkedList<Atom> newChain = new LinkedList<Atom>();
//			for(Atom atom : chain){
//				newChain.addLast(atom);
//			}
//			this.m_aCarbonChainList.add(newChain);
			this.m_aCarbonChainList.add((LinkedList<Atom>)chain.clone());
			return;
		}

		// Depth search
		for(Connection connect : tailAtom.getConnections()){
			Atom nextAtom = connect.endAtom();
			if(chain.contains(nextAtom)) continue;
			chain.addLast(nextAtom);
			this.searchCandidateBackbone(chain);
			chain.removeLast();
		}
	}

	private void convertCandidateBackbone(LinkedList<Atom> backbone){
		// Using CarbonIdentifier
		CarbonIdentifier ident = this.m_objIdentifier;

		// Determination of C1 position
		CarbonChainAnalyzer anal = new CarbonChainAnalyzer();
		Atom head = backbone.getFirst();
		anal.setCarbonChain(backbone);
		Atom CyclicEtherCarbon = anal.getFirstCyclicEtherCarbon();

		// Return if cyclic ether carbon is not found
		if ( CyclicEtherCarbon == null ) return;

		// Return if head carbon like aldehyde or connects two NOS
		ident.setAtom(head);
		if ( ident.isAldehydeLike() || ident.countConnectedNOS() == 2 ) return;

		// Return if cyclic ether carbon like ketal
		if( !ident.setAtom(CyclicEtherCarbon).isKetalLike()) return ;

		// Make branch from head atoms
		LinkedList<Atom> branch = new LinkedList<Atom>();
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
	private int countCarbonsConnectedNOS(LinkedList<Atom> backbone){
		// Using CarbonIdentifier
		CarbonIdentifier ident = this.m_objIdentifier;
		int NOSNum = 0;
		for(Atom atom : backbone){
			if ( ident.setAtom(atom).countConnectedNOS() > 0 ) NOSNum++;
		}
		return NOSNum;
	}

	private int countCarbonsConnectedSingleBondOxygen(LinkedList<Atom> backbone) {
		// Using CarbonIdentifier
		CarbonIdentifier ident = this.m_objIdentifier;
		int num = 0;
		for ( Atom atom : backbone ) {
			if ( ident.setAtom(atom).isCarboxyLike() ) continue;
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

}
