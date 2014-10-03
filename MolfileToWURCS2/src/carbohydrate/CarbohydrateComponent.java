package carbohydrate;

import java.util.LinkedList;

public class CarbohydrateComponent {
	/** Contained CarbonTypes */
	private LinkedList<CarbonType> m_aCarbonTypes = new LinkedList<CarbonType>();
	/** Linkages */
	private LinkedList<Linkage>    m_aLinkages = new LinkedList<Linkage>();

	/**
	 * Add CarbonType
	 * @param ctype CarbonType
	 * @return true if addition is succeed
	 */
	public boolean addBackboneCarbon( CarbonType ctype ) {
		if ( !this.m_aCarbonTypes.contains(ctype) ) return false;
		return this.m_aCarbonTypes.add( ctype );
	}

	/**
	 * Get list of CarbonTypes in this component
	 * @return list of CarbonTypes in this component
	 */
	public LinkedList<CarbonType> getCarbonTypes() {
		return this.m_aCarbonTypes;
	}

	/**
	 * Add linkage
	 * @param link Linkage
	 * @return true if addition is succeed
	 */
	public boolean addLinkages( Linkage link ) {
		if ( this.m_aLinkages.contains(link) ) return false;
		return this.m_aLinkages.add(link);
	}

	/**
	 * Get list of Linkages
	 * @return list of Linkages
	 */
	public LinkedList<Linkage> getLinkages() {
		return this.m_aLinkages;
	}

	/**
	 * Get position of connected carbon (CabonType) in this component
	 * @param ctype
	 * @return number of carbon position
	 */
	public int getPosition( CarbonType ctype ) {
		return this.m_aCarbonTypes.indexOf( ctype );
	}
}
