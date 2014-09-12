package sugar.wurcs;

import java.util.Comparator;

import util.Chemical;
import chemicalgraph2.Connection;

public class BackboneComparator implements Comparator<Backbone> {

	public int compare( Backbone backbone1, Backbone backbone2 ) {
		// C1にketoneLike, aldehideLike, KetalLike, acetalLike 構造を持ち、CarboxylLikeでないかどうかの0,1からなる文字列を、辞書順で降順
		int coOCOSequenceCheck = backbone2.getCoOCOSequence().compareTo(backbone1.getCoOCOSequence());
		if(coOCOSequenceCheck != 0)	return coOCOSequenceCheck;
		
		// C1が環の一部(C1がヘテロ原子を挟んでBackboneの他の炭素に結合している)であるBackboneを優先
		if(backbone1.getFirst() == backbone1.getFirstCyclicEtherCarbon() && backbone2.getFirst() != backbone2.getFirstCyclicEtherCarbon()) return -1;
		if(backbone1.getFirst() != backbone1.getFirstCyclicEtherCarbon() && backbone2.getFirst() == backbone2.getFirstCyclicEtherCarbon()) return 1;
		
		// oxidationSequenceを辞書順で降順
		int oxidationSequenceCheck = backbone2.getOxidationSequence().compareTo(backbone1.getOxidationSequence());
		if(oxidationSequenceCheck != 0) return oxidationSequenceCheck;
		
		// これ以降、Backboneの長さは等しくなる。
		int backboneLen = backbone1.size();
		for(int ii=0; ii<backboneLen; ii++){
			// initialize
			int[] atomNum1 = new int[200];
			int[] atomNum2 = new int[200];
			for(int jj=0; jj<200; jj++){
				atomNum1[jj] = 0;
				atomNum2[jj] = 0;
			}
			
			// Backboneのii番目の炭素に結合している原子の数を種類ごとにカウント
			for(Connection connect : backbone1.get(ii).getConnections()){
				atomNum1[Chemical.getAtomicNumber(connect.endAtom().getSymbol())]++;
			}
			for(Connection connect : backbone2.get(ii).getConnections()){
				atomNum2[Chemical.getAtomicNumber(connect.endAtom().getSymbol())]++;
			}
			
			// 窒素(原子番号7), 酸素(原子番号8), その他の原子（原子番号の大きな元素を優先）に結合数を比較
			if(atomNum1[7] != atomNum2[7]) return atomNum2[7] - atomNum1[7];
			if(atomNum1[8] != atomNum2[8]) return atomNum2[8] - atomNum1[8];
			for(int jj=199; jj>=0; jj--){
				if(atomNum1[jj] != atomNum2[jj]) return atomNum2[jj] - atomNum1[jj];
			}
		}
		
		//20140723 alditolなどで、D体が優先されるようにソート @Issaku YAMADA　ここから
		if (backbone1.skeletonCode.length() > 2 && backbone2.skeletonCode.length() > 2){
			int skchar = backbone1.skeletonCode.length() - 1;
			int targetSkchar = backbone2.skeletonCode.length() - 1;			
			int thisNum = 0;
			int targetNum = 0;
			try {
				thisNum = Integer.parseInt(backbone1.skeletonCode.substring(skchar - 1, skchar));
			} catch (NumberFormatException nfex) {}
			try {
				targetNum = Integer.parseInt(backbone2.skeletonCode.substring(targetSkchar -1, targetSkchar));
			} catch (NumberFormatException nfex) {}			
			return targetNum - thisNum;
		}
		//20140723 alditolなどで、D体が優先されるようにソート @Issaku YAMADA　ここまで
		
		// SkeletoneCodeを辞書順で昇順
		int skeletonCodeCheck = backbone1.skeletonCode.compareTo(backbone2.skeletonCode);
		if(skeletonCodeCheck != 0) return skeletonCodeCheck;
		
		return 0;
	}
}
