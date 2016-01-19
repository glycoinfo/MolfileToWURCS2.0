package org.glycoinfo.WURCSFramework.util.exchange;

import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;
import org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.CarbonChainAnalyzer;
import org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.CarbonIdentifier;

/**
 * Class for finding carbon chain which candidate for backbone
 * @author MasaakiMatsubara
 *
 */
public class CarbonChainFinder {

	private boolean m_bShortChain = false;
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

	//----------------------------
	// Constructor
	//----------------------------
	public CarbonChainFinder(){
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

	public void clear() {
		this.m_aCarbonChainList.clear();
	}

	public LinkedList<LinkedList<Atom>> getCandidateCarbonChains() {
		return this.m_aCarbonChainList;
	}

	//----------------------------
	// Public method (void)
	//----------------------------
	public void find(HashSet<Atom> a_setTerminalCarbons, HashSet<Atom> a_setIgnoreAtoms) {
		// Search carbon chain for candidate backbone
		// Start from all terminal carbons
		LinkedList<Atom> chain = new LinkedList<Atom>();
		for ( Atom atom : a_setTerminalCarbons ) {
			chain.clear();
			chain.add(atom);
			this.searchCandidateBackbone(chain, a_setIgnoreAtoms, a_setTerminalCarbons);
		}

		// Make short carbon chains if the first carbon is not suitable as anomeric,
		// then screening carbon chains by some conditions
		LinkedList<LinkedList<Atom>> candidates = new LinkedList<LinkedList<Atom>>();
		for(LinkedList<Atom> candidateChain : this.m_aCarbonChainList){
			if ( this.m_bShortChain )
				this.convertCandidateBackbone(candidateChain);
/*
			// Screen by neighboring oxygen counting
			NeighboringOxygenCounting t_oNOCCount = new NeighboringOxygenCounting();
			t_oNOCCount.setCarbonChain(candidateChain);
			String t_strNOCPass1 = t_oNOCCount.getNOCPass1();
			String t_strNOCPass2 = t_oNOCCount.getNOCPass2();
			String t_strNOCPass2Ex = t_oNOCCount.getNOCPass2Ex();
			// TODO: remove print
			System.err.println( t_strNOCPass1+"\n"+t_strNOCPass2+"\n"+t_strNOCPass2Ex );
*/
			// Screen by number of connected N, O, or S
			int numNOS = this.countCarbonsConnectedNOS(candidateChain);
			if ( numNOS < this.m_iMinNOS ) continue;

			// Screen by number of connected O with single bond
			if(this.countCarbonsConnectedSingleBondOxygen(candidateChain) < this.m_iMinO) continue;

			// Screen by ratio of N, O, and S which connected carbon chain
			if (candidateChain.size() / numNOS  > this.m_fRatioNOS) continue;

			// Screen by chain length
			int backboneLength = candidateChain.size();
			if ( backboneLength < this.m_iMinLength || backboneLength > this.m_iMaxLength ) continue;
			candidates.add(candidateChain);
		}

		this.m_aCarbonChainList = candidates;
	}

	@SuppressWarnings("unchecked")
	public void searchCandidateBackbone(final LinkedList<Atom> chain, HashSet<Atom> ignores, HashSet<Atom> terminals){
		Atom tailAtom = chain.getLast();
		if(!tailAtom.getSymbol().equals("C")) return;
		if ( ignores.contains( tailAtom ) ) return;

		// Add backbone to candidate backbones if tail atom is terminal carbon and backbone not contains only one carbon.
		if(chain.size() != 1 && terminals.contains(tailAtom)){
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
			this.searchCandidateBackbone(chain, ignores, terminals);
			chain.removeLast();
		}
	}

	/**
	 * Make short candidate carbon chain for Backbone
	 * @param chain
	 */
	private void convertCandidateBackbone(LinkedList<Atom> chain){
		// Using CarbonIdentifier
		CarbonIdentifier ident = this.m_objIdentifier;

		// Determination of C1 position
		CarbonChainAnalyzer anal = new CarbonChainAnalyzer();
		Atom head = chain.getFirst();
		anal.setCarbonChain(chain);
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
		for(Atom atom : chain){
			if(atom == CyclicEtherCarbon) break;
			branch.add(atom);
		}
		// Return if the branch like monosaccharide
		if ( branch.size() - this.countCarbonsConnectedNOS(branch) < 2 ) return;

		// Eliminate the extra carbons from candidate backbone
		while(chain.getFirst()!=CyclicEtherCarbon){
			chain.removeFirst();
		}
	}

	//----------------------------
	// Public method (non void)
	//----------------------------
	private int countCarbonsConnectedNOS(LinkedList<Atom> chain){
		// Using CarbonIdentifier
		CarbonIdentifier ident = this.m_objIdentifier;
		int NOSNum = 0;
		for(Atom atom : chain){
			if ( ident.setAtom(atom).countConnectedNOS() > 0 ) NOSNum++;
		}
		return NOSNum;
	}

	private int countCarbonsConnectedSingleBondOxygen(LinkedList<Atom> chain) {
		// Using CarbonIdentifier
		CarbonIdentifier ident = this.m_objIdentifier;
		int num = 0;
		for ( Atom atom : chain ) {
			if ( ident.setAtom(atom).isCarboxyLike() ) continue;
			for ( Connection connect : atom.getConnections() ) {
				if ( chain.contains(connect.endAtom()) ) continue;
				if ( connect.endAtom().getSymbol().equals("O") && connect.getBond().getType()==1 ) {
					num++;
					break;
				}
			}
		}
		return num;
	}

}
