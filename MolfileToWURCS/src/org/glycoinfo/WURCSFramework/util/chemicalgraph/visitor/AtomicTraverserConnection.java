package org.glycoinfo.WURCSFramework.util.chemicalgraph.visitor;

import java.util.Iterator;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;
import org.glycoinfo.WURCSFramework.chemicalgraph.SubGraph;

public class AtomicTraverserConnection extends AtomicTraverser {

	public AtomicTraverserConnection(AtomicVisitor a_objVisitor) throws AtomicVisitorException {
		super(a_objVisitor);
	}

	/**
	 * @see AtomicTraverser.SubGraphTraverser#traverse(org.glycoinfo.WURCSFramework.chemicalgraph.Atom)
	 */
	@Override
	public void traverse(Atom a_objAtom) throws AtomicVisitorException {
		// traverse subtree
		Iterator<Connection> t_iterLinkages = a_objAtom.getConnections().iterator();
		while (t_iterLinkages.hasNext())
		{
			Connection t_linkChild = t_iterLinkages.next();
			//t_linkChild.accept(this.m_objVisitor);
			this.traverse(t_linkChild);
		}

	}

	/**
	 * @see AtomicTraverser.SubGraphTraverser#traverse(org.glycoinfo.WURCSFramework.chemicalgraph.Connection)
	 */
	@Override
	public void traverse(Connection a_objConnection)
			throws AtomicVisitorException {
		// TODO 自動生成されたメソッド・スタブ
		// callback of the function before subtree
		this.m_iState = AtomicTraverser.ENTER;
		a_objConnection.accept(this.m_objVisitor);

	}

	/**
	 * @see AtomicTraverser.SubGraphTraverser#traverseGraph(org.glycoinfo.WURCSFramework.chemicalgraph.SubGraph)
	 */
	@Override
	public void traverseGraph(SubGraph a_objGraph) throws AtomicVisitorException {
		Iterator<Atom> t_iterAtoms = a_objGraph.getAtoms().iterator();
		while ( t_iterAtoms.hasNext() )
		{
			this.traverse(t_iterAtoms.next());
		}

	}
}
