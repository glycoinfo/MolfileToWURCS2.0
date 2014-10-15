package carbohydrate;

import java.util.LinkedList;

/**
 * Class for edge between Backbone and Modification
 * @author MasaakiMatsubara
 *
 */
public class Edge {

	private Backbone m_objBackbone;
	private Modification m_objModification;
	private LinkedList<Linkage> m_aLinkages = new LinkedList<Linkage>();

	public void setBackbone(Backbone backbone) {
		this.m_objBackbone = backbone;
	}

	public Backbone getBackbone() {
		return this.m_objBackbone;
	}

	public void setModification(Modification mod) {
		this.m_objModification = mod;
	}

	public Modification getModification() {
		return this.m_objModification;
	}

	public boolean addLinkageSite( Linkage linksite ) {
		if ( !this.m_aLinkages.contains( linksite ) ) return false;
		return this.m_aLinkages.add( linksite );
	}

	public LinkedList<Linkage> getLinkageSites() {
		return this.m_aLinkages;
	}


}
