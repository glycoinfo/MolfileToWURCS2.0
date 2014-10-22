package wurcs;

import java.util.LinkedList;

/**
 * Abstract class for component of Carbohydrate
 * @author MasaakiMatsubara
 *
 */
public abstract class WURCSComponent {

	/** BackboneCarbons  */
	protected LinkedList<BackboneCarbon> m_aCarbons = new LinkedList<BackboneCarbon>();
	/** Edges between Backbone and Modification */
	protected LinkedList<WURCSEdge> m_aEdges = new LinkedList<WURCSEdge>();

	/**
	 * Add backbone carbon
	 * @param bc BackboneCarbon
	 * @return true if addition is succeed
	 */
	public boolean addBackboneCarbon( BackboneCarbon bc ) {
		if ( this.m_aCarbons.contains(bc) ) return false;
		this.checkAnomeric(bc);
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
	 * Add edge
	 * @param edge Edage
	 * @return true if addition is succeed
	 */
	public boolean addEdge( WURCSEdge edge ) {
		if ( this.m_aEdges.contains(edge) ) return false;
		return this.m_aEdges.add(edge);
	}

	/**
	 * Get list of edges
	 * @return list of edges
	 */
	public LinkedList<WURCSEdge> getEdges() {
		return this.m_aEdges;
	}

	/**
	 * Abstract method of check anomeric backbone carbon
	 * @param bc BackboneCarbon
	 */
	protected abstract void checkAnomeric(BackboneCarbon bc);
}
