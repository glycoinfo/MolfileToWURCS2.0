package org.glycoinfo.WURCSFramework.chemicalgraph;

/**
 * Enum class for bond stereo
 * @see http://c4.cabrillo.edu/404/ctfile.pdf - p14
 * @author MasaakiMatsubara
 *
 */public enum BondStereo {

	// For single bond
	SINGLE_NOT_STEREO(0, 1),
	SINGLE_UP        (1, 1),
	SINGLE_EITHER    (4, 1),
	SINGLE_DOWN      (6, 1),
	// For double bond
	DOUBLE_USE_XYZ   (0, 2),
	DOUBLE_CIS_TRANS (3, 2);

	private int m_iValue;
	private int m_iMaltiplicity;

	private BondStereo(int a_iValue, int a_iMultiplicity) {
		this.m_iValue = a_iValue;
		this.m_iMaltiplicity = a_iMultiplicity;
	}

	public int getValue() {
		return this.m_iValue;
	}

	public int getMultiplicity() {
		return this.m_iMaltiplicity;
	}

	public static BondStereo forStereoValue(int a_iValue, int a_iMultiplicity) {
		for ( BondStereo t_oBS : BondStereo.values() ) {
			if ( t_oBS.m_iValue        != a_iValue        ) continue;
			if ( t_oBS.m_iMaltiplicity != a_iMultiplicity ) continue;
			return t_oBS;
		}
		return null;
	}
}
