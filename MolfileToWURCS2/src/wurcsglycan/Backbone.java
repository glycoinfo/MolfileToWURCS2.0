package wurcsglycan;


/**
 * Class for backbone of saccharide
 * @author MasaakiMatsubara
 *
 */
public class Backbone extends WURCSComponent{

	/** Anomeric carbon which assigned */
	private BackboneCarbon m_objAnomericCarbon = null;
	/** Configurational carbon which assigned D/L */
//	private LinkedList<BackboneCarbon> m_objConfigurationalCarbons = new LinkedList<BackboneCarbon>();

	/**
	 * Constructor
	 * @param a_aCarbons List of BackboneCarbon
	 */
/*
	public Backbone(LinkedList<BackboneCarbon> a_aCarbons) {
		// Get first anomeric like carbon
		for ( BackboneCarbon carbon : a_aCarbons ) {
			// Ignore last carbon
			if ( carbon.equals( a_aCarbons.getLast() ) ) continue;

			// Set anomeric carbon
			if ( this.m_objAnomericCarbon == null && carbon.isAnomeric() ) {
				this.m_objAnomericCarbon = carbon;
				break;
			}
		}
		this.m_aCarbons = a_aCarbons;
		int nChiral = 0;
		BackboneCarbon lastChiralCarbon = null;
		for ( BackboneCarbon carbon : this.m_aCarbons ) {
			// Check candidate configurational carbon(s)
			char desc = carbon.getDesctriptor().getChar();
			if ( desc == '1' || desc == '2' || desc == '3' || desc == '4' || desc == 'X' ) {
				nChiral++;
				lastChiralCarbon = carbon;
			}
			if ( nChiral == 4 ) {
				this.m_objConfigurationalCarbons.addLast(lastChiralCarbon);
				nChiral = 0;
			}
		}
		if ( lastChiralCarbon != null && !this.m_objConfigurationalCarbons.contains(lastChiralCarbon) )
			this.m_objConfigurationalCarbons.addLast(lastChiralCarbon);
	}
*/

	/** Get skeltone code from BackboneCarbons */
	public String getSkeletonCode() {
		String code = "";
		for ( BackboneCarbon cd : this.getBackboneCarbons() ) {
			code += cd.getDesctriptor().getChar();
		}
		return code;
	}

	public int getAnomericPosition() {
		if ( this.m_objAnomericCarbon == null ) return 0;
		return this.getBackboneCarbons().indexOf(this.m_objAnomericCarbon)+1;
	}

	@Override
	protected void checkAnomeric(BackboneCarbon bc) {
		// Set anomeric carbon
		if ( this.m_objAnomericCarbon == null && bc.isAnomeric() )
			this.m_objAnomericCarbon = bc;
	}

	/** Get anomeric symbol */
	public char getAnomericSymbol() {
		// Get configurational carbon
		int pos = this.getAnomericPosition();
		int i = 0;
		BackboneCarbon bcConfig = null;
		for ( BackboneCarbon bc : this.getBackboneCarbons() ) {
			if ( !bc.isChiral() ) continue;
			i++;
			bcConfig = bc;
			if ( i == pos + 4 ) break;
		}
		char anom = 'x';
		if ( bcConfig == null ) return anom;
		if ( this.m_objAnomericCarbon == null ) return anom;

		char configChar = bcConfig.getDesctriptor().getChar();
		if ( configChar == 'x' || configChar == 'X' ) return anom;
		if ( !Character.isDigit(configChar) ) return anom;
		int iConfig = Integer.valueOf(Character.toString(configChar));

		char anomChar = this.m_objAnomericCarbon.getDesctriptor().getChar();
		if ( !Character.isDigit(anomChar) ) return anom;
		int iAnom = Integer.valueOf(Character.toString(anomChar));
		System.err.println(pos + ":" + iAnom + " vs " + i +":"+ iConfig);
		anom = ( iConfig%2 == iAnom%2 )? 'a' : 'b';

		return anom;
	}

}
