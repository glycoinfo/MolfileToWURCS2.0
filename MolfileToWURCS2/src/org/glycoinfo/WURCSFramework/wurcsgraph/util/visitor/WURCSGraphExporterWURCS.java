package org.glycoinfo.WURCSFramework.wurcsgraph.util.visitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.wurcsgraph.Backbone;
import org.glycoinfo.WURCSFramework.wurcsgraph.LinkagePosition;
import org.glycoinfo.WURCSFramework.wurcsgraph.Modification;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSEdge;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSGraph;
import org.glycoinfo.WURCSFramework.wurcsgraph.util.comparator.WURCSEdgeComparator;

public class WURCSGraphExporterWURCS implements WURCSVisitor {

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
		if ( a_objBackbone.getAnomericPosition() != 0 )
			skeleton += "-" + a_objBackbone.getAnomericPosition() + a_objBackbone.getAnomericSymbol();

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
			skeleton += "_" + MOD;

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

		WURCSGraphTraverser t_objTraverser = this.getTraverser(this);
		t_objTraverser.traverseGraph(a_objGraph);
		for ( Modification mod : this.m_aGlycosidicModifications ) {
			this.makeMLU(mod);
		}
//		makeWURCS(t_objTraverser);
		System.err.println("WURCS="+this.m_strVersion+"/"+this.m_iBMUCounter+","+this.m_iMLUCounter+"/"+this.m_strBMUs+this.m_strMLUs);
	}

	@Override
	public WURCSGraphTraverser getTraverser(WURCSVisitor a_objVisitor) throws WURCSVisitorException {
		return new WURCSGraphTraverserTree(a_objVisitor);
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

		this.m_strMLUs += "_" + str;
	}

	private String makeMOD(Modification mod) {
		String MOD = "";
		LinkedList<WURCSEdge> edges = mod.getEdges();
		WURCSEdgeComparator edgeComp = new WURCSEdgeComparator();
		Collections.sort(edges, edgeComp);
		for ( WURCSEdge modEdge : edges ) {
			if ( !MOD.equals("") ) MOD += "-";
			MOD += this.makeLIP(modEdge);
		}
		String MAP = mod.getMAPCode();
		// Omittion
		if ( MAP.equals("*O") || MAP.equals("*=O") ) return null;
		if ( MAP.equals("*O*") ) MAP = "";
		MOD += MAP;
		return MOD;
	}

	private String makeLIP(WURCSEdge edge) {
		String COLINs = "";
		for ( LinkagePosition link : edge.getLinkages() ) {
			if (! COLINs.equals("") ) COLINs += "|";
			String COLIN = ""+link.getBackbonePosition();
			COLINs += COLIN;
		}
		return COLINs;
	}

}
