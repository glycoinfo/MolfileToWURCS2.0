package util.creator;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;

import sugar.chemicalgraph.Atom;
import sugar.chemicalgraph.Connection;
import util.analytical.CarbonIdentifier;
import carbohydrate.Backbone;
import carbohydrate.BackboneCarbon;
import carbohydrate.CarbonDescriptor;

/**
 * Class for create backbone
 * @author MasaakiMatsubara
 *
 */
public class BackboneCreator {

	private CarbonIdentifier m_objIdent = new CarbonIdentifier();

	public Backbone create(LinkedList<Atom> chain) {
		LinkedList<BackboneCarbon> carbons = new LinkedList<BackboneCarbon>();
		for ( Atom carbon : chain ) {
			CarbonDescriptor cd = this.convertCarbonToDescriptor(carbon, chain);
			carbons.addLast( new BackboneCarbon(cd, this.m_objIdent.setAtom(carbon).isAnomericLike()) );
		}
		return new Backbone(carbons);
	}

	/**
	 * Convert backbone carbon to CarbonDescriptor
	 * @param a_objCarbon a carbon in backbone
	 * @param a_aChain carbon chain of Backbone
	 * @return CarbonDescriptor
	 */
	private CarbonDescriptor convertCarbonToDescriptor( Atom a_objCarbon, LinkedList<Atom> a_aChain ) {
		// Get stereo (chirality)
		String strStereo = a_objCarbon.getChirality();
		String strOrbital = this.m_objIdent.setAtom(a_objCarbon).getHybridOrbital0();

		// Set connected backbone carbon(s)
		Atom C1=null, C2=null;
		if ( a_objCarbon == a_aChain.getFirst() ) {
			C1 = a_aChain.get(1);
		} else if ( a_objCarbon == a_aChain.getLast()  ) {
			C1 = a_aChain.get(a_aChain.size()-2);
		} else {
			int iC = a_aChain.indexOf(a_objCarbon);
			C1 = a_aChain.get(iC-1);
			C2 = a_aChain.get(iC+1);
		}

		if ( a_objCarbon.getConnections().size() > 4 ) return null;

		// Set modification string from connection types and connected atom symbols
		int iType1=0,iType2=0;
		LinkedList<String> modList = new LinkedList<String>();
		HashSet<String>    modUniq = new HashSet<String>();
		boolean bIsFoot = false;
		for ( Connection con : a_objCarbon.getConnections() ) {
			Atom conatom = con.endAtom();
			int type = con.getBond().getType();
			if ( conatom.equals(C1) )               { iType1 = type; continue; }
			if ( C2 != null && conatom.equals(C2) ) { iType2 = type; continue; }

			// Check that the carbon is foot of bridge
			for ( Connection concon : conatom.getConnections() ) {
				if ( a_objCarbon.equals(concon.endAtom()) ) continue;
				if ( a_aChain.contains(concon.endAtom()) ) {
					bIsFoot = true;
					break;
				}
			}

			String mod = ( type == 1 )? "-" : // single bond
						 ( type == 2 )? "=" : // double bond
						 ( type == 3 )? "#" : // triple bond
						 "?";
			mod += conatom.getSymbol();
			modUniq.add( mod + ((bIsFoot)? "b" : "") ); // Distinguish for bridge foot carbon
			modList.add( mod );
		}
		int nUniq=modUniq.size();

		// Swap bond type for backbone carbon
		if ( iType2 > iType1 ) { int type = iType2; iType2 = iType1; iType1 = type; }

		// Sort modification strings
		Collections.sort(modList, new Comparator<String>() {
			public int compare( String mod1, String mod2 ) {
				if ( mod1.equals(mod2) ) return 0;

				// Prioritize large number of bond order
				if ( mod1.charAt(0) == '#' && mod2.charAt(0) != '#' ) return -1;
				if ( mod1.charAt(0) != '#' && mod2.charAt(0) == '#' ) return 1;
				if ( mod1.charAt(0) == '=' && mod2.charAt(0) != '=' ) return -1;
				if ( mod1.charAt(0) != '=' && mod2.charAt(0) == '=' ) return 1;

				// For first mod is "O"
				if ( mod1.charAt(1) == 'O' ) {
					if ( mod2.charAt(1) == 'H' ) return -1;
					return 1;
				}
				// "H" is must
				if ( mod1.charAt(1) == 'H' ) return 1;

				return 0;
			}
		});
		String strMod1 = (modList.size() > 0)? modList.get(0) : null;
		String strMod2 = (modList.size() > 1)? modList.get(1) : null;
		String strMod3 = (modList.size() > 2)? modList.get(2) : null;

		// Return matching CarbonDesctiptor
		return CarbonDescriptor.forCarbonStatus(strOrbital, iType1, iType2, nUniq, strStereo, bIsFoot, strMod1, strMod2, strMod3 );
	}

}
