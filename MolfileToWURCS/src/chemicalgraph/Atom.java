package chemicalgraph;

import chemicalgraph.subgraph.aglycone.Aglycone;
import chemicalgraph.subgraph.backbone.Backbone;
import chemicalgraph.subgraph.modification.Modification;
import utility.Chemical;

/**
 * Class for atom.
 * @author KenichiTanaka
 */
public class Atom {
	//----------------------------
	// Member variable
	//----------------------------
	/** List of connections from this atom to other elements.                 */
	public ConnectionList connections  = new ConnectionList();
	/** Atom symbol                                                           */
	public String   symbol             = null;
	/** Atom coordinates                                                      */
	public double[] coordinate         = new double[3];
	/** Charge (MolfileToWURCS set 0 this variable even if this information is written in input CTFile.)      */
	public int      charge             = 0;
	/** For isotope.(MolfileToWURCS set 0 this variable even if this information is written in input CTFile.) */
	public int      mass               = 0;
	/** Radical (MolfileToWURCS does not use this variable.)                  */
	public int      radical            = 0;
	/** Number of pi electron                                                 */
	public int      pi                 = 0;
	/** Number in input CTFile                                                */
	public int      molfileAtomNo      = 0;

	/** If this atom is member of aromatic group, true is stored in this variable. */
	public boolean  isAromatic         = false;
	/** If this atom is member of cyclic sub structure which constructed by atoms with pi electron, true is stored in this variable. */
	public boolean  isPiCyclic         = false;
	/** If this atom is member of cyclic sub structure which constructed by carbon, true is stored in this variable.   */
	public boolean  isCarbonCyclic     = false;
	
	/** If this atom is member of carbon backbone, Chain object stored in this variable. otherwise null is stored.     */
	public Backbone    backbone           = null;
	/** If this atom is member of aglycone, Aglycone object stored in this variable. otherwise null is stored.         */
	public Aglycone aglycone           = null;
	/** If this atom is member of modification, Modification object stored in this variable. otherwise null is stored. */
	public Modification modification   = null;

	public int      initialECnumber    = 0;
	public int      subgraphECnumber   = 0;

	/** Stereo chemistry for tetrahedrally atom.                              */
	public String   stereoTmp          = null;
	/** Stereo chemistry for tetrahedrally atom.                              */
	public String   stereoMolecule     = null;
	/** Stereo chemistry for tetrahedrally atom.                              */
	public String   stereoModification = null;

	public int tmp = 0;

	//----------------------------
	// Public method (void)
	//----------------------------
	/**
	 * Add hidden hydrogen atoms and calculate coordinates of the atoms.
	 */
	public void addHiddenHydrogens(){
		// Add hidden hydrogen
		AtomList hiddenHydrogens = new AtomList();
		int hiddenHydrogenNumber = this.hiddenHydrogenNumber();
		for(int ii=0; ii<hiddenHydrogenNumber; ii++){
			Atom hiddenHydrogen = new Atom();
			hiddenHydrogen.symbol = "H";
			hiddenHydrogen.charge = 0;
    		new Bond(this, hiddenHydrogen, 1, 0);
    		hiddenHydrogens.add(hiddenHydrogen);
		}

		// Calculate coordinates
		if(hiddenHydrogens.size() == 1){
			double sumX = 0;
			double sumY = 0;
			double sumZ = 0;
			double sumBondLength = 0;
			int    sumBond = 0;
			for(Connection connection : this.connections){
				if(hiddenHydrogens.contains(connection.atom)) continue;
				double[] unitVector3D = connection.unitVector3D();
				sumX += unitVector3D[0];
				sumY += unitVector3D[1];
				sumZ += unitVector3D[2];
				sumBondLength += connection.length();
				sumBond++;
			}
    		Atom hiddenHydrogen = hiddenHydrogens.get(0);
    		hiddenHydrogen.coordinate[0] = this.coordinate[0] - (sumX * sumBondLength / sumBond);
    		hiddenHydrogen.coordinate[1] = this.coordinate[1] - (sumY * sumBondLength / sumBond);
    		hiddenHydrogen.coordinate[2] = this.coordinate[2] - (sumZ * sumBondLength / sumBond);
		}else{
			// hiddenHydrogenNumber = 1の場合と同じ処理を取りあえず記述
			double sumX = 0;
			double sumY = 0;
			double sumZ = 0;
			double sumBondLength = 0;
			int    sumBond = 0;
			for(Connection connection : this.connections){
				if(hiddenHydrogens.contains(connection.atom)) continue;
				double[] unitVector3D = connection.unitVector3D();
				sumX += unitVector3D[0];
				sumY += unitVector3D[1];
				sumZ += unitVector3D[2];
				sumBondLength += connection.length();
				sumBond++;
			}
			
			for(Atom hiddenHydrogen : hiddenHydrogens){
	    		hiddenHydrogen.coordinate[0] = this.coordinate[0] - (sumX * sumBondLength / sumBond);
	    		hiddenHydrogen.coordinate[1] = this.coordinate[1] - (sumY * sumBondLength / sumBond);
	    		hiddenHydrogen.coordinate[2] = this.coordinate[2] - (sumZ * sumBondLength / sumBond);
			}
		}
	}

	//----------------------------
	// Public method (non void)
	//----------------------------
	/**
	 * Return String of hybrid orbital "sp", "sp2", "sp3" or "".
	 * @return hybridOrbital("sp", "sp2", "sp3" or "")
	 */
	public String hybridOrbital(){
		// 立体判定に必要なケースのみ出力 ※InChIのドキュメントを参照
		// sp2
		if(this.is("C" , 0, 2, 1, 0)) return "sp2";
		if(this.is("Si", 0, 2, 1, 0)) return "sp2";
		if(this.is("Ge", 0, 2, 1, 0)) return "sp2";
		if(this.is("N" , 0, 1, 1, 0)) return "sp2";
		if(this.is("N" , 1, 2, 1, 0)) return "sp2";
		if(this.is("O",  0, 0, 1, 0)) return "sp2";
		
		// sp3
		if(this.is("B", -1, 4, 0, 0)) return "sp3";

		if(this.is("C",  0, 4, 0, 0)) return "sp3";
		if(this.is("Si", 0, 4, 0, 0)) return "sp3";
		if(this.is("Ge", 0, 4, 0, 0)) return "sp3";
		if(this.is("Sn", 0, 4, 0, 0)) return "sp3";

		if(this.is("N",  1, 4, 0, 0)) return "sp3";
		if(this.is("N",  0, 3, 1, 0)) return "sp3";
		if(this.is("N",  0, 3, 0, 0)) return "sp3";
		if(this.is("P",  1, 4, 0, 0)) return "sp3";
		if(this.is("P",  0, 3, 1, 0)) return "sp3";
		if(this.is("As", 1, 4, 0, 0)) return "sp3";

		if(this.is("O",  0, 2, 0, 0)) return "sp3";
		if(this.is("S",  1, 3, 1, 0)) return "sp3";
		if(this.is("S",  1, 3, 0, 0)) return "sp3";
		if(this.is("S",  0, 2, 2, 0)) return "sp3";
		if(this.is("S",  0, 2, 1, 0)) return "sp3";
		if(this.is("Se", 1, 3, 1, 0)) return "sp3";
		if(this.is("Se", 1, 3, 0, 0)) return "sp3";
		if(this.is("Se", 0, 2, 2, 0)) return "sp3";
		if(this.is("Se", 0, 2, 1, 0)) return "sp3";

		// sp ※InChIのドキュメントになかったので気がついたケースについて記述
		// 主鎖炭素判定に必要な分をとりあえず記述
		if(this.is("C" , 0, 1, 0, 1)) return "sp";
		if(this.is("C" , 0, 0, 2, 0)) return "sp";
		if(this.is("C" , null, null, 0, 0)) return "sp3";
		
		return "";
	}
	
	/**
	 * Return the number of hidden hydrogen.
	 * @return the number of hidden hydrogen
	 */
	public int hiddenHydrogenNumber(){
		// sp2
		if(this.is("C" , 0, 2, 1, 0)) return 3 - this.connections.size();
		if(this.is("Si", 0, 2, 1, 0)) return 3 - this.connections.size();
		if(this.is("Ge", 0, 2, 1, 0)) return 3 - this.connections.size();
		if(this.is("N" , 0, 1, 1, 0)) return 2 - this.connections.size();
		if(this.is("N" , 1, 2, 1, 0)) return 3 - this.connections.size();
		if(this.is("O",  0, 0, 1, 0)) return 1 - this.connections.size();

		// sp3
		if(this.is("B", -1, 4, 0, 0)) return 4 - this.connections.size();

		if(this.is("C",  0, 4, 0, 0)) return 4 - this.connections.size();
		if(this.is("Si", 0, 4, 0, 0)) return 4 - this.connections.size();
		if(this.is("Ge", 0, 4, 0, 0)) return 4 - this.connections.size();
		if(this.is("Sn", 0, 4, 0, 0)) return 4 - this.connections.size();

		if(this.is("N",  1, 4, 0, 0)) return 4 - this.connections.size();
		if(this.is("N",  0, 3, 1, 0)) return 4 - this.connections.size();
		if(this.is("N",  0, 3, 0, 0)) return 3 - this.connections.size();
		if(this.is("P",  1, 4, 0, 0)) return 4 - this.connections.size();
		if(this.is("P",  0, 3, 1, 0)) return 4 - this.connections.size();
		if(this.is("As", 1, 4, 0, 0)) return 4 - this.connections.size();

		if(this.is("O",  0, 2, 0, 0)) return 2 - this.connections.size();
		if(this.is("S",  1, 3, 1, 0)) return 4 - this.connections.size();
		if(this.is("S",  1, 3, 0, 0)) return 3 - this.connections.size();
		if(this.is("S",  0, 2, 2, 0)) return 4 - this.connections.size();
		if(this.is("S",  0, 2, 1, 0)) return 3 - this.connections.size();
		if(this.is("Se", 1, 3, 1, 0)) return 4 - this.connections.size();
		if(this.is("Se", 1, 3, 0, 0)) return 3 - this.connections.size();
		if(this.is("Se", 0, 2, 2, 0)) return 4 - this.connections.size();
		if(this.is("Se", 0, 2, 1, 0)) return 3 - this.connections.size();

		// sp ※InChIのドキュメントになかったので気がついたケースについて記述
		// 主鎖炭素判定に必要な分をとりあえず記述
		if(this.is("C" , 0, 1, 0, 1)) return 2 - this.connections.size();
		if(this.is("C" , 0, 0, 2, 0)) return 2 - this.connections.size();
		
		return 0;
	}

	/**
	 * Return the atomic number.
	 * @return the atomic number
	 */
	public int atomicNumber(){
		return Chemical.GetAtomicNumber(this.symbol);
	}
	
	/**
	 * Return the oxidation number.
	 * @return the oxidation number
	 */
	public int oxidationNumber(){
		int oxidationNumber = 0;
		for(Connection connection : this.connections){
			if(     connection.bond.type == 2){          oxidationNumber += 1; }
			else if(connection.bond.type == 3){          oxidationNumber += 2; }

			if(     connection.atom.symbol.equals("N")){ oxidationNumber += 1; }
			else if(connection.atom.symbol.equals("O")){ oxidationNumber += 1; }
			else if(connection.atom.symbol.equals("S")){ oxidationNumber += 1; }
		}
		return oxidationNumber;
	}
	
	/**
	 * Return true if the input conditions are satisfied
	 * @param symbol
	 * @param charge
	 * @param singleBondNum
	 * @param doubleBondNum
	 * @param tripleBondNum
	 * @return true if the input conditions are satisfied.
	 */
	public boolean is(String symbol, Integer charge, Integer singleBondNum, Integer doubleBondNum, Integer tripleBondNum){
		if((symbol != null) && !this.symbol.equals(symbol)) return false;
		if((charge != null) && this.charge != charge) return false;
		int bond1 = 0;
		int bond2 = 0;
		int bond3 = 0;
		for(Connection connection : this.connections){
			if(connection.bond.type == 1) bond1++;
			if(connection.bond.type == 2) bond2++;
			if(connection.bond.type == 3) bond3++;
		}
		if((singleBondNum != null) && bond1 >singleBondNum) return false;
		if((doubleBondNum != null) && bond2!=doubleBondNum) return false;
		if((tripleBondNum != null) && bond3!=tripleBondNum) return false;
		return true;
	}

	/**
	 * Return true if the atom is metal atom. Return false otherwise.
	 * @return true if the atom is metal atom. false otherwise.
	 */
	public boolean isMetal(){
		return Chemical.isMetal(this.symbol);
	}
	
	/**
	 * Return true if the atom is member of backbone. Return false otherwise.
	 * @return true if the atom is member of backbone. false otherwise.
	 */
	public boolean isBackbone(){
		return (this.backbone!=null || this.symbol.equals("*"));
	}
	
	/**
	 * Return true if the atom is member of modification. Return false otherwise.
	 * @return true if the atom is member of modification. false otherwise.
	 */
	public boolean isModification(){
		return this.modification != null;
	}
	
	/**
	 * Return true if the atom is member of aglycone. Return false otherwise.
	 * @return true if the atom is member of aglycone. false otherwise.
	 */
	public boolean isAglycone(){
		return this.aglycone != null;
	}

	/**
	 * Return true if the atom is terminal carbon of chemical subgraph which is not contain aromatic rings, ring of atoms with pi electron, ring of carbon and atom of non carbon. Return false otherwise.<br>
	 * 入力化学構造から以下に含まれる原子を取り除くことで得られる部分構造に対して、末端炭素である場合にtrueを返す。<br>
	 * 除外する構造：芳香環、π電子を持つ原子で構成される環、炭素原子で構成される環、炭素原子以外の原子<br>
	 * @return true if the atom is terminal carbon of chemical subgraph which is not contain aromatic rings, ring of atoms with pi electron, ring of carbon and atom of non carbon. false otherwise.
	 */
	public boolean isTerminalCarbon(){
		if(!this.symbol.equals("C")) return false;
		if( this.isAromatic) return false;
		if( this.isPiCyclic) return false;
		if( this.isCarbonCyclic) return false;
		int numC = 0;
		for(Connection connection : this.connections){
			if(connection.atom.symbol.equals("C") && !connection.atom.isAromatic && !connection.atom.isPiCyclic && !connection.atom.isCarbonCyclic){ numC++; }
		}
		return (numC==1) ? true : false;
	}
	
	/**
	 * Return true if the atom is oxygen of hydroxy group(R-OH). Return false otherwise.
	 * @return true if the atom is oxygen of hydroxy group(R-OH). false otherwise.
	 */
	public boolean isHydroxy(){
		int numOther = 0;
		int num1R = 0;
		if(!this.symbol.equals("O")) return false;
		for(Connection connection : this.connections){
			if(     connection.bond.type == 1 && connection.atom.symbol.equals("H")){ continue; }
			else if(connection.bond.type == 1){ num1R++; }
			else{ numOther++; }
		}
		return (num1R==1 && numOther==0) ? true : false;
	}
	
	/**
	 * Return true if the atom is nitrogen of (R-NHn). Return false otherwise.
	 * @return true if the atom is nitrogen of (R-NHn). false otherwise.
	 */
	public boolean isNHn(){
		int num1R = 0;
		int numOther = 0;
		if(!this.symbol.equals("N")) return false;
		for(Connection connection : this.connections){
			if(     connection.bond.type == 1 && connection.atom.symbol.equals("H")){ continue; }
			else if(connection.bond.type == 1){ num1R++; }
			else{ numOther++; }
		}
		return (num1R==1 && numOther==0) ? true : false;
	}
	
	/**
	 * Return true if the atom is carbon of acetal like group(R3-C(OR1)(OR2)-H). Return false otherwise.
	 * @return true if the atom is carbon of acetal like group(R3-C(OR1)(OR2)-H). false otherwise.
	 */
	public boolean isAcetalLike(){
		int num1C = 0;
		int num1NOS = 0;
		int numOther = 0;
		if(!this.symbol.equals("C")) return false;
		for(Connection connection : this.connections){
			if(     connection.bond.type == 1 && connection.atom.symbol.equals("H")){ continue; }
			else if(connection.bond.type == 1 && connection.atom.symbol.equals("C")){ num1C++; }
			else if(connection.bond.type == 1 && connection.atom.symbol.equals("N")){ num1NOS++; }
			else if(connection.bond.type == 1 && connection.atom.symbol.equals("O")){ num1NOS++; }
			else if(connection.bond.type == 1 && connection.atom.symbol.equals("S")){ num1NOS++; }
			else{ numOther++; }
		}
		return (num1C==1 && num1NOS==2 && numOther==0) ? true : false;
	}

	/**
	 * Return true if the atom is carbon of aldehyde like group. Return false otherwise.
	 * @return true if the atom is carbon of aldehyde like group. false otherwise.
	 */
	public boolean isAldehydeLike(){
		int num1C = 0;
		int num2NOS = 0;
		int numOther = 0;
		if(!this.symbol.equals("C")) return false;
		for(Connection connection : this.connections){
			if(     connection.bond.type == 1 && connection.atom.symbol.equals("H")){ continue; }
			else if(connection.bond.type == 1 && connection.atom.symbol.equals("C")){ num1C++; }
			else if(connection.bond.type == 2 && connection.atom.symbol.equals("N")){ num2NOS++; }
			else if(connection.bond.type == 2 && connection.atom.symbol.equals("O")){ num2NOS++; }
			else if(connection.bond.type == 2 && connection.atom.symbol.equals("S")){ num2NOS++; }
			else{ numOther++; }
		}
		return (num1C==1 && num2NOS==1 && numOther == 0) ? true : false;
	}

	/**
	 * Return true if the atom is carbon of carboxy like group(R-COOH). Return false otherwise.
	 * @return true if the atom is carbon of carboxy like group(R-COOH). false otherwise.
	 */
	public boolean isCarboxyLike(){
		int num1C = 0;
		int num1NOS = 0;
		int num2NOS = 0;
		int numOther = 0;
		if(!this.symbol.equals("C")) return false;
		for(Connection connection : this.connections){
			if(     connection.bond.type == 1 && connection.atom.symbol.equals("C")){ num1C++; }
			else if(connection.bond.type == 1 && connection.atom.symbol.equals("N")){ num1NOS++; }
			else if(connection.bond.type == 1 && connection.atom.symbol.equals("O")){ num1NOS++; }
			else if(connection.bond.type == 1 && connection.atom.symbol.equals("S")){ num1NOS++; }
			else if(connection.bond.type == 2 && connection.atom.symbol.equals("N")){ num2NOS++; }
			else if(connection.bond.type == 2 && connection.atom.symbol.equals("O")){ num2NOS++; }
			else if(connection.bond.type == 2 && connection.atom.symbol.equals("S")){ num2NOS++; }
			else{ numOther++; }
		}
		return (num1C==1 && num1NOS==1 && num2NOS==1 && numOther==0) ? true : false;
	}

	/**
	 * Return true if the atom is carbon of ketal like group, Ketal(R3-C(OR1)(OR2)-R4), HemiKetal(R3-C(OH) (OR2)-R4), Aminal(R1-C(NR2)(NR3)-R4) or HemiAminal(R1-C(NR2)(OR3)-R4). Return false otherwise.
	 * @return true if the atom is carbon of Ketal(R3-C(OR1)(OR2)-R4), HemiKetal(R3-C(OH) (OR2)-R4), Aminal(R1-C(NR2)(NR3)-R4) or HemiAminal(R1-C(NR2)(OR3)-R4). false otherwise.
	 */
	public boolean isKetalLike(){
		int num1C = 0;
		int num1NOS = 0;
		int numOther = 0;
		if(!this.symbol.equals("C")) return false;
		for(Connection connection : this.connections){
			if(     connection.bond.type == 1 && connection.atom.symbol.equals("H")){ continue; }
			else if(connection.bond.type == 1 && connection.atom.symbol.equals("C")){ num1C++; }
			else if(connection.bond.type == 1 && connection.atom.symbol.equals("N")){ num1NOS++; }
			else if(connection.bond.type == 1 && connection.atom.symbol.equals("O")){ num1NOS++; }
			else if(connection.bond.type == 1 && connection.atom.symbol.equals("S")){ num1NOS++; }
			else{ numOther++; }
		}
		return (num1C==2 && num1NOS==2 && numOther==0) ? true : false;
	}

	/**
	 * Return true if the atom is carbon of ketone like group. Return false otherwise.
	 * @return true if the atom is carbon of ketone like group. false otherwise.
	 */
	public boolean isKetoneLike(){
		int num2NOS = 0;
		if(!this.symbol.equals("C")) return false;
		for(Connection connection : this.connections){
			if(     connection.bond.type == 2 && connection.atom.symbol.equals("N")){ num2NOS++; }
			else if(connection.bond.type == 2 && connection.atom.symbol.equals("O")){ num2NOS++; }
			else if(connection.bond.type == 2 && connection.atom.symbol.equals("S")){ num2NOS++; }
		}
		return (num2NOS==1) ? true : false;
	}

	/**
	 * Return true if the atom is carbon of lactone like group. Return false otherwise.
	 * @return true if the atom is carbon of lactone like group. false otherwise.
	 */
	public boolean isLactoneLike(){
		int num1C = 0;
		int num1NOS = 0;
		int num2NOS = 0;
		int numOther = 0;
		if(!this.symbol.equals("C")) return false;
		for(Connection connection : this.connections){
			if(     connection.bond.type == 1 && connection.atom.symbol.equals("C")){ num1C++; }
			else if(connection.bond.type == 1 && connection.atom.symbol.equals("N")){ num1NOS++; }
			else if(connection.bond.type == 1 && connection.atom.symbol.equals("O")){ num1NOS++; }
			else if(connection.bond.type == 1 && connection.atom.symbol.equals("S")){ num1NOS++; }
			else if(connection.bond.type == 2 && connection.atom.symbol.equals("N")){ num2NOS++; }
			else if(connection.bond.type == 2 && connection.atom.symbol.equals("O")){ num2NOS++; }
			else if(connection.bond.type == 2 && connection.atom.symbol.equals("S")){ num2NOS++; }
			else{ numOther++; }
		}
		return (num1C==1 && num1NOS==1 && num2NOS==1 && numOther==0) ? true : false;
	}

	public int getMaxDepth(AtomList ancestors) {
		int maxdepth = ancestors.size();
		for(Connection connection : this.connections){
			if(ancestors.contains(connection.atom)) continue;
			ancestors.addLast(connection.atom);
			maxdepth = Math.max(connection.atom.getMaxDepth(ancestors), maxdepth);
			ancestors.removeLast();
		}
		return maxdepth;
	}

	public boolean isAnomer() {
		if(!this.isBackbone()) return false;
		return (this.backbone.getAnomer() == this) ? true : false;
	}
}
