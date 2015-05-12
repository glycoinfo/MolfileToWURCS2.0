package org.glycoinfo.WURCSFramework.util.exchange;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.wurcs.graph.CarbonDescriptor;

import chemicalgraph.Atom;
import chemicalgraph.Connection;
import chemicalgraph.util.Chemical;
import chemicalgraph.util.analytical.CarbonIdentifier;

public class CarbonToDescriptor_TBD {

	public CarbonDescriptor convert(Atom a_objCarbon) {
		// Return unknown type if carbon has connections more than 4
		if ( a_objCarbon.getConnections().size() > 4 ) return CarbonDescriptor.XXX_UNKNOWN;

		CarbonIdentifier t_objIdentC = new CarbonIdentifier();

		// Get stereo (chirality)
		String strStereo = a_objCarbon.getChirality();
		String strOrbital = t_objIdentC.setAtom(a_objCarbon).getHybridOrbital0();

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

				// Compare symbol. Prioritize large atomic number.
				String symbol1 = ( !mod1.endsWith("_") )? mod1.substring(1) : mod1.substring(1, mod1.length()-1);
				String symbol2 = ( !mod2.endsWith("_") )? mod2.substring(1) : mod2.substring(1, mod2.length()-1);
				if ( !symbol1.equals(symbol2) )
					return Chemical.getAtomicNumber(symbol2) - Chemical.getAtomicNumber(symbol1);

				// Compare bridging or not
				if (  mod1.endsWith("_") && !mod2.endsWith("_") ) return -1;
				if ( !mod1.endsWith("_") &&  mod2.endsWith("_") ) return 1;

				return 0;
			}
		});
		// Remove "_"
//		System.err.println();
		for ( int i=0; i< modStrList.size(); i++ ) {
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
		return CarbonDescriptor.forCarbonSituation(strOrbital, iType1, iType2, nUniq, strStereo, !bridgeCons.isEmpty(), strMod1, strMod2, strMod3 );
	}

}
