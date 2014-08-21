package utility.visitor;

/**
 *
 * @author Masaaki Matsubara
 *
 */
public interface Visitable {
	public void accept (ChemicalGraphVisitor a_objVisitor) throws ChemicalGraphVisitorException;
}
