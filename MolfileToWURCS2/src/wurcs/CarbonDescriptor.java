package wurcs;


/**
 * Class for carbon descriptor of backbone SkeletonCode
 * @author MasaakiMatsubara
 *
 */
public enum CarbonDescriptor {

	// Terminal
	SZ3_METHYL_L   ( 'm', "sp3", 1,0, 1, null, null, "-H", "-H", "-H" ), // -C(H)(H)(H)
	SZ3_METHYL_U   ( 'M', "sp3", 1,0, 1, null, null, "-X", "-X", "-X" ), // -C(X)(X)(X)
	SZ3_HYDROXYL_L ( 'h', "sp3", 1,0, 2, null, null, "-H", "-H", "-O" ), // -C(O)(H)(H)
	SZ3_HYDROXYL_U ( 'H', "sp3", 1,0, 2, null, null, "-H", "-H", "-X" ), // -C(X)(H)(H)
	SZ3_ACETAL     ( 'b', "sp3", 1,0, 2, null, null, "-O", "-O", "-X" ), // -C(X)(O)(O)
	SZ3_GEMINAL_W  ( 'W', "sp3", 1,0, 2, null, null, "-X", "-X", "-H" ), // -C(X)(X)(H)
	SZ3_GEMINAL_L  ( 'L', "sp3", 1,0, 2, null, null, "-X", "-X", "-X" ), // -C(X)(X)(Y)
	SZ3_CHIRAL_S   ( '1', "sp3", 1,0, 3,  "S", true, "-X", "-X", "-X" ), // -C(X)(Y)(Z)
	SZ3_CHIRAL_R   ( '2', "sp3", 1,0, 3,  "R", true, "-X", "-X", "-X" ), // -C(X)(Y)(Z)
	SZ3_CHIRAL_s   ( '3', "sp3", 1,0, 3,  "s", true, "-X", "-X", "-X" ), // -C(X)(Y)(Z)
	SZ3_CHIRAL_r   ( '4', "sp3", 1,0, 3,  "r", true, "-X", "-X", "-X" ), // -C(X)(Y)(Z)
	SZ3_CHIRAL_X   ( 'X', "sp3", 1,0, 3,  "X", true, "-X", "-X", "-X" ), // -C(X)(Y)(Z)
	SZ3_NORING_U   ( 'U', "sp3", 1,0, 3, null, false,"-X", "-X", "-H" ), // -C(X)(Y)(H)
	SZ3_NORING_R   ( 'R', "sp3", 1,0, 3, null, false,"-X", "-X", "-X" ), // -C(X)(Y)(Z)
	SZ2_ALDEHYDE_L ( 'o', "sp2", 1,0, 2, null, null, "=O", "-H", null ), // -C(=O)(H)
	SZ2_ACID_L     ( 'a', "sp2", 1,0, 2, null, null, "=O", "-O", null ), // -C(=O)(O)
	SZ2_ALDEHYDE_U ( 'O', "sp2", 1,0, 2, null, null, "=X", "-H", null ), // -C(=X)(H)
	SZ2_ACID_U     ( 'A', "sp2", 1,0, 2, null, null, "=X", "-X", null ), // -C(=X)(Y)
	DZ2_METHYLENE_L( 'v', "sp2", 2,0, 1, null, null, "-H", "-H", null ), // =C(H)(H)
	DZ2_ETHENE_L   ( 'c', "sp2", 2,0, 1, null, null, "-O", "-O", null ), // =C(O)(O)
	DZ2_METHYLENE_U( 'V', "sp2", 2,0, 1, null, null, "-X", "-X", null ), // =C(X)(X)
	DZ2_CISTRANS_E ( 'G', "sp2", 2,0, 2,  "E", null, "-X", "-H", null ), // =C(X)(H)
	DZ2_CISTRANS_Z ( 'I', "sp2", 2,0, 2,  "Z", null, "-X", "-H", null ), // =C(X)(H)
	DZ2_CISTRANS_N ( 'P', "sp2", 2,0, 2,  "N", null, "-X", "-H", null ), // =C(X)(H)
	DZ2_CISTRANS_X ( 'J', "sp2", 2,0, 2,  "X", null, "-X", "-H", null ), // =C(X)(H)
	DZ2_ETHENE_U   ( 'C', "sp2", 2,0, 2, null, null, "-X", "-X", null ), // =C(X)(Y)
	SZ1_XETHYNE    ( 'Y',  "sp", 1,0, 1, null, null, "#X", null, null ), // -C(#X)
	DZ1_ALLENE     ( 'q',  "sp", 2,0, 1, null, null, "=X", null, null ), // =C(=X)
	TZ1_ETHYNE_L   ( 't',  "sp", 3,0, 1, null, null, "-H", null, null ), // #C(H)
	TZ1_ETHYNE_U   ( 'T',  "sp", 3,0, 1, null, null, "-X", null, null ), // #C(X)

	// Non-terminal
	SS3_METHYNE    ( 'd', "sp3", 1,1, 1, null, null, "-H", "-H", null ), // -C(H)(H)-
	SS3_ACETAL     ( 'b', "sp3", 1,1, 1, null, null, "-O", "-O", null ), // -C(O)(O)-
	SS3_XMETHYNE   ( 'D', "sp3", 1,1, 1, null, null, "-X", "-X", null ), // -C(X)(X)-
	SS3_STEREO_S   ( '1', "sp3", 1,1, 2,  "S", null, "-X", "-X", null ), // -C(X)(Y)-
	SS3_STEREO_R   ( '2', "sp3", 1,1, 2,  "R", null, "-X", "-X", null ), // -C(X)(Y)-
	SS3_STEREO_s   ( '3', "sp3", 1,1, 2,  "s", null, "-X", "-X", null ), // -C(X)(Y)-
	SS3_STEREO_r   ( '4', "sp3", 1,1, 2,  "r", null, "-X", "-X", null ), // -C(X)(Y)-
	SS3_STEREO_X   ( 'X', "sp3", 1,1, 2,  "X", null, "-X", "-X", null ), // -C(X)(Y)-
	SS2_KETONE_L   ( 'k', "sp2", 1,1, 1, null, null, "=O", null, null ), // -C(=O)-
	SS2_KETONE_U   ( 'K', "sp2", 1,1, 1, null, null, "=X", null, null ), // -C(=X)-
	DS2_CISTRANS_EL( 'e', "sp2", 2,1, 1,  "E", null, "-H", null, null ), // =C(H)-
	DS2_CISTRANS_ZL( 'z', "sp2", 2,1, 1,  "Z", null, "-H", null, null ), // =C(H)-
	DS2_CISTRANS_NL( 'n', "sp2", 2,1, 1,  "N", null, "-H", null, null ), // =C(H)-
	DS2_CISTRANS_XL( 'f', "sp2", 2,1, 1,  "X", null, "-H", null, null ), // =C(H)-
	DS2_CISTRANS_EU( 'E', "sp2", 2,1, 1,  "E", null, "-X", null, null ), // =C(X)-
	DS2_CISTRANS_ZU( 'Z', "sp2", 2,1, 1,  "Z", null, "-X", null, null ), // =C(X)-
	DS2_CISTRANS_NU( 'N', "sp2", 2,1, 1,  "N", null, "-X", null, null ), // =C(X)-
	DS2_CISTRANS_XU( 'F', "sp2", 2,1, 1,  "X", null, "-X", null, null ), // =C(X)-
	DD1_ALLENE     ( 'q',  "sp", 2,2, 0, null, null, null, null, null ), // =C=
	TS1_ETHYNE     ( 'y',  "sp", 3,1, 0, null, null, null, null, null ), // #C-

	XXX_UNKNOWN    ( '?',  null, 0,0, 0, null, null, null, null, null ); // C???

	private char   m_strChar;
	private String m_strHybridOrbital;
	private int    m_iBondTypeCarbon1;
	private int    m_iBondTypeCarbon2;
	private int    m_nUniqueModification;
	private String m_strStereo;
	private Boolean m_bIsFootOfBridge;
	private String m_strModification1;
	private String m_strModification2;
	private String m_strModification3;

	/**
	 * Private constructor of CarbonDescriptor for a charactor of SkeltonCode
	 * @param a_strChar SkeltonCode character of target carbon
	 * @param a_strOrbital Hybrid orbital of target carbon
	 * @param a_strStereo Stereo of target carbon
	 * @param a_strX First symbol of atom connected target carbon (not backbone carbon)
	 * @param a_strY Second symbol of atom connected target carbon (not backbone carbon) or null
	 * @param a_strZ Third symbol of atom connected target carbon (not backbone carbon) or null
	 */
	private CarbonDescriptor( char a_strChar, String a_strOrbital, int a_iTypeC1, int a_iTypeC2, int a_nUniqMod, String a_strStereo, Boolean a_bIsFoot, String a_strMod1, String a_strMod2, String a_strMod3 ) {
		this.m_strChar = a_strChar;
		this.m_strHybridOrbital = a_strOrbital;
		this.m_iBondTypeCarbon1 = a_iTypeC1;
		this.m_iBondTypeCarbon2 = a_iTypeC2;
		this.m_nUniqueModification = a_nUniqMod;
		this.m_strStereo = a_strStereo;
		this.m_bIsFootOfBridge = a_bIsFoot;
		this.m_strModification1 = a_strMod1;
		this.m_strModification2 = a_strMod2;
		this.m_strModification3 = a_strMod3;
	}

	/** Get SkeletonCode character of the carbon */
	public char getChar() {
		return this.m_strChar;
	}

	/** Get String of hybrid orbital of the carbon */
	public String getHybridOrbital() {
		return this.m_strHybridOrbital;
	}

	/** Whether or not the carbon is terminal */
	public Boolean isTerminal() {
		if ( this.m_strChar == '?' ) return null;
		return ( this.m_iBondTypeCarbon2 == 0 );
	}

	/** Get number of unique modifications connected the carbon */
	public int getNumberOfUniqueModifications() {
		return this.m_nUniqueModification;
	}

	/** Get string of stereo */
	public String getStereo() {
		return this.m_strStereo;
	}

	/** Whether or not the carbon is foot of bridge */
	public Boolean isFootOfBridge() {
		return this.m_bIsFootOfBridge;
	}

	/**
	 * Get string of modification
	 * @param num Number of modification
	 * @return String of modification
	 */
	public String getModification(int num) {
		if (num == 1) return this.m_strModification1;
		if (num == 2) return this.m_strModification2;
		if (num == 3) return this.m_strModification3;
		return null;
	}

	/**
	 * Match and get CarbonDescriptor which correspond to a character of SkeltonCode
	 * @param cName A character of SkeltonCode
	 * @return
	 */
	public static CarbonDescriptor forCharacter(char cName) {
		for ( CarbonDescriptor cd : CarbonDescriptor.values() ) {
			if ( cd.m_strChar == cName ) return cd;
		}
		return CarbonDescriptor.XXX_UNKNOWN;
	}

	/**
	 * Match and get CarbonDescriptor which correspond to a character of SkeltonCode
	 * @param a_strOrbital
	 * @param a_iTypeC1
	 * @param a_iTypeC2
	 * @param a_nUniqMod
	 * @param a_strStereo
	 * @param a_bIsFoot
	 * @param a_strMod1
	 * @param a_strMod2
	 * @param a_strMod3
	 * @return
	 */
	public static CarbonDescriptor forCarbonStatus( String a_strOrbital, int a_iTypeC1, int a_iTypeC2, int a_nUniqMod, String a_strStereo, Boolean a_bIsFoot, String a_strMod1, String a_strMod2, String a_strMod3 ) {
		// Match values
		for ( CarbonDescriptor cd : CarbonDescriptor.values() ) {
			if ( cd.m_strHybridOrbital != null
				&& !cd.m_strHybridOrbital.equals( a_strOrbital ) ) continue;
			if (  cd.m_iBondTypeCarbon1 != a_iTypeC1             ) continue;
			if (  cd.m_iBondTypeCarbon2 != a_iTypeC2             ) continue;
			if (  cd.m_nUniqueModification != a_nUniqMod         ) continue;
			if ( cd.m_strStereo != null
				&& !cd.m_strStereo.equals( a_strStereo )         ) continue;
			if ( cd.m_bIsFootOfBridge != null
				&& !cd.m_bIsFootOfBridge.equals( a_bIsFoot )     ) continue;
			if ( !cd.matchModifications( a_strMod1, a_strMod2, a_strMod3 ) ) continue;
			return cd;
		}
		return CarbonDescriptor.XXX_UNKNOWN;
	}

	private boolean matchModifications( String Mod1, String Mod2, String Mod3 ) {

		// Set this modification strings to array
		String[] mMods = new String[3];
		mMods[0] = this.m_strModification1;
		mMods[1] = this.m_strModification2;
		mMods[2] = this.m_strModification3;
		// Set argument modification strings to array
		String[] aMods = new String[3];
		aMods[0] = Mod1;
		aMods[1] = Mod2;
		aMods[2] = Mod3;

		// Matching each modifications
		for ( int i=0; i<3; i++ ) {
			if ( mMods[i] == null ) {
				if ( aMods[i] == null ) return true;
				return false;
			}
			if ( aMods[i] == null ) return false;
			if ( !mMods[i].equals(aMods[i]) ) {
				if ( mMods[i].charAt(0) != aMods[i].charAt(0) ) return false;
				if ( mMods[i].charAt(1) != 'X' ) return false;
			}
		}

		return true;
	}
}
