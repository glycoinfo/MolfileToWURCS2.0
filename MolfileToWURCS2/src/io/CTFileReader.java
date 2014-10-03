package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import sugar.chemicalgraph.Atom;
import sugar.chemicalgraph.Bond;
import sugar.chemicalgraph.Connection;
import sugar.chemicalgraph.Molecule;
import util.Chemical;

/**
 * Read CTfile(Sdfile, Molfile) and return Molcule
 * @author Masaaki Matsubara
 */
public class CTFileReader {
	//----------------------------
	// Member variable
	//----------------------------
	private String filepath;
	private String filename;
	private int totalRecodeNum;
	private BufferedReaderWithStdout br;
	private boolean sdfileOutput;
	private int recordNo; //record number of ctfile
	private HashMap<String, LinkedList<String>> data = new HashMap<String, LinkedList<String>>();

	//----------------------------
	// Constructor
	//----------------------------
	public CTFileReader(final String a_objFilepath, final boolean a_bSdfileOutput) {
		this.filepath = a_objFilepath;
		try{
			File file = new File(this.filepath);
			FileReader fr = new FileReader(file);
			this.br = new BufferedReaderWithStdout(fr);
			this.filename = file.getName();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		this.sdfileOutput = a_bSdfileOutput;
		this.recordNo = 1;
	}

	//----------------------------
	// Public method (non void)
	//----------------------------
	/**
	 * read a record from ctfile and store Molecule
	 * @return Molecule (or null if file has no more molecule)
	 */
	public Molecule getMolecule() {
		Molecule mol = new Molecule();

		while(true){
			// Header Block
			// IIPPPPPPPPMMDDYYHHmmddSSssssssssssEEEEEEEEEEEERRRRRR
			// 0000000000111111111122222222223333333333444444444455
			// 0123456789012345678901234567890123456789012345678901
			String line;

			line = br.readLine(sdfileOutput); if(line == null) return null;
			line = br.readLine(sdfileOutput); if(line == null) return null;
			line = br.readLine(sdfileOutput); if(line == null) return null;

			// Counts Line
			// aaabbblllfffcccsssxxxrrrpppiiimmmvvvvvv
			// 000000000011111111112222222222333333333
			// 012345678901234567890123456789012345678
			line = br.readLine(sdfileOutput, "$$$$");

			//201304170004 IssakuYAMADA
			if(line.length() < 18){  // 39 || !line.substring(34, 39).trim().equals("V2000")){
				//201304170004 IssakuYAMADA
				// Not ignore V2000 and others
				while(line!=null && !line.equals("$$$$")) line = br.readLine(sdfileOutput, "$$$$");
				if(line==null) return null;
				if(line.equals("$$$$")) return mol;
			}
			int AtomNum = Integer.parseInt(line.substring(0, 3).trim());
			int BondNum = Integer.parseInt(line.substring(3, 6).trim());

			// Atom Block
			// xxxxx.xxxxyyyyy.yyyyzzzzz.zzzz aaaddcccssshhhbbbvvvHHHrrriiimmmnnneee
			// 000000000011111111112222222222333333333344444444445555555555666666666
			// 012345678901234567890123456789012345678901234567890123456789012345678
			for(int AtomNo = 0; AtomNo<AtomNum; AtomNo++){
				line = br.readLine(sdfileOutput);

//				Atom atom = new Atom();
//				atom.molfileAtomNo = AtomNo + 1;
				double[] crd = new double[3];
				crd[0]  = Double.parseDouble(line.substring(0, 10).trim());
				crd[1] = Double.parseDouble(line.substring(10, 20).trim());
				crd[2] = Double.parseDouble(line.substring(20, 30).trim());
//				atom.coordinate = crd;
//				atom.symbol = line.substring(31, 34).trim();
				// TODO:
				String symbol = line.substring(31, 34).trim();

				// TODO:
				Atom atom = new Atom(symbol);
				atom.setCoordinate(crd);

				// mass difference -3, -2, -1, 0, 1, 2, 3, 4
				int massdiff = Integer.parseInt(line.substring(34, 36).trim());
//				atom.mass = atom.atomicNumber() + massdiff;
				// TODO:
				atom.setMass( Chemical.getAtomicNumber(symbol) + massdiff );

				// 0 = uncharged or value other than these, 1 = +3, 2 = +2, 3 = +1, 4 = doublet radical, 5 = -1, 6 = -2, 7 = -3
				int charge = Integer.parseInt(line.substring(36, 39).trim());
//				atom.charge = (charge==0) ? 0 : 4-charge;
				// TODO:
				atom.setCharge( (charge==0) ? 0 : 4-charge );

//				mol.atoms.add(atom);
				// TODO:
				mol.add(atom);
			}

			// Bond Block
			for(int BondNo = 0; BondNo<BondNum; BondNo++){
				line = br.readLine(sdfileOutput);
//				Atom atom0 = mol.atoms.get(Integer.parseInt(line.substring(0, 3).trim()) - 1);
//				Atom atom1 = mol.atoms.get(Integer.parseInt(line.substring(3, 6).trim()) - 1);
				// TODO:
				Atom atom0 = mol.getAtoms().get(Integer.parseInt(line.substring(0, 3).trim()) - 1);
				Atom atom1 = mol.getAtoms().get(Integer.parseInt(line.substring(3, 6).trim()) - 1);
				int type = Integer.parseInt(line.substring(6, 9).trim());
				//int stereo = Integer.parseInt(line.substring(9, 12).trim());
				//201304162349 IssakuYAMADA
				int stereo =0;
				if (line.length()>10)
				{
					stereo = Integer.parseInt(line.substring(9, 12).trim());
				}
				//201304162349 IssakuYAMADA

				Bond bond = new Bond(atom0, atom1, type, stereo);

//				mol.bonds.addLast(bond);
				// TODO:
				mol.add(bond);
			}

//			LinkedList<Atom> atomlist = mol.atoms;
			// TODO:
			LinkedList<Atom> atomlist = mol.getAtoms();
			// Properties Block
			// "M  CHG", "M  RAD" and "M  ISO" are readable
			while((line = br.readLine(sdfileOutput)) != null){
				if(line.length() < 6) continue;
				if(line.substring(0, 6).trim().equals("M  END")){
					break;
				}else if(line.substring(0, 6).trim().equals("M  CHG")){
					// Charge
					// M  CHGnn8 aaa vvv aaa vvv ...
					// 012345678901234567890123456789
					int Num = Integer.parseInt(line.substring(6, 9).trim());
					for(int No=0; No<Num; No++){
						int atomNo = Integer.parseInt(line.substring(10+No*8, 13+No*8).trim()) - 1;
						int value  = Integer.parseInt(line.substring(14+No*8, 17+No*8).trim());
//						atomlist.get(atomNo).charge = value;
						// TODO:
						atomlist.get(atomNo).setCharge(value);
					}
				}else if(line.substring(0, 6).trim().equals("M  RAD")){
					// Radical
					// M  CHGnn8 aaa vvv aaa vvv ...
					// 012345678901234567890123456789
					int Num = Integer.parseInt(line.substring(6, 9).trim());
					for(int No=0; No<Num; No++){
						int atomNo = Integer.parseInt(line.substring(10+No*8, 13+No*8).trim()) - 1;
						int value  = Integer.parseInt(line.substring(14+No*8, 17+No*8).trim());
//						atomlist.get(atomNo).radical = value;
						//TODO:
						atomlist.get(atomNo).setRadical(value);
					}
				}else if(line.substring(0, 6).trim().equals("M  ISO")){
					// Isotope
					// M  ISOnn8 aaa vvv aaa vvv ...
					// 012345678901234567890123456789
					int Num = Integer.parseInt(line.substring(6, 9).trim());
					for(int No=0; No<Num; No++){
						int atomNo = Integer.parseInt(line.substring(10+No*8, 13+No*8).trim()) - 1;
						int value  = Integer.parseInt(line.substring(14+No*8, 17+No*8).trim());
//						atomlist.get(atomNo).mass = value;
						// TODO:
						atomlist.get(atomNo).setMass(value);
					}
				}
			}

			// Count pi electron
			for(Atom atom : atomlist){
				int pi = 0;
//				String symbol = atom.symbol;
				// TODO:
				String symbol = atom.getSymbol();
				if(     symbol.equals("N")){ pi = 2; }
				else if(symbol.equals("O")){ pi = 2; }
				else if(symbol.equals("S")){ pi = 2; }
				int pi_num = 0;
//				for(Connection connection : atom.connections){
				// TODO:
				for(Connection connection : atom.getConnections()){
//					int type = connection.bond.type;
					// TODO:
					int type = connection.getBond().getType();
					if(     type == 2){ pi_num += 1; }
					else if(type == 3){ pi_num += 2; }
					else if(type == 4){ pi_num =  1; }
				}
				if(pi_num!=0) pi = pi_num;
//				if(atom.charge < 0) pi -= atom.charge * 2;
				// TODO:
				if(atom.getCharge() < 0) pi -= atom.getCharge() * 2;
//				atom.pi = pi;
				// TODO:
				atom.setNumberOfPiElectron(pi);
			}

			// Data item
			// > 25 <ALTERNATE.NAMES>                    // Data header
			// 1,2 CYCLOHEXANE-DICARBOXYLIC ACID TRANS,L // Data
			// HEXAHYDROPHTHALIC ACID TRANS,L            // Data
			//                                           // Blank line
			String key = "";
			while((line = br.readLine(sdfileOutput, "$$$$")) != null){
				if(line.equals("$$$$")){
					break;
				}else if(line.length() == 0){
					// Blank line
					continue;
				}else if(line.substring(0, 1).equals(">")){
					// Data header
					// > 25 <ALTERNATE.NAMES>
					// line.split("<")[0] = "> 25 ", line.split("<")[1] = "ALTERNATE.NAMES>"
					// line.split("<")[1].split(">")[0] = "ALTERNATE.NAMES"
					key = line.split("<")[1].split(">")[0];
//					if(!this.data.containsKey(key)){
						this.data.put(key, new LinkedList<String>());
//					}
				}else{
					// Data
					if(key.equals("")) continue;
					this.data.get(key).add(line);
				}
			}

			this.recordNo++;
			return mol;
		}
	}

	/**
	 * Get field data of field ID in sd file
	 * @return String of field data
	 */
	public String getFieldData(String fieldID) {
		String field = "";
		if( this.data.containsKey(fieldID) ){
			field = "";
			for(String data : this.data.get(fieldID)){
				field += data;
			}
		}else{
			field = this.filename;
			if(this.totalRecodeNum!=1) field += "_" + recordNo;
		}
		return field;
	}

	public int getRecordNo() {
		return this.recordNo;
	}

	public void close() throws IOException {
		this.br.close();
	}
}
