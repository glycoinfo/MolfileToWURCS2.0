package org.glycoinfo.WURCSFramework.wurcsglycan.util.visitor;

import org.glycoinfo.WURCSFramework.wurcsglycan.Backbone;
import org.glycoinfo.WURCSFramework.wurcsglycan.Modification;
import org.glycoinfo.WURCSFramework.wurcsglycan.WURCSGlycan;

public interface WURCSVisitor {
	public void visit( Backbone     a_objBackbone     ) throws WURCSVisitorException;
	public void visit( Modification a_objModification ) throws WURCSVisitorException;

	public void start( WURCSGlycan   a_objGraph       ) throws WURCSVisitorException;

	public WURCSTraverser getTraverser( WURCSVisitor a_objVisitor ) throws WURCSVisitorException;

	public void clear();

}
