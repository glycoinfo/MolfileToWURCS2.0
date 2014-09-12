package chemicalgraph2;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * Class for atom
 * @author MasaakiMatsubara
 */
public class Atom {
	//----------------------------
	// Member variable
	//----------------------------
	/** List of connections from this atom to other elements.                 */
	private LinkedList<Connection> m_aConnections  = new LinkedList<Connection>();
	/** Atom symbol                                                           */
	private String   m_strSymbol           = null;
	/** Atom coordinates                                                      */
	private double[] m_dCoordinate         = new double[3];
	/** Charge (MolfileToWURCS set 0 this variable even if this information is written in input CTFile.)      */
	private int      m_iCharge             = 0;
	/** For isotope.(MolfileToWURCS set 0 this variable even if this information is written in input CTFile.) */
	private int      m_iMass               = 0;
	/** Radical (MolfileToWURCS does not use this variable.)                  */
	private int      m_iRadical            = 0;
	/** Number of pi electron                                                 */
	private int      m_iPiElectron        = 0;

	//----------------------------
	// Constructor
	//----------------------------
	public Atom(String symbol) {
		this.m_strSymbol = symbol;
	}

	//----------------------------
	// Accessor(getter)
	//----------------------------
	public LinkedList<Connection> getConnections() {
		return this.m_aConnections;
	}

	public String getSymbol() {
		return this.m_strSymbol;
	}

	public double[] getCoordinate() {
		return this.m_dCoordinate;
	}

	public int getCharge(){
		return this.m_iCharge;
	}

	public int getMass() {
		return this.m_iMass;
	}

	public int getRadical() {
		return this.m_iRadical;
	}

	public int getNumberOfPiElectron() {
		return m_iPiElectron;
	}

	//----------------------------
	// Accessor(setter)
	//----------------------------
	public void addConnection(Connection con) {
		this.m_aConnections.addLast(con);
	}

	public void setCoordinate(double[] crd) {
		this.m_dCoordinate = crd;
	}

	public void setCharge(int charge) {
		this.m_iCharge = charge;
	}

	public void setMass(int mass) {
		this.m_iMass = mass;
	}

	public void setRadical(int radical) {
		this.m_iRadical = radical;
	}

	public void setNumberOfPiElectron(int numPi) {
		this.m_iPiElectron = numPi;
	}

	//----------------------------
	// Public method
	//----------------------------
	public void sortConnections(Comparator<Connection> comparator) {
		Collections.sort(this.m_aConnections, comparator);
	}
}
