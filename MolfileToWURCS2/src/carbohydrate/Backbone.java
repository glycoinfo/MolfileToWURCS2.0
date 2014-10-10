package carbohydrate;

import java.util.LinkedList;

/**
 * Class for backbone of saccharide
 * @author MasaakiMatsubara
 *
 */
public class Backbone extends CarbohydrateComponent{
	/** Anomeric carbon which assigned */
	private BackboneCarbon m_objAnomericCarbon = null;
	/** Configurational carbon which assigned D/L */
//	private LinkedList<BackboneCarbon> m_objConfigurationalCarbons = new LinkedList<BackboneCarbon>();

	public Backbone(LinkedList<BackboneCarbon> a_aCarbons) {
		super(a_aCarbons);

		for ( BackboneCarbon carbon : this.m_aCarbons ) {
			// Ignore last carbon
			if ( carbon.equals( this.m_aCarbons.getLast() ) ) continue;

			// Set anomeric carbon
			if ( this.m_objAnomericCarbon == null && carbon.isAnomeric() ) {
				this.m_objAnomericCarbon = carbon;
				break;
			}
		}
/*
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
*/
	}

	/** Get skeltone code from BackboneCarbons */
	public String getSkeletonCode() {
		String code = "";
		for ( BackboneCarbon cd : this.getBackboneCarbons() ) {
			code += cd.getDesctriptor().getChar();
		}
		return code;
	}

	/** Get anomeric symbol */
/*	public char getAnomericSymbol() {
		char anom = 'x';
		if ( this.m_objConfigurationalCarbons.isEmpty() ) return anom;
		if ( this.m_objAnomericCarbon == null ) return anom;

		char anomChar = this.m_objAnomericCarbon.getDesctriptor().getChar();
		char configChar = this.m_objConfigurationalCarbons.getFirst().getDesctriptor().getChar();
		if ( configChar == 'X' )
		anom = ( configChar == anomChar )? 'a' : 'b';

		return anom;
	}
*/
}
