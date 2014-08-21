package molfile2wurcs;

import chemicalgraph.subgraph.molecule.MoleculeList;

/**
 * CTfileを読み込みWURCSに変換、標準出力にIDとWURCSを出力、その後、GUIを用いて変換結果を可視化。
 * @author KenichiTanaka
 *  based on MolfileToWURCS_20140204_2_h-0_Anomer
 */
public class GUI_main {
	//----------------------------
	// Public method
	//----------------------------
	public static void main(final String[] args) {
		Parameter parameter = new Parameter(args, true);
		MoleculeList mols = new MoleculeList();
		for(CTFile ctfile : parameter.ctfiles){
			ctfile.importV2000(parameter.m_ID, parameter.m_minNOS, parameter.m_minO, parameter.m_minBackboneLength, parameter.m_maxBackboneLength, parameter.m_sdfileOutput, mols, parameter.ratioBackboneNOS);  // Issaku YAMADA
		}
		
		if(mols.size()==0) return;
		
		// Sort
		// 開発時に、問題のあるレコードを上位に表示させたい場合等に、目的に合ったソート関数を実行する。
//		mols.sortByMainChainInfo();
//		mols.sortByCompareIDwithSkeletoneCode();
		
		// Viewer
		Viewer viewer = new Viewer(mols);
		viewer.colNum = 1;
		viewer.rowNum = 1;
		viewer.setSize(960, 640);
		viewer.setVisible(true);
	}
}
