package carbohydrate;

import java.util.LinkedList;

/**
 * Class for component of Carbohydrate
 * @author MasaakiMatsubara
 *
 */
public class CarbohydrateComponent {
	/** Contained CarbonTypes */
	protected LinkedList<BackboneCarbon> m_aCarbons;
	/** Linkages */
	protected LinkedList<Linkage>        m_aLinkages = new LinkedList<Linkage>();

	/**
	 * Constructor
	 * @param a_aCarbons List of BackboneCarbon
	 */
	public CarbohydrateComponent(LinkedList<BackboneCarbon> a_aCarbons) {
		this.m_aCarbons = a_aCarbons;
	}

	/**
	 * Add CarbonType
	 * @param ctype CarbonType
	 * @return true if addition is succeed
	 */
	public boolean addBackboneCarbon( BackboneCarbon bc ) {
//		if ( this.m_aCarbonTypes.contains(ctype) ) return false;
		return this.m_aCarbons.add( bc );
	}

	/**
	 * Get list of BackboneCarbon in this component
	 * @return list of BackboneCarbon in this component
	 */
	public LinkedList<BackboneCarbon> getBackboneCarbons() {
		return this.m_aCarbons;
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
	public int getPosition( BackboneCarbon bc ) {
		return this.m_aCarbons.indexOf( bc );
	}
}
