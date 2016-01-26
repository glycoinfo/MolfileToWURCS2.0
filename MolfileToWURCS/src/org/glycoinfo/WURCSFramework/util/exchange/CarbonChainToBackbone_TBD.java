package org.glycoinfo.WURCSFramework.util.exchange;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.util.Chemical;
import org.glycoinfo.ChemicalStructureUtility.util.analytical.CarbonChainAnalyzer;
import org.glycoinfo.ChemicalStructureUtility.util.analytical.CarbonIdentifier;
import org.glycoinfo.WURCSFramework.wurcs.graph.Backbone;
import org.glycoinfo.WURCSFramework.wurcs.graph.BackboneCarbon;
import org.glycoinfo.WURCSFramework.wurcs.graph.Backbone_TBD;
import org.glycoinfo.WURCSFramework.wurcs.graph.CarbonDescriptor_TBD;

/**
 * Class for create backbone
 * @author MasaakiMatsubara
 *
 */
public class CarbonChainToBackbone_TBD {

	private static final int OTHER           = 0;
	/** The carbon is sp3, terminal and foot of bridge, and has chirality */
	private static final int SP3_TERMINAL    = 1;
	/** The carbon is sp3 and non-terminal, and has chirality*/
	private static final int SP3_NONTERMINAL = 2;
	/** The carbon is sp2 and terminal, and has double bond between connected carbon */
	private static final int SP2_TERMINAL    = 3;
	/** The carbon is sp2 and non-terminal, and has double bond between connected carbon */
	private static final int SP2_NONTERMINAL = 4;

//	private CarbonIdentifier m_objIdent = new CarbonIdentifier();

	/**
	 * Create Backbone from carbon chain
	 * @param chain List of atoms in carbon chain
	 * @return Backbone
	 */
	public Backbone convert(LinkedList<Atom> chain) {
		Backbone_TBD backbone = new Backbone_TBD();
		int anomPos = 0;
		Atom t_oAnomericCarbon = new CarbonChainAnalyzer().setCarbonChain(chain).getAnomericCarbon();
		System.err.println( t_oAnomericCarbon );
		BackboneCarbon anomCarbon = null;
		for ( Atom carbon : chain ) {
			CarbonDescriptor_TBD cd = this.convertCarbonToDescriptor(carbon, chain);
			backbone.addBackboneCarbon( new BackboneCarbon(backbone, cd) );

			// For anomeric carbon
			if ( anomPos != 0 ) continue;
			if ( !carbon.equals(t_oAnomericCarbon) ) continue;
//			if ( !this.m_objIdent.setAtom(carbon).isAnomericLike() ) continue;
//			if ( cd.getChar() == 'o' || cd.getChar() == 'O' ) continue;
			anomPos = chain.indexOf(carbon)+1;
			anomCarbon = backbone.getBackboneCarbons().removeLast();
			cd = ( anomPos == 1 )? CarbonDescriptor_TBD.SZX_ANOMER : CarbonDescriptor_TBD.SSX_ANOMER;
			backbone.addBackboneCarbon( new BackboneCarbon(backbone, cd) );
		}
		backbone.setAnomericPosition(anomPos);
		// Check symbol of anomer
		char anomSymbol = 'x';
		if ( anomPos > 0 )
			anomSymbol = this.getAnomericSymbol(backbone, anomCarbon);
		backbone.setAnomericSymbol(anomSymbol);
		return backbone;
	}

	/**
	 * Analyze backbone carbon and convert to CarbonDescriptor
	 * @param a_objCarbon Target carbon in carbon cahin
	 * @param a_aChain Carbon chain
	 * @return CarbonDescriptor
	 */
	private CarbonDescriptor_TBD convertCarbonToDescriptor( Atom a_objCarbon, LinkedList<Atom> a_aChain ) {

		// Return unknown type if carbon has connections more than 4
		if ( a_objCarbon.getConnections().size() > 4 ) return CarbonDescriptor_TBD.XXX_UNKNOWN;

		// Get stereo (chirality)
		String strStereo = a_objCarbon.getChirality();
		String strOrbital = new CarbonIdentifier().setAtom(a_objCarbon).getHybridOrbital0();

		// Set connected backbone carbon(s)
		Atom C1=null, C2=null;
		boolean isTerminal = true;
		if ( a_objCarbon == a_aChain.getFirst() ) {
			C1 = a_aChain.get(1);
		} else if ( a_objCarbon == a_aChain.getLast()  ) {
			C2 = a_aChain.get(a_aChain.size()-2);
		} else {
			int iC = a_aChain.indexOf(a_objCarbon);
			C1 = a_aChain.get(iC-1);
			C2 = a_aChain.get(iC+1);
			isTerminal = false;
		}

		// Set connections for judge stereos for backbone carbon
		Connection conC1=null,conC2=null;
		int iType1=0,iType2=0;
		LinkedList<Connection> conList = new LinkedList<Connection>();
		HashSet<Connection> bridgeCons = new HashSet<Connection>();
		for ( Connection con : a_objCarbon.getConnections() ) {
			Atom conatom = con.endAtom();
			int type = con.getBond().getType();
			if ( C1 != null && conatom.equals(C1) ) { conC1 = con; iType1 = type; continue; }
			if ( C2 != null && conatom.equals(C2) ) { conC2 = con; iType2 = type; continue; }
			conList.add(con);

			// Check that the carbon is foot of bridge
			for ( Connection concon : conatom.getConnections() ) {
				if ( a_objCarbon.equals(concon.endAtom()) ) continue;
				if ( a_aChain.contains(concon.endAtom()) ) {
					bridgeCons.add(con);
					break;
				}
			}
		}

		// Swap bond type for backbone carbon
		if ( iType2 > iType1 ) { int type = iType2; iType2 = iType1; iType1 = type; }


		// Set modification string from connection types and connected atom symbols
		LinkedList<String> modStrList = new LinkedList<String>();
		HashSet<String>    modStrUniq = new HashSet<String>();
		for ( Connection con : conList ) {
			Atom conatom = con.endAtom();
			int type = con.getBond().getType();
			String mod = ( type == 1 )? "-" : // single bond
						 ( type == 2 )? "=" : // double bond
						 ( type == 3 )? "#" : // triple bond
						 "?";
			mod += conatom.getSymbol() + ( (bridgeCons.contains(con))? "_" : "" );  // Distinguish for bridge foot carbon
			modStrList.add( mod );
			modStrUniq.add( mod );
		}
		int nUniq=modStrUniq.size();
		// Count same string for each modifications
		HashMap<String, Integer> modUniqCount = new HashMap<String, Integer>();
		for ( String modU : modStrUniq ) {
			int count = 0;
			for ( String mod : modStrList ) {
				if ( modU.equals(mod) ) count++;
			}
			modUniqCount.put(modU, count);
		}

		// Sort modification strings
		final HashMap<String, Integer> modUniqCountFinal = modUniqCount;
		Collections.sort(modStrList, new Comparator<String>() {
			public int compare( String mod1, String mod2 ) {
				if ( mod1.equals(mod2) ) return 0;

				// Compare number of same modification. Prioritize much one.
				if ( modUniqCountFinal.get(mod1) != modUniqCountFinal.get(mod2) )
					return modUniqCountFinal.get(mod2) - modUniqCountFinal.get(mod1);

				// Compare number of bond order. Prioritize larger one.
				if ( mod1.charAt(0) == '#' && mod2.charAt(0) != '#' ) return -1;
				if ( mod1.charAt(0) != '#' && mod2.charAt(0) == '#' ) return 1;
				if ( mod1.charAt(0) == '=' && mod2.charAt(0) != '=' ) return -1;
				if ( mod1.charAt(0) != '=' && mod2.charAt(0) == '=' ) return 1;

				// Compare bridging or not
				if (  mod1.endsWith("_") && !mod2.endsWith("_") ) return -1;
				if ( !mod1.endsWith("_") &&  mod2.endsWith("_") ) return 1;

				// Compare symbol. Prioritize large atomic number.
				String symbol1 = ( !mod1.endsWith("_") )? mod1.substring(1) : mod1.substring(1, mod1.length()-1);
				String symbol2 = ( !mod2.endsWith("_") )? mod2.substring(1) : mod2.substring(1, mod2.length()-1);
				if ( !symbol1.equals(symbol2) )
					return Chemical.getAtomicNumber(symbol2) - Chemical.getAtomicNumber(symbol1);

				return 0;
			}
		});
		// TODO: remove "_"
//		System.err.println();
		for ( int i=0; i< modStrList.size(); i++ ) {
			// TODO: remove print
//			System.err.println(modStrList.get(i) + ":" + modStrList.get(i).substring(1, modStrList.get(i).length()) );
			modStrList.set( i, modStrList.get(i).replaceAll("_", "") );
		}
		String strMod1 = (modStrList.size() > 0)? modStrList.get(0) : null;
		String strMod2 = (modStrList.size() > 1)? modStrList.get(1) : null;
		String strMod3 = (modStrList.size() > 2)? modStrList.get(2) : null;

		// Check whether that the carbon is terget of stereo judgement
		int CType =
			( strOrbital.equals("sp3") &&  isTerminal && nUniq == 3 && !bridgeCons.isEmpty() )? SP3_TERMINAL    :
			( strOrbital.equals("sp3") && !isTerminal && nUniq == 2                          )? SP3_NONTERMINAL :
			( strOrbital.equals("sp2") &&  isTerminal && nUniq == 2 && iType1 == 2           )? SP2_TERMINAL    :
			( strOrbital.equals("sp2") && !isTerminal && nUniq == 1 && iType1 == 2           )? SP2_NONTERMINAL :
			OTHER;
		// Get stereo for backbone carbon
		if ( CType != OTHER ) {
			if ( CType == SP3_TERMINAL || CType == SP3_NONTERMINAL )
				strStereo = this.getSP3StereoForBackbone(CType, a_objCarbon, conC1, conC2, conList, bridgeCons);
			if ( CType == SP2_TERMINAL || CType == SP2_NONTERMINAL )
				strStereo = this.getSP2StereoForBackbone(CType, a_objCarbon, conC1, conC2, a_aChain, conList);
		}

		// Return matching CarbonDesctiptor
		return CarbonDescriptor_TBD.forCarbonSituation(strOrbital, iType1, iType2, nUniq, strStereo, !bridgeCons.isEmpty(), strMod1, strMod2, strMod3 );
	}

	// IUPACを参考にして、Stereo判定を行う対象かどうかのチェックをまず入れる。
	// ここでのStereoはIUPACのStereoの決定方法とは異なるので注意
	// 糖鎖の主鎖炭素と隣接原子の種類が等しければ、同一のSKeletoneCodeを出力させたい
	// この為、主鎖炭素と隣接する原子のみを対象として、立体判定を行っている。
	// また、EZ判定では、主鎖炭素の向きが2重結合の両端で等しいかどうかを判定基準として用いている。
	// WURCSを生成する過程において、修飾付加記号の部分で化合物全体の立体が一意になるようにする。
	/**
	 * Get string for sp3 carbon in backbone
	 * @param carbonType Integer of carbon type (sp3 terminal or sp3 non-terminal)
	 * @param C Target carbon
	 * @param conC1 Connection to backword carbon from target carbon
	 * @param conC2 Connection to forword carbon from target carbon
	 * @param Mods List of modification connections
	 * @param bridgeCons List of modification connections which are bridging carbon chain
	 * @return String of stereo
	 */
	private String getSP3StereoForBackbone(int carbonType, Atom C, Connection conC1, Connection conC2, LinkedList<Connection> Mods, final HashSet<Connection> bridgeCons ) {

		// Sort connection of modifications
		// bond type -> is bridge -> symbol
		Collections.sort(Mods, new Comparator<Connection> (){
			public int compare(Connection con1, Connection con2) {
				if( con1.getBond().getType() != con2.getBond().getType()  )
					return con2.getBond().getType() - con1.getBond().getType();
				if (  bridgeCons.contains(con1) && !bridgeCons.contains(con2) ) return -1;
				if ( !bridgeCons.contains(con1) &&  bridgeCons.contains(con2) ) return 1;
				if(!con1.endAtom().getSymbol().equals(con2.endAtom().getSymbol()) )
					return Chemical.getAtomicNumber(con2.endAtom().getSymbol()) - Chemical.getAtomicNumber(con1.endAtom().getSymbol());
				return 0;
			}
		});
		Connection conM1 = ( Mods.size() > 0 )? Mods.get(0) : null;
		Connection conM2 = ( Mods.size() > 1 )? Mods.get(1) : null;
		Connection conM3 = ( Mods.size() > 2 )? Mods.get(2) : null;

		String stereo = "";

		/************* sp3 terminal ***************
		 *    M1----C?  For terminal carbon in ring
		 *    |     |   M1 -> C1 -> M2 -> M3
		 * M3-C--M2 |   (priority: M2 > M3)
		 *    |     |
		 *    C1-??-C?
		 ******************************************/
		if( carbonType == SP3_TERMINAL ){
			Connection conC  = (conC1 != null)? conC1 : conC2;

//			if(mod3.connectedBackboneNum > 1){ M0 = mod3.connects.get(0); M1 = mod1.connects.get(0); M2 = mod2.connects.get(0); }
//			if(mod2.connectedBackboneNum > 1){ M0 = mod2.connects.get(0); M1 = mod1.connects.get(0); M2 = mod3.connects.get(0); }
//			if(mod1.connectedBackboneNum > 1){ M0 = mod1.connects.get(0); M1 = mod2.connects.get(0); M2 = mod3.connects.get(0); }
			if ( bridgeCons.contains(conM3) ) { Connection conTmp = conM1;  conM1 = conM3; conM3 = conM2; conM2 = conTmp; }
			if ( bridgeCons.contains(conM2) ) { Connection conTmp = conM1;  conM1 = conM2; conM2 = conTmp; }
//			if(backbone.indexOf(connectBackbone1.atom) < backbone.indexOf(atom)){
			if ( conC1 == null && conC2 != null ) {
				stereo = Chemical.sp3stereo(conC, conM1, conM2, conM3);
			} else {
				stereo = Chemical.sp3stereo(conM1, conC, conM2, conM3);
			}
		}

		/************* sp3 non-terminal ****************
		 *    C1        For non-terminal carbon in ring
		 *    |         C1 -> C2 -> M1 -> M2
		 * M2-C--M1-C?  (carbon number: C1 < C2)
		 *    |     |
		 *    C2-??-C?
		 ***********************************************/
		if( carbonType == SP3_NONTERMINAL ){
//			if(backbone.indexOf(connectBackbone1.atom) < backbone.indexOf(connectBackbone2.atom)){
//				stereo = Chemical.sp3stereo(conC1, conC2, conM1, conM2);
//			}else{
//				stereo = Chemical.sp3stereo(conC2, conC1, conM1, conM2);
//			}
			// Always number of C1 less than C2
			stereo = Chemical.sp3stereo(conC1, conC2, conM1, conM2);
		}
		return stereo;
	}

	/**
	 * Get string for sp2 carbon in backbone
	 * @param carbonType Integer of carbon type (sp3 terminal or sp3 non-terminal)
	 * @param C Target carbon
	 * @param conC1 Connection to backword carbon from target carbon
	 * @param conC2 Connection to forword carbon from target carbon
	 * @param chain Carbon chain
	 * @param conList List of modification connections
	 * @return String of stereo
	 */
	private String getSP2StereoForBackbone(int carbonType, Atom C, Connection conC1, Connection conC2, LinkedList<Atom> chain, LinkedList<Connection> conList ) {

		// Sort connection of modifications
		// bond type -> symbol
		Collections.sort(conList, new Comparator<Connection> (){
			public int compare(Connection con1, Connection con2) {
				if( con1.getBond().getType() != con2.getBond().getType()  )
					return con2.getBond().getType() - con1.getBond().getType();
				if(!con1.endAtom().getSymbol().equals(con2.endAtom().getSymbol()) )
					return Chemical.getAtomicNumber(con2.endAtom().getSymbol()) - Chemical.getAtomicNumber(con1.endAtom().getSymbol());
				return 0;
			}
		});
		Atom M1 = (conList.size() > 0)? conList.get(0).endAtom() : null;

		Atom A0 = C;
		Atom B0 = null;
		Atom A1 = null;
		Atom B1 = null;

		// sp2 terminal
		/**
		 *  M1      ?     M2      ?
		 *    \    /        \    /
		 *     C==C1   or    C==C1
		 *    /    \        /    \
		 *  M2      Cx    M1      Cx
		 */
		if ( carbonType == SP2_TERMINAL ) {
			B0 = ( conC1 != null )? conC1.endAtom() : conC2.endAtom();
			A1 = M1;
		}
		// sp2 non-terminal
		if ( carbonType == SP2_NONTERMINAL ) {
			B0 = ( conC1.getBond().getType() > conC2.getBond().getType() )? conC1.endAtom() : conC2.endAtom();
			A1 = ( conC1.getBond().getType() > conC2.getBond().getType() )? conC2.endAtom() : conC1.endAtom();
		}
		for ( Connection con : B0.getConnections() ) {
			B1 = con.endAtom();
			if ( B1.equals(A0) ) continue;
			if ( !chain.contains(B1) ) continue;
			break;
		}
		return Chemical.sp2stereo(A0, A1, B0, B1);

	}

	/** Get anomeric symbol */
	private char getAnomericSymbol(Backbone_TBD a_oBackbone, BackboneCarbon a_oAnomCarbon) {
		// Get configurational carbon
		int t_iAnomPos = a_oBackbone.getAnomericPosition();
		if ( t_iAnomPos == 0 ) return 'x';

		LinkedList<BackboneCarbon> t_aBCs = a_oBackbone.getBackboneCarbons();
		// XXX remove print
//		System.err.println(a_oBackbone.getSkeletonCode());
		// For reversed backbone
		boolean t_bIsReverse = false;
		if ( t_iAnomPos > t_aBCs.size()/2 ) {
			t_bIsReverse = true;
			Collections.reverse(t_aBCs);
			t_iAnomPos = t_aBCs.size()-t_iAnomPos+1;
		}

		BackboneCarbon t_oAnomRefCarbon = null;
		int t_nChiral = 0;
		for ( int i=0, n=t_aBCs.size(); i<n; i++ ) {
			BackboneCarbon t_oBC = a_oBackbone.getBackboneCarbons().get(i);
			if ( !t_oBC.isChiral() ) continue;
			if ( i != t_iAnomPos-1 ) t_nChiral++;
			t_oAnomRefCarbon = t_oBC;
			// XXX remove print
//			System.err.print(t_oBC.getDesctriptor().getChar());
			if ( t_nChiral == 4 ) break;
		}
		// XXX remove print
//		System.err.println();
		if ( t_bIsReverse )
			Collections.reverse(t_aBCs);

		// Determine anomeric charactor
		char anom = 'x';
		if ( t_oAnomRefCarbon == null ) return anom;
		if ( a_oAnomCarbon == null ) return anom;
		// XXX remove print
//		System.err.println(" "+bcConfig.getDesctriptor().getChar());

		char cRef = t_oAnomRefCarbon.getDesctriptor().getChar();
		char cAnom = a_oAnomCarbon.getDesctriptor().getChar();

		if ( cRef == 'x' || cRef == 'X' ) {
			return (cAnom == '3' || cAnom == '7')? 'b' :
				   (cAnom == '4' || cAnom == '8')? 'a' : anom;
		}
		if ( !Character.isDigit(cRef) ) return anom;
		int iConfig = Character.getNumericValue(cRef);

		if ( !Character.isDigit(cAnom) ) return anom;
		int iAnom = Character.getNumericValue(cAnom);

		// XXX remove print
//		System.err.println(pos + ":" + iAnom + " vs " + i +":"+ iConfig);
		anom = ( iConfig%2 == iAnom%2 )? 'a' : 'b';

		return anom;
	}
}
