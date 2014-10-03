package util.analytical;

import sugar.chemicalgraph.Atom;
import sugar.chemicalgraph.Connection;

/**
 *
 * @author MasaakiMatsubara
 *
 */
public enum HybridOrbitalType {

	// sp3
	SP3_LP0( "sp3", 0, 4, 0, 0 ), // >C<
	SP3_LP1( "sp3", 1, 3, 0, 0 ), // -N<
	SP3_LP2( "sp3", 2, 2, 0, 0 ), // >O

	// sp3 for P
	SP3_P_LP0( "sp3", 0, 3, 1, 0 ), // >P(=)-
//	SP3_P_LP1( "sp3", 1, 3, 0, 0 ), // >P(:)- same to SP3_LP1
	// sp3 for S or Se
	SP3_S_LP0( "sp3", 0, 2, 2, 0 ), // >S(=)=
	SP3_S_LP1( "sp3", 1, 2, 1, 0 ), // >S(:)=
//	SP3_S_LP2( "sp3", 2, 2, 0, 0 ), // >S(:): same to SP3_LP2

	// sp2
	SP2_LP0( "sp2", 0, 2, 1, 0 ), // >N+=, >C=
	SP2_LP1( "sp2", 1, 1, 1, 0 ), // -N=
	SP2_LP2( "sp2", 2, 0, 1, 0 ), // =O

	// sp
	SP_LP0_13( "sp", 0, 1, 0, 1 ), // -C#
	SP_LP0_22( "sp", 0, 0, 2, 0 ), // =C=
	SP_LP1_13( "sp", 1, 0, 0, 1 ), // #N
	SP_XXX   ( "sp", null, null, 0, 0 ); // C??

	String  m_strName;
	Integer m_nLonePair;
	Integer m_nSingleBond;
	Integer m_nDoubleBond;
	Integer m_nTripleBond;

	private HybridOrbitalType( String name, Integer lp, Integer nSingle, Integer nDouble, Integer nTriple ) {
		this.m_strName  = name;
		this.m_nLonePair = lp;
		this.m_nSingleBond = nSingle;
		this.m_nDoubleBond = nDouble;
		this.m_nTripleBond = nTriple;
	}

	public String getName() {
		return this.m_strName;
	}

	/**
	 * Get hybrid orbital type of the atom from the valences.
	 * @param lp Number of lone pair which the atom has
	 * @param nSingle Number of single bond
	 * @param nDouble Number of double bond
	 * @param nTriple Number of triple bond
	 * @return HybridOrbitalType for the atom or null
	 */
	public HybridOrbitalType forNumBonds( Integer lp, Integer nSingle, Integer nDouble, Integer nTriple ) {
		for ( HybridOrbitalType orbital : HybridOrbitalType.values() ) {
			if ( orbital.m_nLonePair != lp ) continue;
			if ( orbital.m_nSingleBond  < nSingle ) continue;
			if ( orbital.m_nDoubleBond != nDouble ) continue;
			if ( orbital.m_nTripleBond != nTriple ) continue;
			return orbital;
		}
		return null;
	}

	public HybridOrbitalType forAtom( Atom atom ) {
		int nSingle = 0;
		int nDouble = 0;
		int nTriple = 0;
		for ( Connection con : atom.getConnections() ) {
			int nOrder = con.getBond().getType();
			if ( nOrder == 1 ) nSingle++;
			if ( nOrder == 2 ) nDouble++;
			if ( nOrder == 3 ) nTriple++;
		}
	}
}
