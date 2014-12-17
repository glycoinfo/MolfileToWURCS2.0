package wurcsglycan;

import java.util.LinkedList;

import wurcsglycan.util.visitor.WURCSVisitor;
import wurcsglycan.util.visitor.WURCSVisitorException;


/**
 * Class for backbone of saccharide
 * @author MasaakiMatsubara
 *
 */
public class Backbone extends WURCSComponent{

	/** BackboneCarbons  */
	private LinkedList<BackboneCarbon> m_aCarbons = new LinkedList<BackboneCarbon>();
	/** Anomeric carbon which assigned */
	private BackboneCarbon m_objAnomericCarbon = null;
	/** Configurational carbon which assigned D/L */
//	private LinkedList<BackboneCarbon> m_objConfigurationalCarbons = new LinkedList<BackboneCarbon>();

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

	/** Get skeltone code from BackboneCarbons */
	public String getSkeletonCode() {
		String code = "";
		for ( BackboneCarbon cd : this.m_aCarbons ) {
			code += cd.getDesctriptor().getChar();
		}
		return code;
	}

	public int getAnomericPosition() {
		if ( this.m_objAnomericCarbon == null ) return 0;
		return this.getBackboneCarbons().indexOf(this.m_objAnomericCarbon)+1;
	}

	/** Get anomeric symbol */
	public char getAnomericSymbol() {
		// Get configurational carbon
		int pos = this.getAnomericPosition();
		int i = 0;
		BackboneCarbon bcConfig = null;
		for ( BackboneCarbon bc : this.getBackboneCarbons() ) {
			if ( !bc.isChiral() ) continue;
			i++;
			bcConfig = bc;
			if ( i == pos + 4 ) break;
		}

		// Determine anomeric charactor
		char anom = 'x';
		if ( bcConfig == null ) return anom;
		if ( this.m_objAnomericCarbon == null ) return anom;

		char cConfig = bcConfig.getDesctriptor().getChar();
		char cAnom = this.m_objAnomericCarbon.getDesctriptor().getChar();

		if ( cConfig == 'x' || cConfig == 'X' ) {
			return (cAnom == '3' || cAnom == '7')? 'b' :
				   (cAnom == '4' || cAnom == '8')? 'a' : anom;
		}
		if ( !Character.isDigit(cConfig) ) return anom;
		int iConfig = Character.getNumericValue(cConfig);

		if ( !Character.isDigit(cAnom) ) return anom;
		int iAnom = Character.getNumericValue(cAnom);

		System.err.println(pos + ":" + iAnom + " vs " + i +":"+ iConfig);
		anom = ( iConfig%2 == iAnom%2 )? 'a' : 'b';

		return anom;
	}

	/**
	 * Get anomeric edge
	 * @return edge Edge on anomeric position
	 */
	public WURCSEdge getAnomericEdge() {
		for ( WURCSEdge edge : this.getEdges() ) {
			if ( edge.getLinkages().size()>1 ) continue;
			if ( edge.getLinkages().get(0).getBackbonePosition() != this.getAnomericPosition() ) continue;
			return edge;
		}
		return null;
	}

	private void checkAnomeric(BackboneCarbon bc) {
		// Set anomeric carbon
		if ( this.m_objAnomericCarbon == null && bc.isAnomeric() )
			this.m_objAnomericCarbon = bc;
	}

	@Override
	public void accept(WURCSVisitor a_objVisitor) throws WURCSVisitorException {
		// TODO 自動生成されたメソッド・スタブ
		a_objVisitor.visit(this);
	}

}
