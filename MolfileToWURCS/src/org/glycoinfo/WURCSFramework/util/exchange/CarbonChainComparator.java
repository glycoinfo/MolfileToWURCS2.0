package org.glycoinfo.WURCSFramework.util.exchange;

import java.util.Comparator;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.util.Chemical;
import org.glycoinfo.ChemicalStructureUtility.util.analytical.CarbonChainAnalyzer;
import org.glycoinfo.WURCSFramework.util.graph.comparator.BackboneComparator;
import org.glycoinfo.WURCSFramework.wurcs.graph.Backbone;

public class CarbonChainComparator implements Comparator<LinkedList<Atom>> {

	CarbonChainAnalyzer m_objAnalyzer1 = new CarbonChainAnalyzer();
	CarbonChainAnalyzer m_objAnalyzer2 = new CarbonChainAnalyzer();
//	SkeltonCodeGenerator m_objCodeGen = new SkeltonCodeGenerator();

	@Override
	public int compare(LinkedList<Atom> chain1, LinkedList<Atom> chain2) {
		// For length of carbon chain, to prioritize longer one
		// TODO: check comparation order
//		int t_iComp = chain2.size() - chain1.size();
//		if ( t_iComp != 0 ) return t_iComp;

		this.m_objAnalyzer1.setCarbonChain(chain1);
		this.m_objAnalyzer2.setCarbonChain(chain2);

		// C1が環の一部(C1がヘテロ原子を挟んでBackboneの他の炭素に結合している)であるBackboneを優先
		Atom etherCarbon1 = this.m_objAnalyzer1.getFirstCyclicEtherCarbon();
		Atom etherCarbon2 = this.m_objAnalyzer2.getFirstCyclicEtherCarbon();
		if(chain1.getFirst() == etherCarbon1 && chain2.getFirst() != etherCarbon2) return -1;
		if(chain1.getFirst() != etherCarbon1 && chain2.getFirst() == etherCarbon2) return 1;

		// C1にketoneLike, aldehideLike, KetalLike, acetalLike 構造を持ち、CarboxylLikeでないかどうかの0,1からなる文字列を、辞書順で降順
		// For sequence of (potential) carbonyl and not carboxyl
		String sequence1 = this.m_objAnalyzer1.getCoOCOSequence();
		String sequence2 = this.m_objAnalyzer2.getCoOCOSequence();
		int checkCoOCOSequence = sequence2.compareTo(sequence1);
		if ( checkCoOCOSequence != 0 ) return checkCoOCOSequence;

		// oxidationSequenceを辞書順で降順
		sequence1 = this.m_objAnalyzer1.getOxidationSequence();
		sequence2 = this.m_objAnalyzer2.getOxidationSequence();
		int checkOxidationSequence = sequence2.compareTo(sequence1);
		if(checkOxidationSequence != 0) return checkOxidationSequence;

		// これ以降、Backboneの長さは等しくなる。
		int checkAtomNumber = countConnectedAtomKind(chain1, chain2);
		if ( checkAtomNumber != 0 ) return checkAtomNumber;

		int result = this.compareBackbone(chain1, chain2);
		if( result != 0 ) return result;

//		result = SkeltonCode(chain1, chain2);
//		if( result != 0 ) return result;

		return 0;
	}

	/** Compare number of atoms which connected with chain for every kind */
	private int countConnectedAtomKind( LinkedList<Atom> chain1, LinkedList<Atom> chain2 ) {
		int chainLen = chain1.size();

		for(int ii=0; ii<chainLen; ii++){
			// initialize
			int[] atomNum1 = new int[200];
			int[] atomNum2 = new int[200];
			for(int jj=0; jj<200; jj++){
				atomNum1[jj] = 0;
				atomNum2[jj] = 0;
			}

			// Backboneのii番目の炭素に結合している原子の数を種類ごとにカウント
			// Count number of atoms which connected iith carbon of chain for evety kind
			for(Connection connect : chain1.get(ii).getConnections()){
				atomNum1[Chemical.getAtomicNumber(connect.endAtom().getSymbol())]++;
			}
			for(Connection connect : chain2.get(ii).getConnections()){
				atomNum2[Chemical.getAtomicNumber(connect.endAtom().getSymbol())]++;
			}

			// 窒素(原子番号7), 酸素(原子番号8), その他の原子（原子番号の大きな元素を優先）に結合数を比較
			// Compare number of connection atoms in the order of
			// nitrogen (atomic number 7), oxigen (atomic number 8) and other (prioritize large atomic number)
			if(atomNum1[7] != atomNum2[7]) return atomNum2[7] - atomNum1[7];
			if(atomNum1[8] != atomNum2[8]) return atomNum2[8] - atomNum1[8];
			for(int jj=199; jj>=0; jj--){
				if(atomNum1[jj] != atomNum2[jj]) return atomNum2[jj] - atomNum1[jj];
			}
		}
		return 0;
	}

	/** Compare backbone */
	private int compareBackbone( LinkedList<Atom> chain1, LinkedList<Atom> chain2 ) {
		CarbonChainToBackbone_TBD t_oCC2B = new CarbonChainToBackbone_TBD();
		Backbone backbone1 = t_oCC2B.convert(chain1);
		Backbone backbone2 = t_oCC2B.convert(chain2);

		return (new BackboneComparator()).compare(backbone1, backbone2);
	}

/*
	private int compateSkeletonCode() {
		String skeltonCode1 = this.m_objCodeGen.makeSkeletonCode(chain1);
		String skeltonCode2 = this.m_objCodeGen.makeSkeletonCode(chain2);

		//20140723 alditolなどで、D体が優先されるようにソート @Issaku YAMADA　ここから
		if (skeltonCode1.length() > 2 && skeltonCode2.length() > 2){
			int skchar = skeltonCode1.length() - 1;
			int targetSkchar = skeltonCode2.length() - 1;
			int thisNum = 0;
			int targetNum = 0;
			try {
				thisNum = Integer.parseInt(skeltonCode1.substring(skchar - 1, skchar));
			} catch (NumberFormatException nfex) {}
			try {
				targetNum = Integer.parseInt(skeltonCode2.substring(targetSkchar -1, targetSkchar));
			} catch (NumberFormatException nfex) {}
			return targetNum - thisNum;
		}
		//20140723 alditolなどで、D体が優先されるようにソート @Issaku YAMADA　ここまで

		// SkeletoneCodeを辞書順で昇順
		int skeletonCodeCheck = skeltonCode1.compareTo(skeltonCode2);
		if(skeletonCodeCheck != 0) return skeletonCodeCheck;
		return 0;
	}
*/
}
