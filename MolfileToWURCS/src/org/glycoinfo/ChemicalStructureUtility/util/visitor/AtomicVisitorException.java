package org.glycoinfo.ChemicalStructureUtility.util.visitor;

/**
 * Exception class for visitor of SubGraph
 * @author MasaakiMatsubara
 *
 */
public class AtomicVisitorException extends Exception {
	protected String m_strMessage;

	/**
	 * @param message
	 */
	public AtomicVisitorException(String a_strMessage)
	{
		super(a_strMessage);
		this.m_strMessage = a_strMessage;
	}

	/**
	 * @param message
	 */
	public AtomicVisitorException(String a_strMessage,Throwable a_objThrowable)
	{
		super(a_strMessage,a_objThrowable);
		this.m_strMessage = a_strMessage;
	}

	public String getErrorMessage()
	{
		return this.m_strMessage;
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
}
