package org.glycoinfo.WURCSFramework.wurcsglycan.util.visitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.wurcsglycan.Backbone;
import org.glycoinfo.WURCSFramework.wurcsglycan.LinkagePosition;
import org.glycoinfo.WURCSFramework.wurcsglycan.Modification;
import org.glycoinfo.WURCSFramework.wurcsglycan.WURCSEdge;
import org.glycoinfo.WURCSFramework.wurcsglycan.WURCSGraph;
import org.glycoinfo.WURCSFramework.wurcsglycan.util.comparator.WURCSEdgeComparator;

public class WURCSGlycanExporterWURCS implements WURCSVisitor {

	private String m_strVersion = "2.0";
	private int    m_iBMUCounter = 0;
	private int    m_iMLUCounter = 0;
	private String m_strBMUs = "";
	private String m_strMLUs = "";
	private LinkedList<Backbone> m_aBackbones = new LinkedList<Backbone>();
	private LinkedList<Modification> m_aGlycosidicModifications = new LinkedList<Modification>();

	public void visit(Backbone a_objBackbone) throws WURCSVisitorException {
		if ( !this.m_aBackbones.contains(a_objBackbone) ) this.m_aBackbones.addLast(a_objBackbone);

		String skeleton = a_objBackbone.getSkeletonCode();
		if ( a_objBackbone.getAnomericPosition() != 0 ) {
			skeleton += "-" + a_objBackbone.getAnomericPosition();
			skeleton += ":" + a_objBackbone.getAnomericSymbol();
		}
		LinkedList<WURCSEdge> edges = a_objBackbone.getEdges();
		WURCSEdgeComparator edgeComp = new WURCSEdgeComparator();
		Collections.sort(edges, edgeComp);
		HashSet<Modification> searchedMods = new HashSet<Modification>();
		for ( WURCSEdge edge : edges ) {
			Modification mod = edge.getModification();
			if ( searchedMods.contains(mod) ) continue;
			if ( mod.isGlycosidic() ) continue;

			String MOD = this.makeMOD(mod);
			if ( MOD == null ) continue;
			skeleton += "|" + MOD;

			searchedMods.add(mod);
			if ( !edge.isReverse() ) continue;
			System.err.println("has parent");
		}
		this.m_strBMUs += "["+skeleton+"]";
		System.err.println(skeleton);

		this.m_iBMUCounter++;
	}

	public void visit(Modification a_objModification) throws WURCSVisitorException {
		if ( !a_objModification.isGlycosidic() ) return;
		this.m_aGlycosidicModifications.addLast(a_objModification);
		this.m_iMLUCounter++;
	}

	public void visit(WURCSEdge a_objWURCSEdge) throws WURCSVisitorException {
		// nothing to do
	}

	@Override
	public void start(WURCSGraph a_objGraph) throws WURCSVisitorException {
		this.clear();

		WURCSGlycanTraverser t_objTraverser = this.getTraverser(this);
		t_objTraverser.traverseGraph(a_objGraph);
		for ( Modification mod : this.m_aGlycosidicModifications ) {
			this.makeMLU(mod);
		}
//		makeWURCS(t_objTraverser);
		System.err.println("WURCS="+this.m_strVersion+"/"+this.m_iBMUCounter+","+this.m_iMLUCounter+"/"+this.m_strBMUs+this.m_strMLUs);
	}

	@Override
	public WURCSGlycanTraverser getTraverser(WURCSVisitor a_objVisitor) throws WURCSVisitorException {
		return new WURCSGlycanTraverserTree(a_objVisitor);
	}

	@Override
	public void clear() {
		this.m_strVersion = "2.0";
		this.m_iBMUCounter = 0;
		this.m_iMLUCounter = 0;
		this.m_strBMUs = "";
		this.m_strMLUs = "";
		this.m_aBackbones = new LinkedList<Backbone>();

	}

	private void makeMLU(Modification mod) {
		String str = "";
		int nAnomeric = 0;
		LinkedList<WURCSEdge> edges = mod.getEdges();
		WURCSEdgeComparator edgeComp = new WURCSEdgeComparator();
		Collections.sort(edges, edgeComp);
		for ( WURCSEdge edge : edges ) {
			if ( !str.equals("") ) str += "_";
			Backbone backbone = edge.getBackbone();
			for ( LinkagePosition link : edge.getLinkages() ) {
				str += link.getCOLINCode(this.m_aBackbones.indexOf(backbone)+1,true);
				if ( link.getBackbonePosition() == backbone.getAnomericPosition() ) nAnomeric++;
			}
		}
		str += ( mod.getMAPCode().equals("*O*") )? "" : mod.getMAPCode();
		System.err.print(str);
		System.err.println((nAnomeric>0)? (nAnomeric>1)? " both of anomeric:"+nAnomeric : "" : " at non-anomeric" );

		this.m_strMLUs += "|" + str;
	}

	private String makeMOD(Modification mod) {
		String MOD = "";
		LinkedList<WURCSEdge> edges = mod.getEdges();
		WURCSEdgeComparator edgeComp = new WURCSEdgeComparator();
		Collections.sort(edges, edgeComp);
		for ( WURCSEdge modEdge : edges ) {
			if ( !MOD.equals("") ) MOD += "_";
			MOD += this.makeCOLIN(modEdge);
		}
		String MAP = mod.getMAPCode();
		// Omittion
		if ( MAP.equals("*O") || MAP.equals("*=O") ) return null;
		if ( MAP.equals("*O*") ) MAP = "";
		MOD += MAP;
		return MOD;
	}

	private String makeCOLIN(WURCSEdge edge) {
		String COLINs = "";
		int nLink = edge.getLinkages().size();
		for ( LinkagePosition link : edge.getLinkages() ) {
			String COLIN = ""+link.getBackbonePosition();
			if ( nLink > 1 ) COLIN = "("+COLIN+")";
			COLINs += COLIN;
		}
		return COLINs;
	}

}
