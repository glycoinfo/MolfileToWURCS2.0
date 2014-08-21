package utility.visitor;

/**
 *
 * @author Masaaki Matsubara
 *
 */
public class ChemicalGraphVisitorException extends Exception {
    protected String m_strMessage;

    /**
     * @param message
     */
    public ChemicalGraphVisitorException(String a_strMessage)
    {
        super(a_strMessage);
        this.m_strMessage = a_strMessage;
    }

    /**
     * @param message
     */
    public ChemicalGraphVisitorException(String a_strMessage,Throwable a_objThrowable)
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
