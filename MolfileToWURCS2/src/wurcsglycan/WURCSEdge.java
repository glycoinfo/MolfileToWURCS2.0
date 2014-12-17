package wurcsglycan;

import java.util.LinkedList;

/**
 * Class for edge between Backbone and Modification
 * @author MasaakiMatsubara
 *
 */
public class WURCSEdge {

	private Backbone m_objBackbone;
	private Modification m_objModification;
	private LinkedList<LinkagePosition> m_aLinkages = new LinkedList<LinkagePosition>();

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

	public boolean addLinkage( LinkagePosition link ) {
		if ( this.m_aLinkages.contains( link ) ) return false;
		return this.m_aLinkages.add( link );
	}

	public LinkedList<LinkagePosition> getLinkages() {
		return this.m_aLinkages;
	}
}
