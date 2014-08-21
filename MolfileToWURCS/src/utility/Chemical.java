package utility;

import chemicalgraph.Atom;
import chemicalgraph.Connection;


/**
 * 原子番号の取得、金属原子判定、立体判定
 * @author KenichiTanaka
 */
public class Chemical {
	//----------------------------
	// Member variable
	//----------------------------
	private static double EPS = 0.0000001;
	
	//----------------------------
	// Public method
	//----------------------------
	/**
	 * return atomic number of input symbol.
	 * @param ElementSymbol
	 * @return AtomicNumber
	 */
	public static int GetAtomicNumber(String ElementSymbol) {
		String[] PeriodicTable = {
			"",
			"H",                                                                                                  "He", //   1 -   2
			"Li", "Be",                                                             "B",  "C",  "N",  "O",  "F",  "Ne", //   3 -  10
			"Na", "Mg",                                                             "Al", "Si", "P",  "S",  "Cl", "Ar", //  11 -  18
			"K" , "Ca", "Sc", "Ti", "V",  "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn", "Ga", "Ge", "As", "Se", "Br", "Kr", //  19 -  36
			"Rb", "Sr", "Y" , "Zr", "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In", "Sn", "Sb", "Te", "I",  "Xe", //  37 -  54
			"Cs", "Ba",                                                                                                 //  55 -  56
			"La", "Ce", "Pr", "Nd", "Pm", "Sm", "Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb", "Lu",                   //  57 -  71
			                  "Hf", "Ta", "W" , "Re", "Os", "Ir", "Pt", "Au", "Hg", "Tl", "Pb", "Bi", "Po", "At", "Rn", //  72 -  86
			"Fr", "Ra",                                                                                                 //  87 -  88
			"Ac", "Th", "Pa", "U", "Np", "Pu", "Am", "Cm", "Bk", "Cf", "Es", "Fm", "Md", "No", "Lr",                    //  89 - 103
			"A", "Q", "X", "?", "R"                                                                                     // 104 - 108
		};
		if(ElementSymbol.equals("D")) return 1;
		if(ElementSymbol.equals("T")) return 1;
		for(int atomNo = 0; atomNo < PeriodicTable.length; atomNo++){
			if(ElementSymbol.equals(PeriodicTable[atomNo])){
				return atomNo;
			}
		}
		return -1;
	}

	/**
	 * Return true if symbol is metal atom.
	 * @param symbol
	 * @return true if symbol is metal atom.
	 */
	public static boolean isMetal(String symbol){
		String[] MetalAtoms = {"Li", "Na", "K", "Rb", "Cs", "Be", "Mg", "Ca", "Sr", "Ba", "Ra"};
		for(int ii=0; ii<MetalAtoms.length; ii++){
			if(symbol.equals(MetalAtoms[ii])){
				return true;
			}
		}
		return false;
	}

	/**
	 * Stereo chemistry for tetrahedrally atom.
	 * 四面体中心原子のRS判定
	 * 四面体中心原子をoとし、oに結合している4原子をa, b, c ,dとする。
	 * dがoの真後ろ(紙面奥)に来るように配置して眺めた場合、a->b->cが右回転ならR、左回転ならSを返す。
	 * RS表記に用いる場合、CIP順位則によってoまわりの4原子に順位を付けた後、1:a, 2:b, 3:c, 4:dを割り当てる。
	 * <pre>
	 * R               S               X
	 *       a               a
	 *      /               /
	 *  c- o -(down)-d  b- o -(down)-d  unknown stereo
	 *      \               \
	 *       b               c
	 * </pre>
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @return stereo("R", "S" or "X")
	 */
	public static String sp3stereo(Connection a, Connection b, Connection c, Connection d){
		double[] unitA = a.unitVector3D();
		double[] unitD = d.unitVector3D();
		
		double[] vectorAD = new double[3];
		vectorAD[0] = unitD[0] - unitA[0];
		vectorAD[1] = unitD[1] - unitA[1];
		vectorAD[2] = unitD[2] - unitA[2];
		
		double result = Calculation.innerProduct(vectorAD, Calculation.outerProduct(a.unitVector3D(), b.unitVector3D(), c.unitVector3D()));
		
		if(Math.abs(result) < EPS){
			return "X";
		}
		return (result>0) ? "R" : "S";
	}

	/**
	 * Stereo chemistry for double bond.
	 * <pre>
	 * Z              E              N
	 * A1         B1  A1         *   A1         *
	 *   \       /      \       /      \       /
	 *    A0 = B0        A0 = B0        A0 = B0
	 *   /       \      /       \      /       \
	 *  *         *    *         B1   *         *
	 * </pre>
	 * @param A0
	 * @param A1
	 * @param B0
	 * @param B1
	 * @return stereo("E", "Z" or "X")
	 */
	public static String sp2stereo(Atom A0, Atom A1, Atom B0, Atom B1){
		for(Connection connection : A0.connections){
			if(connection.stereo==3 || connection.stereo==4){
				return "X";
			}
		}
		for(Connection connection : B0.connections){
			if(connection.stereo==3 || connection.stereo==4){
				return "X";
			}
		}
		if(B1 == null) return "N";
	
		double result = Calculation.innerProduct(Calculation.outerProduct(A0, A1, B0), Calculation.outerProduct(B0, A0, B1));
		
		if(Math.abs(result) < EPS){
			return "X";
		}
		return (result>0) ? "Z" : "E";
	}
	
	//The around atoms on the carbon atom in the backbone has only one valence for the each atom.
	/**
	 * Return valence of atom.
	 * @param symbol
	 * @return 1 if symbol contains in v1Atoms.
	 */
	public static int hasValence(String symbol){
		int val = -1;
		String[] Atoms = {"H","Li", "Na", "K", "Rb", "Cs", "Fr", "F", "Cl", "Br", "I" };
		for(int ii=0; ii<Atoms.length; ii++){
			if(symbol.equals(Atoms[ii])){
				val = 1;
				return val;
			}
		}
		return val;
	}
}
