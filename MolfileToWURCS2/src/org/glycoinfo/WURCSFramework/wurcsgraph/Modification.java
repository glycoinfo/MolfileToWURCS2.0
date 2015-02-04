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

	private String m_strMAPCode;

	public Modification( String MAPCode ) {
		this.m_strMAPCode = MAPCode;
	}

	public String getMAPCode() {
		return this.m_strMAPCode;
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
		HashSet<String> omissions = new HashSet<String>();
		omissions.add("*O");
		omissions.add("*O*");
		omissions.add("*=O");
		if ( omissions.contains( this.m_strMAPCode ) ) return true;
		return false;
	}

	public Modification copy() {
		return new Modification(this.m_strMAPCode);
	}

	@Override
	public void accept(WURCSVisitor a_objVisitor) throws WURCSVisitorException {
		// TODO 自動生成されたメソッド・スタブ
		a_objVisitor.visit(this);
	}

}
