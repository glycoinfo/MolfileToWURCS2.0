package org.glycoinfo.ChemicalStructureUtility.io.MDLMOL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Bond;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Molecule;
import org.glycoinfo.ChemicalStructureUtility.util.Chemical;

/**
 * Class for reading CTfile(Sdfile, Molfile) and extract Molecule object
 * @author MasaakiMatsubara
 */
public class CTFileReader {
	//----------------------------
	// Member variable
	//----------------------------
	private String m_strfilePath;
	private String m_strFileName;
	private String m_strDirName;
	private int m_nTotalRecode;
	private BufferedReader m_brOutput;
	private String m_strMOLString;
	private int m_iRecordNo; //record number of ctfile
	private HashMap<String, LinkedList<String>> m_mapIDToData = new HashMap<String, LinkedList<String>>();
	private Molecule m_oMolecule;

	//----------------------------
	// Constructor
	//----------------------------
	public CTFileReader(final String a_objFilepath) {
		this.m_strfilePath = a_objFilepath;
		try{
			File file = new File(this.m_strfilePath);
			this.m_brOutput = new BufferedReader(new FileReader(file));
			this.m_strFileName = file.getName();
			this.m_strDirName = file.getParent();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		this.m_iRecordNo = 1;
	}

	/**
	 * Get field data of field ID in sd file
	 * @return String of field data
	 */
	public String getFieldData(String a_strFieldID) {
		String t_strField = "";
		if( this.m_mapIDToData.containsKey(a_strFieldID) ){
			t_strField = "";
			for(String t_strData : this.m_mapIDToData.get(a_strFieldID)){
				t_strField += t_strData;
			}
		}else{
			t_strField = this.m_strFileName;
			if(this.m_nTotalRecode!=1) t_strField += "_" + this.m_iRecordNo;
		}
		return t_strField;
	}

	public int getRecordNo() {
		return this.m_iRecordNo;
	}

	public String getFileName() {
		return this.m_strFileName;
	}

	public String getDirectoryName() {
		return this.m_strDirName;
	}

	public String getMOLString() {
		return this.m_strMOLString;
	}

	public void close() throws IOException {
		this.m_brOutput.close();
	}

	public boolean readNext() {
		this.m_strMOLString = "";
		this.m_oMolecule = this.readMolecule();
		return this.m_oMolecule != null;
	}

	public Molecule getMolecule() {
		return this.m_oMolecule;
	}

	/**
	 * read a record from ctfile and store Molecule
	 * @return Molecule (or null if file has no more molecule)
	 */
	private Molecule readMolecule() {
		Molecule t_oMol = new Molecule();

		while(true){
			// Header Block
			// IIPPPPPPPPMMDDYYHHmmddSSssssssssssEEEEEEEEEEEERRRRRR
			// 0000000000111111111122222222223333333333444444444455
			// 0123456789012345678901234567890123456789012345678901
			String t_strLine;

			t_strLine = this.readLine(); if(t_strLine == null) return null;
			t_strLine = this.readLine(); if(t_strLine == null) return null;
			t_strLine = this.readLine(); if(t_strLine == null) return null;

			// Counts Line
			// aaabbblllfffcccsssxxxrrrpppiiimmmvvvvvv
			// 000000000011111111112222222222333333333
			// 012345678901234567890123456789012345678
			t_strLine = this.readLine();

			//201304170004 IssakuYAMADA
			if(t_strLine.length() < 18){  // 39 || !line.substring(34, 39).trim().equals("V2000")){
				//201304170004 IssakuYAMADA
				// Not ignore V2000 and others
				while(t_strLine!=null && !t_strLine.equals("$$$$"))
					t_strLine = this.readLine();
				if(t_strLine==null) return null;
				if(t_strLine.equals("$$$$")) return t_oMol;
			}
			int t_nAtom = Integer.parseInt(t_strLine.substring(0, 3).trim());
			int t_nBond = Integer.parseInt(t_strLine.substring(3, 6).trim());

			// Atom Block
			// xxxxx.xxxxyyyyy.yyyyzzzzz.zzzz aaaddcccssshhhbbbvvvHHHrrriiimmmnnneee
			// 000000000011111111112222222222333333333344444444445555555555666666666
			// 012345678901234567890123456789012345678901234567890123456789012345678
			for(int i = 0; i<t_nAtom; i++){
				t_strLine = this.readLine();

				double[] crd = new double[3];
				crd[0] = Double.parseDouble(t_strLine.substring(0, 10).trim());
				crd[1] = Double.parseDouble(t_strLine.substring(10, 20).trim());
				crd[2] = Double.parseDouble(t_strLine.substring(20, 30).trim());
				String symbol = t_strLine.substring(31, 34).trim();

				Atom t_oAtom = new Atom(symbol);
				t_oAtom.setAtomID(i+1);
				t_oAtom.setCoordinate(crd);

				// mass difference -3, -2, -1, 0, 1, 2, 3, 4
				int massdiff = Integer.parseInt(t_strLine.substring(34, 36).trim());
				t_oAtom.setMass( Chemical.getAtomicNumber(symbol) + massdiff );

				// 0 = uncharged or value other than these, 1 = +3, 2 = +2, 3 = +1, 4 = doublet radical, 5 = -1, 6 = -2, 7 = -3
				int charge = Integer.parseInt(t_strLine.substring(36, 39).trim());
				t_oAtom.setCharge( (charge==0) ? 0 : 4-charge );

				t_oMol.add(t_oAtom);
			}

			// Bond Block
			for(int i = 0; i<t_nBond; i++){
				t_strLine = this.readLine();

				Atom atom0 = t_oMol.getAtoms().get(Integer.parseInt(t_strLine.substring(0, 3).trim()) - 1);
				Atom atom1 = t_oMol.getAtoms().get(Integer.parseInt(t_strLine.substring(3, 6).trim()) - 1);
				int type = Integer.parseInt(t_strLine.substring(6, 9).trim());
				//int stereo = Integer.parseInt(line.substring(9, 12).trim());
				//201304162349 IssakuYAMADA
				int stereo =0;
				if (t_strLine.length()>10)
				{
					stereo = Integer.parseInt(t_strLine.substring(9, 12).trim());
				}
				//201304162349 IssakuYAMADA

				Bond bond = new Bond(atom0, atom1, type, stereo);

				t_oMol.add(bond);
			}

			LinkedList<Atom> t_aAtomList = t_oMol.getAtoms();
			// Properties Block
			// "M  CHG", "M  RAD", "M  ISO" and "A  " are readable
			while((t_strLine = this.readLine()) != null){
				if(t_strLine.length() < 6) continue;
				if(t_strLine.substring(0, 6).trim().equals("M  END")){
					break;
				}else if(t_strLine.substring(0, 6).trim().equals("M  CHG")){
					// Charge
					// M  CHGnn8 aaa vvv aaa vvv ...
					// 012345678901234567890123456789
					int Num = Integer.parseInt(t_strLine.substring(6, 9).trim());
					for(int No=0; No<Num; No++){
						int atomNo = Integer.parseInt(t_strLine.substring(10+No*8, 13+No*8).trim()) - 1;
						int value  = Integer.parseInt(t_strLine.substring(14+No*8, 17+No*8).trim());
						t_aAtomList.get(atomNo).setCharge(value);
					}
				}else if(t_strLine.substring(0, 6).trim().equals("M  RAD")){
					// Radical
					// M  CHGnn8 aaa vvv aaa vvv ...
					// 012345678901234567890123456789
					int Num = Integer.parseInt(t_strLine.substring(6, 9).trim());
					for(int No=0; No<Num; No++){
						int atomNo = Integer.parseInt(t_strLine.substring(10+No*8, 13+No*8).trim()) - 1;
						int value  = Integer.parseInt(t_strLine.substring(14+No*8, 17+No*8).trim());
						t_aAtomList.get(atomNo).setRadical(value);
					}
				}else if(t_strLine.substring(0, 6).trim().equals("M  ISO")){
					// Isotope
					// M  ISOnn8 aaa vvv aaa vvv ...
					// 012345678901234567890123456789
					int Num = Integer.parseInt(t_strLine.substring(6, 9).trim());
					for(int No=0; No<Num; No++){
						int atomNo = Integer.parseInt(t_strLine.substring(10+No*8, 13+No*8).trim()) - 1;
						int value  = Integer.parseInt(t_strLine.substring(14+No*8, 17+No*8).trim());
						t_aAtomList.get(atomNo).setMass(value);
					}
				}else if(t_strLine.substring(0, 3).equals("A  ")){
					//atom alias
					// A  aaa
					// 0123456789
					int aliasAtomNo = Integer.parseInt(t_strLine.substring(3, 6).trim()) - 1;
					// read next line
					t_strLine = this.readLine();
					t_aAtomList.get(aliasAtomNo).setAliasName(t_strLine.trim());
				}
			}

			// Count pi electron
			for(Atom t_oAtom : t_aAtomList){
				int pi = 0;
				String symbol = t_oAtom.getSymbol();
				if(     symbol.equals("N")){ pi = 2; }
				else if(symbol.equals("O")){ pi = 2; }
				else if(symbol.equals("S")){ pi = 2; }
				int t_nPi = 0;
				for(Connection t_oCon : t_oAtom.getConnections()){
					int type = t_oCon.getBond().getType();
					if(     type == 2){ t_nPi += 1; }
					else if(type == 3){ t_nPi += 2; }
					else if(type == 4){ t_nPi =  1; }
				}
				if(t_nPi!=0) pi = t_nPi;
				if(t_oAtom.getCharge() < 0) pi -= t_oAtom.getCharge() * 2;
				t_oAtom.setNumberOfPiElectron(pi);
			}

			// Data item
			// > 25 <ALTERNATE.NAMES>                    // Data header
			// 1,2 CYCLOHEXANE-DICARBOXYLIC ACID TRANS,L // Data
			// HEXAHYDROPHTHALIC ACID TRANS,L            // Data
			//                                           // Blank line
			String key = "";
			while( (t_strLine = this.readLine()) != null){
				if(t_strLine.equals("$$$$")){
					break;
				}else if(t_strLine.length() == 0){
					// Blank line
					continue;
				}else if( t_strLine.length() > 2 && t_strLine.substring(0, 2).equals("> ")){
					// Data header
					// > 25 <ALTERNATE.NAMES>
					// line.split("<")[0] = "> 25 ", line.split("<")[1] = "ALTERNATE.NAMES>"
					// line.split("<")[1].split(">")[0] = "ALTERNATE.NAMES"
					key = t_strLine.split("<")[1].split(">")[0];
					this.m_mapIDToData.put(key, new LinkedList<String>());
				}else{
					// Data
					if(key.equals("")) continue;
					this.m_mapIDToData.get(key).add(t_strLine);
				}
			}

			this.m_iRecordNo++;
			return t_oMol;
		}
	}

	/**
	 * Read line using BufferedReader and store mol string
	 * @return A line read
	 */
	private String readLine() {
		String t_strLine = null;
		try {
			t_strLine = this.m_brOutput.readLine();
			if ( t_strLine == null ) return null;
			if ( !t_strLine.equals("$$$$") )
				this.m_strMOLString += t_strLine+"\n";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return t_strLine;
	}
}
