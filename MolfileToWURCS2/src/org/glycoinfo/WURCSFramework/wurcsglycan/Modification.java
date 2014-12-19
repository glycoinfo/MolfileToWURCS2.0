package org.glycoinfo.WURCSFramework.wurcsglycan;

import java.util.HashSet;

import org.glycoinfo.WURCSFramework.wurcsglycan.util.visitor.WURCSVisitor;
import org.glycoinfo.WURCSFramework.wurcsglycan.util.visitor.WURCSVisitorException;



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

	public boolean isAglycone() {
		if ( this.getEdges().isEmpty() ) return false;
		for ( WURCSEdge edge : this.getEdges() ) {
			if ( edge.getLinkages().size() == 1
				&& edge.getBackbone().getAnomericPosition() == edge.getLinkages().get(0).getBackbonePosition() ) continue;
			return false;
		}
		return true;
	}

	/**
	 *
	 * @return true if MAP code of the Modification is omission terget
	 */
	public boolean canOmit() {
		HashSet<String> omissions = new HashSet<String>();
		omissions.add("*O");
		omissions.add("*O*");
		omissions.add("*=O");
		if ( omissions.contains( this.m_strMAPCode ) ) return true;
		return false;
	}

	@Override
	public void accept(WURCSVisitor a_objVisitor) throws WURCSVisitorException {
		// TODO 自動生成されたメソッド・スタブ
		a_objVisitor.visit(this);
	}

}
