package carbohydrate;

import sugar.chemicalgraph.Atom;
import sugar.chemicalgraph.Connection;
import util.analytical.AtomIdentifier;

public enum BackboneCarbonType {

	// Terminal
	S3_METHYL_L   ( "m", "sp3", 1, 0, null, 1,1,1,  "H",  "H",  "H" ), // -C(H)(H)(H)
	S3_METHYL_U   ( "M", "sp3", 1, 0, null, 1,1,1,  "X",  "X",  "X" ), // -C(X)(X)(X)
	S3_HYDROXYL_L ( "h", "sp3", 1, 0, null, 1,1,1,  "H",  "H",  "O" ), // -C(H)(H)(O)
	S3_HYDROXYL_U ( "H", "sp3", 1, 0, null, 1,1,1,  "H",  "H",  "X" ), // -C(H)(H)(X)
	S3_ACETAL     ( "b", "sp3", 1, 0, null, 1,1,1,  "O",  "O",  "X" ), // -C(O)(O)(X)
	S3_GEMINAL_W  ( "W", "sp3", 1, 0, null, 1,1,1,  "X",  "X",  "H" ), // -C(X)(X)(H)
	S3_GEMINAL_L  ( "L", "sp3", 1, 0, null, 1,1,1,  "X",  "X",  "Y" ), // -C(X)(X)(Y)
	S3_CHIRAL_S   ( "1", "sp3", 1, 0,  "S", 1,1,1,  "X",  "Y",  "Z" ), // -C(X)(Y)(Z)
	S3_CHIRAL_R   ( "2", "sp3", 1, 0,  "R", 1,1,1,  "X",  "Y",  "Z" ), // -C(X)(Y)(Z)
	S3_CHIRAL_s   ( "3", "sp3", 1, 0,  "s", 1,1,1,  "X",  "Y",  "Z" ), // -C(X)(Y)(Z)
	S3_CHIRAL_r   ( "4", "sp3", 1, 0,  "r", 1,1,1,  "X",  "Y",  "Z" ), // -C(X)(Y)(Z)
	S3_CHIRAL_X   ( "X", "sp3", 1, 0,  "X", 1,1,1,  "X",  "Y",  "Z" ), // -C(X)(Y)(Z)
	S3_UNKNOWN_U  ( "U", "sp3", 1, 0, null, 1,1,1,  "X",  "Y",  "H" ), // -C(X)(Y)(H)
	S3_UNKNOWN_R  ( "R", "sp3", 1, 0, null, 1,1,1,  "X",  "Y",  "Z" ), // -C(X)(Y)(Z)
	S2_ALDEHYDE_L ( "o", "sp2", 1, 0, null, 2,1,0,  "O",  "H", null ), // -C(=O)(H)
	S2_ACID_L     ( "a", "sp2", 1, 0, null, 2,1,0,  "O",  "O", null ), // -C(=O)(O)
	S2_ALDEHYDE_U ( "O", "sp2", 1, 0, null, 2,1,0,  "X",  "H", null ), // -C(=X)(H)
	S2_ACID_U     ( "a", "sp2", 1, 0, null, 2,1,0,  "O",  "O", null ), // -C(=X)(Y)
	D2_METHYLENE_L( "v", "sp2", 2, 0, null, 1,1,0,  "H",  "H", null ), // =C(H)(H)
	D2_ETHENE_L   ( "c", "sp2", 2, 0, null, 1,1,0,  "O",  "O", null ), // =C(O)(O)
	D2_ETHENE_U   ( "V", "sp2", 2, 0, null, 1,1,0,  "X",  "X", null ), // =C(X)(X)
	D2_CISTRANS_E ( "G", "sp2", 2, 0,  "E", 1,1,0,  "X",  "H", null ), // =C(X)(H)
	D2_CISTRANS_Z ( "I", "sp2", 2, 0,  "Z", 1,1,0,  "X",  "H", null ), // =C(X)(H)
	D2_CISTRANS_N ( "P", "sp2", 2, 0,  "N", 1,1,0,  "X",  "H", null ), // =C(X)(H)
	D2_CISTRANS_X ( "J", "sp2", 2, 0,  "X", 1,1,0,  "X",  "H", null ), // =C(X)(H)
	D2_METHYLENE_U( "C", "sp2", 2, 0, null, 1,1,0,  "X",  "Y", null ), // =C(X)(Y)
	S1_XETHYNE    ( "Y",  "sp", 1, 0, null, 3,0,0,  "X", null, null ), // -C(#X)
	D1_ALLENE     ( "q",  "sp", 2, 0, null, 2,0,0,  "X", null, null ), // =C(=X)
	T1_ETHYNE_L   ( "t",  "sp", 3, 0, null, 1,0,0,  "H", null, null ), // #C(H)
	T1_ETHYNE_U   ( "T",  "sp", 3, 0, null, 1,0,0,  "X", null, null ), // #C(X)

	// Non terminal
	SS3_METHYNE    ( "d", "sp3", 1, 1, null, 1,1,0,  "H",  "H", null ), // -C(O)(O)-
	SS3_ACETAL     ( "b", "sp3", 1, 1, null, 1,1,0,  "O",  "O", null ), // -C(O)(O)-
	SS3_XMETHYNE   ( "D", "sp3", 1, 1, null, 1,1,0,  "X",  "X", null ), // -C(O)(O)-
	SS3_STEREO_S   ( "1", "sp3", 1, 1,  "S", 1,1,0,  "X",  "Y", null ), // -C(O)(O)-
	SS3_STEREO_R   ( "2", "sp3", 1, 1,  "R", 1,1,0,  "X",  "Y", null ), // -C(O)(O)-
	SS3_STEREO_s   ( "3", "sp3", 1, 1,  "s", 1,1,0,  "X",  "Y", null ), // -C(O)(O)-
	SS3_STEREO_r   ( "4", "sp3", 1, 1,  "r", 1,1,0,  "X",  "Y", null ), // -C(O)(O)-
	SS3_STEREO_X   ( "X", "sp3", 1, 1,  "X", 1,1,0,  "X",  "Y", null ), // -C(O)(O)-
	SS2_KETONE_L   ( "k", "sp2", 1, 1, null, 2,0,0,  "O", null, null ), // -C(=O)-
	SS2_KETONE_U   ( "K", "sp2", 1, 1, null, 2,0,0,  "X", null, null ), // -C(=X)-
	DS2_CISTRANS_EL( "e", "sp2", 2, 1,  "E", 1,0,0,  "H", null, null ), // =C(H)-
	DS2_CISTRANS_ZL( "z", "sp2", 2, 1,  "Z", 1,0,0,  "H", null, null ), // =C(H)-
	DS2_CISTRANS_NL( "n", "sp2", 2, 1,  "N", 1,0,0,  "H", null, null ), // =C(H)-
	DS2_CISTRANS_XL( "f", "sp2", 2, 1,  "X", 1,0,0,  "H", null, null ), // =C(H)-
	DS2_CISTRANS_EU( "E", "sp2", 2, 1,  "E", 1,0,0,  "X", null, null ), // =C(X)-
	DS2_CISTRANS_ZU( "Z", "sp2", 2, 1,  "Z", 1,0,0,  "X", null, null ), // =C(X)-
	DS2_CISTRANS_NU( "N", "sp2", 2, 1,  "N", 1,0,0,  "X", null, null ), // =C(X)-
	DS2_CISTRANS_XU( "F", "sp2", 2, 1,  "X", 1,0,0,  "X", null, null ), // =C(X)-
	DD1_ALLENE     ( "F",  "sp", 2, 2, null, 0,0,0, null, null, null ), // =C=
	TS1_ETHYNE     ( "y",  "sp", 3, 1, null, 0,0,0, null, null, null ); // #C-

	private AtomIdentifier m_objIdent = new AtomIdentifier();

	private String m_strChar;
	private String m_strHybridOrbital;
	private int    m_iBondTypeCarbon1;
	private int    m_iBondTypeCarbon2;
	private String m_strStereo;
	private int    m_iBondTypeX;
	private int    m_iBondTypeY;
	private int    m_iBondTypeZ;
	private String m_strSymbolX;
	private String m_strSymbolY;
	private String m_strSymbolZ;

	/**
	 * Private constructor of CarbonType for target carbon
	 * @param a_strChar SkeltonCode character of target carbon
	 * @param a_strOrbital Hybrid orbital of target carbon
	 * @param a_strStereo Stereo of target carbon
	 * @param a_strX First symbol of atom connected target carbon (not backbone carbon)
	 * @param a_strY Second symbol of atom connected target carbon (not backbone carbon) or null
	 * @param a_strZ Third symbol of atom connected target carbon (not backbone carbon) or null
	 */
	private BackboneCarbonType( String a_strChar, String a_strOrbital, int a_iTypeC1, int a_iTypeC2, String a_strStereo, int a_iTypeX, int a_iTypeY, int a_iTypeZ, String a_strX, String a_strY, String a_strZ ) {
		this.m_strChar = a_strChar;
		this.m_strHybridOrbital = a_strOrbital;
		this.m_iBondTypeCarbon1 = a_iTypeC1;
		this.m_iBondTypeCarbon2 = a_iTypeC2;
		this.m_strStereo = a_strStereo;
		this.m_iBondTypeX = a_iTypeX;
		this.m_iBondTypeY = a_iTypeY;
		this.m_iBondTypeZ = a_iTypeZ;
		this.m_strSymbolX = a_strX;
		this.m_strSymbolY = a_strY;
		this.m_strSymbolZ = a_strZ;
	}

	public String getChar() {
		return this.m_strChar;
	}

	/**
	 * Get BackboneCarbonType for Carbon which SkeltonCode character
	 * @param a_objCarbon Carbon for SkeltonCode character
	 * @param a_objHeadCarbon Head side carbon connected a_objCarbon
	 * @param a_objTailCarbon Tail side carbon connected a_objCarbon
	 * @return ctype BackboneCarbonType
	 */
	public BackboneCarbonType forCarbon( String a_strStereo, Atom a_objCarbon, Atom a_objHeadCarbon, Atom a_objTailCarbon ) {
		// Set connected backbone carbon(s)
		Atom C1, C2;
		if ( a_objHeadCarbon == null && a_objTailCarbon == null ) {
			return null;
		} else if ( a_objHeadCarbon == null || a_objTailCarbon == null ) {
			// Terminal
			C1 = ( a_objHeadCarbon != null )? a_objHeadCarbon : a_objTailCarbon;
			C2 = null;
		} else {
			// Non-terminal
			C1 = a_objHeadCarbon;
			C2 = a_objTailCarbon;
		}

		// Set hybrid orbital
		this.m_objIdent.setAtom(a_objCarbon);
		String orbital = this.m_objIdent.getHybridOrbital();

		if ( a_objCarbon.getConnections().size() > 4 ) return null;

		// Set connection types and connected atom symbols
		int typeC1=0,typeC2=0;
		int typeX=0,typeY=0,typeZ=0;
		String symbolX=null,symbolY=null,symbolZ=null;
		String presymbolOrig="", presymbol="";
		for ( Connection con : a_objCarbon.getConnections() ) {
			Atom conatom = con.endAtom();
			int type = con.getBond().getType();
			if ( conatom.equals(C1) )               { typeC1 = type; continue; }
			if ( C2 != null && conatom.equals(C2) ) { typeC2 = type; continue; }

			// Check for connected atoms
			String symbolOrig = conatom.getSymbol();
			String symbol;
			if ( symbolOrig.equals("H") || symbolOrig.equals("O") ) {
				symbol = symbolOrig;
			} else if ( presymbolOrig.equals(symbolOrig)) {
				symbol = presymbol;
			} else {
				symbol = (presymbol.equals("X"))? "Y" : (presymbol.equals("Y"))? "Z" : "X";
			}
			// Set previous symbol
			presymbolOrig = symbolOrig;
			presymbol = symbol;

			if ( typeX < type || ( typeX == type && this.compareSymbol(symbolX, symbol) < 0 )) {
				typeZ = typeY; symbolZ = symbolY;
				typeY = typeX; symbolY = symbolX;
				typeX = type;  symbolX = symbol;
				if ( this.compareSymbol(symbolX, symbolY) < 0 ){
					symbol = symbolX;
					symbolX = symbolY;
					symbolY = symbol;
				}
				if ( this.compareSymbol(symbolY, symbolZ) < 0 ){
					symbol = symbolY;
					symbolY = symbolZ;
					symbolZ = symbol;
				}
				continue;
			}
			if ( typeY < type || ( typeY == type && this.compareSymbol(symbolY, symbol) < 0 ) ) {
				typeZ = typeY; symbolZ = symbolY;
				typeY = type;  symbolY = symbol;
				if ( this.compareSymbol(symbolY, symbolZ) < 0 ){
					symbol = symbolY;
					symbolY = symbolZ;
					symbolZ = symbol;
				}
				continue;
			}
			typeZ = type;  symbolZ = symbol;
		}
		if ( typeC1 < typeC2 ) {
			int tmp = typeC1;
			typeC1 = typeC2;
			typeC2 = tmp;
		}

		// Match values
		for ( BackboneCarbonType ctype : BackboneCarbonType.values() ) {
			if ( !ctype.m_strHybridOrbital.equals(orbital) ) continue;
			if (  ctype.m_iBondTypeCarbon1 != typeC1       ) continue;
			if (  ctype.m_iBondTypeCarbon2 != typeC2       ) continue;
			if ( !ctype.m_strStereo.equals(a_strStereo)    ) continue;
			if (  ctype.m_iBondTypeX != typeX              ) continue;
			if (  ctype.m_iBondTypeY != typeY              ) continue;
			if (  ctype.m_iBondTypeZ != typeZ              ) continue;
			if ( !this.matchSymbol(ctype.m_strSymbolX, symbolX) ) continue;
			if ( !this.matchSymbol(ctype.m_strSymbolY, symbolY) ) continue;
			if ( !this.matchSymbol(ctype.m_strSymbolZ, symbolZ) ) continue;
			return ctype;
		}
		return null;
	}

	private boolean matchSymbol( String symbol1, String symbol2 ) {
		if ( symbol1 == null ) {
			if( symbol2 == null ) return true;
			return false;
		}
		if ( symbol2 == null ) return false;
		if ( symbol1.equals(symbol2) ) return true;
		if ( symbol1.equals("X") || symbol1.equals("Y") || symbol1.equals("Z") ) {
			if ( symbol2.equals("H") || symbol2.equals("O") ) return true;
			return false;
		}
		return false;
	}

	private int compareSymbol( String symbol1, String symbol2 ) {
		if ( symbol1.equals(symbol2) ) return 0;
		if ( symbol1.equals("H") ) return -1;
		if ( symbol2.equals("H") ) return 1;
		if ( symbol1.equals("O") ) return -1;
		if ( symbol2.equals("O") ) return 1;
		if ( symbol1.equals("Z") ) return -1;
		if ( symbol2.equals("Z") ) return 1;
		if ( symbol1.equals("Y") ) return -1;
		if ( symbol2.equals("Y") ) return 1;
		return 0;
	}

}
