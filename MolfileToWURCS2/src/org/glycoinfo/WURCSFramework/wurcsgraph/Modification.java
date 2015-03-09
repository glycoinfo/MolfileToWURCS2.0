package org.glycoinfo.WURCSFramework.wurcsgraph;

import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.wurcsgraph.visitor.WURCSVisitor;
import org.glycoinfo.WURCSFramework.wurcsgraph.visitor.WURCSVisitorException;



/**
 * Class for modification
 * @author MasaakiMatsubara
 *
 */
public class Modification extends WURCSComponent{

	public static final int UNKNOWN_REPEAT = -1;
	public static final int NO_REPEAT = 0;

	private String m_strMAPCode;
	/** minima count for this repeat unit ; -1 for unknown, 0 for no repeat */
	private int m_iMinRepeatCount = Modification.NO_REPEAT;
	/** maxima count for this repeat unit ; -1 for unknown, 0 for no repeat */
	private int m_iMaxRepeatCount = Modification.NO_REPEAT;

	public Modification( String MAPCode ) {
		this.m_strMAPCode = MAPCode;
	}

	public String getMAPCode() {
		return this.m_strMAPCode;
	}

	public void setMinRepeatCount(int a_nRepMin) {
		this.m_iMinRepeatCount = a_nRepMin;
	}

	public int getMinRepeatCount() {
		return this.m_iMinRepeatCount;
	}

	public void setMaxRepeatCount(int a_nRepMax) {
		this.m_iMaxRepeatCount = a_nRepMax;
	}

	public int getMaxRepeatCount() {
		return this.m_iMaxRepeatCount;
	}

	/**
	 * Whether or not this is an aglycone
	 * @return True or false
	 */
	public boolean isAglycone() {
		if ( this.getEdges().isEmpty() ) return false;
		for ( WURCSEdge edge : this.getEdges() ) {
			if ( edge.getLinkages().size() == 1
				&& edge.getBackbone().getAnomericPosition() == edge.getLinkages().getFirst().getBackbonePosition() ) continue;
			return false;
		}
		return true;
	}

	public boolean isGlycosidic() {
		LinkedList<WURCSEdge> edges = this.getEdges();
		if ( edges.isEmpty() || edges.size() == 1 ) return false;
		HashSet<Backbone> uniqBackbones = new HashSet<Backbone>();
		for ( WURCSEdge edge : edges ) {
			uniqBackbones.add( edge.getBackbone() );
		}
		if ( uniqBackbones.size() < 2 ) return false;
		return true;
	}

	/**
	 *
	 * @return true if MAP code of the Modification is omission terget
	 */
	public boolean canOmitMAP() {
		if ( this.m_strMAPCode.equals("*O") || this.m_strMAPCode.equals("*=O") || this.m_strMAPCode.equals("*O*") )
			return true;
		return false;
	}

	public Modification copy() {
		return new Modification(this.m_strMAPCode);
	}

	@Override
	public void accept(WURCSVisitor a_objVisitor) throws WURCSVisitorException {
		a_objVisitor.visit(this);
	}

}
