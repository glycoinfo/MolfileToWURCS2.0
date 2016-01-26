package org.glycoinfo.ChemicalStructureUtility.io.MDLMOL;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Class for reading parameters from args from arguments and make path name list of CTFile
 * @author KenichiTanaka
 * @author IssakuYAMADA
 * @author Masaaki Matsubara
 */
public class ParameterReader {
	//----------------------------
	// Member variable
	//----------------------------
	public int m_nMinNOS = 2; //BackboneLength 2014/05/27 IY
	public int m_nMinO   = 2; //BackboneLength 2014/05/27 IY -> 2015/10/08 MM
	public int m_iMinBackboneLength = 3; //ok 2014/05/27 Issaku YAMADA
	//public static int m_maxBackboneLength = 9; //;9,10,...20, 100, 1000, max(10000) 2014/05/27　2014/06/18 (999 IY // static 2014/06/18 IY
	public int m_iMaxBackboneLength = 999; //2014/07/24 Issaku YAMADA
	public String m_strDir = null;
	/* Tag name for sd file */
	public String m_strID = null;
	public boolean m_bOutputSDFile = false;
	public ArrayList<String> m_aCTFilePaths = new ArrayList<String>();
	//2014/07/29 Issaku YAMADA
	// Regard the Backbone as candidate monosaccharide if the Backbone satisfy below equation.
	// ratioBackboneNOS < number of carbon of Backbone / number of NOS connected to Backbone
	public float m_fRatioBackboneNOS = 2.0f;

	//----------------------------
	// Constructor
	//----------------------------
	public ParameterReader(final String[] args, final boolean useFileChooser) {
		// Parse args and set value for member valiable
		LinkedList<String> t_aInputFilePaths = new LinkedList<String>();
		for(int ii=0; ii<args.length; ii++){
			if(      args[ii].equals("-end")             ){ break; }
			else if( args[ii].equals("-minNOS")          ){ ii++; this.m_nMinNOS = Integer.parseInt(args[ii]); }
			else if( args[ii].equals("-minO")            ){ ii++; this.m_nMinO = Integer.parseInt(args[ii]); }
			else if( args[ii].equals("-minBackboneLength")){ ii++; this.m_iMinBackboneLength = Integer.parseInt(args[ii]); }
			else if( args[ii].equals("-maxBackboneLength")){ ii++; this.m_iMaxBackboneLength = Integer.parseInt(args[ii]); }
			else if( args[ii].equals("-ratioBackboneNOS")){ ii++; this.m_fRatioBackboneNOS = Float.parseFloat(args[ii]); }//2014/07/29 Issaku YAMADA
			else if( args[ii].equals("-ID")              ){ ii++; this.m_strID = args[ii]; }
			else if( args[ii].equals("-dir")             ){ ii++; this.m_strDir = args[ii]; }
			else if( args[ii].equals("-sdf")             ){ this.m_bOutputSDFile = true; }
			else{  t_aInputFilePaths.add(args[ii]); }
		}

		// select file using SelectFileDialog, if no file and useFileChooser is true
		if ( t_aInputFilePaths.isEmpty() && useFileChooser ) {
/*
			// Use JFileChooser
			JFileChooser filechooser = new JFileChooser(this.m_dir);
			filechooser.setMultiSelectionEnabled(true);
			filechooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int selected = filechooser.showOpenDialog(null);
			if (selected == JFileChooser.APPROVE_OPTION) {
				File[] files = filechooser.getSelectedFiles();
				for(File file : files){
					this.addFilesRecursively(file);
				}
			}
*/
			// Use FileDialog
			FileDialog dialog = new FileDialog((Frame)null, "", FileDialog.LOAD);
			dialog.setVisible(true);
			String dirname = dialog.getDirectory();
			String filename = dialog.getFile();
			if( filename != null ) {
				t_aInputFilePaths.add(dirname+filename);
			}
		}
		for ( String t_strFile : t_aInputFilePaths )
			this.addFilesRecursively(t_strFile);

		if(this.m_strID != null) return;

		// Get and display unique list of field from input CTFile, if no field name
		// Get list of unique fields
		LinkedList<String> t_aFieldNames = new LinkedList<String>();
		for(String ctfilepath : this.m_aCTFilePaths){
			File file = new File(ctfilepath);
			try {
				BufferedReader t_oBR = new BufferedReader( new FileReader(file) );
				String line;
				while( (line = t_oBR.readLine()) != null){
					if (line.length()>=6 && line.substring(0, 6).trim().equals("M  END")) {
//						ctfile.totalRecodeNum++;
						continue;
					}
					if (line.length() == 0) continue;

					if (line.substring(0, 1).equals(">")) {
						if ( ! line.startsWith("> ") ) continue;
						// line.split("<")[0] = "> 25 ", line.split("<")[1] = "ALTERNATE.NAMES>"
						// line.split("<")[1].split(">")[0] = "ALTERNATE.NAMES"
						String DataHeader = line.split("<")[1].split(">")[0];
						if (!t_aFieldNames.contains(DataHeader)) t_aFieldNames.add(DataHeader);
					}
				}
				t_oBR.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if ( t_aFieldNames.size() == 0 ) return;

		// Display fields
		int t_iNo = 0;
		for ( String t_strID : t_aFieldNames ) {
			System.err.println( (t_iNo)+" : "+t_strID );
			t_iNo++;
		}
		System.err.print("Prease select ID field : ");

		// Input selected field
		String t_strInput = null;
		try {
			BufferedReader t_oBR = new BufferedReader(new InputStreamReader(System.in), 1);
			t_strInput = t_oBR.readLine();
			t_iNo = Integer.parseInt(t_strInput);
			this.m_strID = (t_iNo < t_aFieldNames.size()) ? t_aFieldNames.get(t_iNo) : null;
			t_oBR.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e){
			this.m_strID = null;
		}
	}

	public ArrayList<String> getCTfileList() {
		return this.m_aCTFilePaths;
	}

	//----------------------------
	// Private method
	//----------------------------
	private void addFilesRecursively(String filename){
		File fp = new File(filename);
		this.addFilesRecursively(fp);
	}

	private void addFilesRecursively(File fp) {
		if ( fp.isFile() ) {
			if ( !( fp.getPath().endsWith(".mol") ) && !( fp.getPath().endsWith(".sdf") ) ) return;
			this.m_aCTFilePaths.add(fp.getPath());
			return;
		}
		if ( !fp.isDirectory() ) return;
		File[] filelist = fp.listFiles();
		for(int jj=0; jj<filelist.length; jj++){
			this.addFilesRecursively(filelist[jj]);
		}
	}
}
