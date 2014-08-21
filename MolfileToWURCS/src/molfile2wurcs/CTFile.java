package molfile2wurcs;

import glycan.Glycan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;

import chemicalgraph.Atom;
import chemicalgraph.Bond;
import chemicalgraph.Connection;
import chemicalgraph.subgraph.backbone.Backbone;
import chemicalgraph.subgraph.backbone.BackboneList;
import chemicalgraph.subgraph.molecule.Molecule;
import chemicalgraph.subgraph.molecule.MoleculeList;


import utility.BufferedReaderWithStdout;

/**
 * CTfile(Sdfile, Molfile)を入力し、1レコード毎に、データを読み込みWURCSを出力する。
 * @author KenichiTanaka
 * @author IssakuYAMADA
 */
public class CTFile{
	//----------------------------
	// Member variable
	//----------------------------
	public String filepath;
	public String filename;
	public int totalRecodeNum;
	BufferedReaderWithStdout br;

	//----------------------------
	// Public method (void)
	//----------------------------
	/**
	 * @param fieldID
	 * @param minNOS
	 * @param minO
	 * @param minChainLength
	 * @param maxChainLength
	 * @param sdfileOutput
	 * @param mols
	 */
	public void importV2000(final String fieldID, final int minNOS, final int minO, final int minChainLength, final int maxChainLength, final boolean sdfileOutput, final MoleculeList mols, final float ratioBackboneNOS){  // Issaku YAMADA
		try{
			File file = new File(this.filepath);
			FileReader fr = new FileReader(file);
			br = new BufferedReaderWithStdout(fr);
			this.filename = file.getName();
	        int recodeNo = 1;

				
	        while(true){
	        	// CTFileから1レコード読み込む
	        	Molecule mol = this.getMolecure(br, fieldID, sdfileOutput, recodeNo);
	        	if(mol==null) break;
	        	if(!sdfileOutput){
					System.out.print(mol.ID);
	        	}
	        	
	        	// WURCSの生成を行う
	        	mol.generateWURCS(minNOS, minO, minChainLength, maxChainLength, ratioBackboneNOS);  //Issaku YAMADA
	        	
	        	// 結果を出力する。
				if(sdfileOutput){
					System.out.println("> <WURCS>");
					for(Glycan glycan : mol.glycans){
						System.out.println(glycan.WURCS);
					}
					System.out.println();
					System.out.println("> <MainChain>");
					for(BackboneList backbones : mol.candidateBackboneGroups){
						for(Backbone backbone : backbones){
							if(!backbone.isBackbone) continue;
							System.out.println(backbone.molfileAtomNos());
						}
					}
					System.out.println();
					System.out.println("$$$$");
				}else{
					for(Glycan glycan : mol.glycans){
						System.out.print("\t" + glycan.WURCS);
					}
					System.out.println();
				}
	        	recodeNo++;
	        	if(mols!=null) mols.add(mol);
	        }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	//----------------------------
	// Private method (non void)
	//----------------------------
	/**
	 * ctfileから1レコード分読み込みMoleculeに格納
	 * @param br ctfile
	 * @param fieldID value of "&gt  &ltfieldID&gt" is used as ID.
	 * @param sdfileOutput
	 * @param recodeNo recode number of ctfile
	 * @return Molecule
	 */
	private Molecule getMolecure(final BufferedReaderWithStdout br, final String fieldID, final boolean sdfileOutput, final int recodeNo) {
		Molecule mol = new Molecule();
        
		mol.filepath = this.filepath;
		mol.filename = this.filename;

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
				// V2000形式以外は無視しないように変更（CTFileに大体あってれば読み込む設定とした。）
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

				Atom atom = new Atom();
				atom.molfileAtomNo = AtomNo + 1;
				atom.coordinate[0] = Double.parseDouble(line.substring(0, 10).trim());
				atom.coordinate[1] = Double.parseDouble(line.substring(10, 20).trim());
				atom.coordinate[2] = Double.parseDouble(line.substring(20, 30).trim());
				atom.symbol = line.substring(31, 34).trim();

				// mass difference -3, -2, -1, 0, 1, 2, 3, 4
				int massdiff = Integer.parseInt(line.substring(34, 36).trim());
				atom.mass = atom.atomicNumber() + massdiff;
				
				// 0 = uncharged or value other than these, 1 = +3, 2 = +2, 3 = +1, 4 = doublet radical, 5 = -1, 6 = -2, 7 = -3
				int charge = Integer.parseInt(line.substring(36, 39).trim()); 
				atom.charge = (charge==0) ? 0 : 4-charge;

				mol.atoms.add(atom);
			}
			
			// Bond Block
			for(int BondNo = 0; BondNo<BondNum; BondNo++){
				line = br.readLine(sdfileOutput);

				Atom atom0 = mol.atoms.get(Integer.parseInt(line.substring(0, 3).trim()) - 1);
				Atom atom1 = mol.atoms.get(Integer.parseInt(line.substring(3, 6).trim()) - 1);
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
				
				mol.bonds.addLast(bond);
			}

			// Properties Block
			// "M  CHG", "M  RAD", "M  ISO"に対応
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
						mol.atoms.get(atomNo).charge = value;
					}
				}else if(line.substring(0, 6).trim().equals("M  RAD")){
					// Radical
					// M  CHGnn8 aaa vvv aaa vvv ...
					// 012345678901234567890123456789
					int Num = Integer.parseInt(line.substring(6, 9).trim());
					for(int No=0; No<Num; No++){
						int atomNo = Integer.parseInt(line.substring(10+No*8, 13+No*8).trim()) - 1;
						int value  = Integer.parseInt(line.substring(14+No*8, 17+No*8).trim());
						mol.atoms.get(atomNo).radical = value;
					}
				}else if(line.substring(0, 6).trim().equals("M  ISO")){
					// Isotope
					// M  ISOnn8 aaa vvv aaa vvv ...
					// 012345678901234567890123456789
					int Num = Integer.parseInt(line.substring(6, 9).trim());
					for(int No=0; No<Num; No++){
						int atomNo = Integer.parseInt(line.substring(10+No*8, 13+No*8).trim()) - 1;
						int value  = Integer.parseInt(line.substring(14+No*8, 17+No*8).trim());
						mol.atoms.get(atomNo).mass = value;
					}
				}
			}
			
			// Count pi electron
			for(Atom atom : mol.atoms){
				atom.pi = 0;
				if(     atom.symbol.equals("N")){ atom.pi = 2; }
				else if(atom.symbol.equals("O")){ atom.pi = 2; }
				else if(atom.symbol.equals("S")){ atom.pi = 2; }
				int pi_num = 0;
				for(Connection connection : atom.connections){
		    		if(     connection.bond.type == 2){ pi_num += 1; }
		    		else if(connection.bond.type == 3){ pi_num += 2; }
		    		else if(connection.bond.type == 4){ pi_num = 1; }
				}
				if(pi_num!=0) atom.pi = pi_num;
				if(atom.charge < 0) atom.pi -= atom.charge * 2;
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
					String DataHeader[] = line.split("<");    // tmp[0] = "> 25 ", tmp[1] = "ALTERNATE.NAMES>"
					String DataHeader_tmp[] = DataHeader[1].split(">"); // tmp2[0] = "ALTERNATE.NAMES"
					key = DataHeader_tmp[0];
					if(!mol.data.containsKey(key)){
						LinkedList<String> val = new LinkedList<String>();
						mol.data.put(key, val);
					}
				}else{
					// Data
					if(key.equals("")) continue;
					mol.data.get(key).add(line);
				}
			}
			
			if(mol.data.get(fieldID) != null){
				mol.ID = "";
				for(String data : mol.data.get(fieldID)){
					mol.ID += data;
				}
			}else{
				mol.ID = this.filename;
				if(this.totalRecodeNum!=1)mol.ID += "_" + recodeNo;
			}
			return mol;
		}
	}
}
