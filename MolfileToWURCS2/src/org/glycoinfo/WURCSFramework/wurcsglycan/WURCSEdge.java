package org.glycoinfo.WURCSFramework.wurcsglycan;

import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.wurcsglycan.util.visitor.WURCSVisitable;
import org.glycoinfo.WURCSFramework.wurcsglycan.util.visitor.WURCSVisitor;
import org.glycoinfo.WURCSFramework.wurcsglycan.util.visitor.WURCSVisitorException;

/**
 * Class for edge between Backbone and Modification
 * @author MasaakiMatsubara
 *
 */
public class WURCSEdge implements WURCSVisitable {

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

	public WURCSEdge copy() {
		WURCSEdge copy = new WURCSEdge();
		for ( LinkagePosition link : this.m_aLinkages ) {
			copy.addLinkage( link.copy() );
		}
		return copy;
	}

	public void invertBackbonePositions() throws WURCSException {
		if ( this.getBackbone() == null )
			throw new WURCSException("Backbone must be set.");
		for ( LinkagePosition link : this.m_aLinkages ) {
			link.invertBackbonePosition(this.getBackbone().getBackboneCarbons().size());
		}
	}

	@Override
	public void accept(WURCSVisitor a_objVisitor) throws WURCSVisitorException {
		a_objVisitor.visit(this);
	}

}
