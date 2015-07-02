package chemicalgraph;

/**
 * Enum class for bond type
 * @see http://c4.cabrillo.edu/404/ctfile.pdf - p14
 * @author MasaakiMatsubara
 *
 */
public enum BondType {

	SINGLE             (1, 1), // single bond
	DOUBLE             (2, 2), // double bond
	TRIPLE             (3, 3), // triple bond
	AROMATIC           (4, 2), // aromatic
	SINGLE_OR_DOUBLE   (5, 0),
	SINGLE_OR_AROMATIC (6, 0),
	DOUBLE_OR_AROMATIC (7, 0),
	ANY                (8, 0);

	private int m_iValue;
	private int m_iMultiplicity;

	private BondType(int a_iBondType, int a_iMultiplicity){
		this.m_iValue = a_iBondType;
		this.m_iMultiplicity = a_iMultiplicity;
	}

	public int getValue() {
		return this.m_iValue;
	}

	public int getMultiplicity() {
		return this.m_iMultiplicity;
	}

	static public BondType forType(int a_iBondType) {
		for ( BondType t_oBT : BondType.values() ) {
			if ( t_oBT.getValue() != a_iBondType ) continue;
			return t_oBT;
		}
		return null;
	}
}
