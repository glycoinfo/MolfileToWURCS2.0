package org.glycoinfo.WURCSFramework.util.chemicalgraph.visitor;

/**
 * Interface for visitable components by SubGraphVisitor
 * @author MasaakiMatsubara
 *
 */
public interface AtomicVisitable {
	public void accept (AtomicVisitor a_objVisitor) throws AtomicVisitorException;
}
