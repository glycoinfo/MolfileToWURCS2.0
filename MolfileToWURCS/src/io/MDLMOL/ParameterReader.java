package io.MDLMOL;

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
 * Class for parameter reader
 * read parameters from arguments and make path name list of CTFile
 * @author KenichiTanaka
 * @author IssakuYAMADA
 * @author Masaaki Matsubara
 */
public class ParameterReader {
	//----------------------------
	// Member variable
	//----------------------------
	public int m_minNOS = 2; //BackboneLength 2014/05/27 IY
	public int m_minO   = 2; //BackboneLength 2014/05/27 IY
	public int m_minBackboneLength = 3; //ok 2014/05/27 Issaku YAMADA
	//public static int m_maxBackboneLength = 9; //;9,10,...20, 100, 1000, max(10000) 2014/05/27　2014/06/18 (999 IY // static 2014/06/18 IY
	public int m_maxBackboneLength = 999; //2014/07/24 Issaku YAMADA
	public String m_dir = null;
	/* Tag name for sd file */
	public String m_ID = null;
	public boolean m_sdfileOutput = false;
	public ArrayList<String> ctfilepaths = new ArrayList<String>();
	private BufferedReader br = null;
	//2014/07/29 Issaku YAMADA
	// Regard the Backbone as candidate monosaccharide if the Backbone satisfy below equation.
	// ratioBackboneNOS < number of carbon of Backbone / number of NOS connected to Backbone
	public float m_ratioBackboneNOS = 2.0f;

	//----------------------------
	// Constructor
	//----------------------------
	public ParameterReader(final String[] args, final boolean useFileChooser){
		// parse args and set value for member valiable
		for(int ii=0; ii<args.length; ii++){
			if(      args[ii].equals("-end")             ){ break; }
			else if( args[ii].equals("-minNOS")          ){ ii++; this.m_minNOS = Integer.parseInt(args[ii]); }
			else if( args[ii].equals("-minO")            ){ ii++; this.m_minO = Integer.parseInt(args[ii]); }
			else if( args[ii].equals("-minBackboneLength")){ ii++; this.m_minBackboneLength = Integer.parseInt(args[ii]); }
			else if( args[ii].equals("-maxBackboneLength")){ ii++; this.m_maxBackboneLength = Integer.parseInt(args[ii]); }
			else if( args[ii].equals("-ratioBackboneNOS")){ ii++; this.m_ratioBackboneNOS = Float.parseFloat(args[ii]); }//2014/07/29 Issaku YAMADA
			else if( args[ii].equals("-ID")              ){ ii++; this.m_ID = args[ii]; }
			else if( args[ii].equals("-dir")             ){ ii++; this.m_dir = args[ii]; }
			else if( args[ii].equals("-sdf")             ){ this.m_sdfileOutput = true; }
			else{                                         this.addFilesRecursively(args[ii]); }
		}

		// select file using SelectFileDialog, if no file and useFileChooser is true
		if ((this.ctfilepaths.size() == 0 ) && useFileChooser) {
/*			JFileChooser filechooser = new JFileChooser(this.m_dir);
			filechooser.setMultiSelectionEnabled(true);
			filechooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int selected = filechooser.showOpenDialog(null);
			if (selected == JFileChooser.APPROVE_OPTION) {
				File[] files = filechooser.getSelectedFiles();
				for(File file : files){
					this.addFilesRecursively(file);
				}
			}
*/			FileDialog dialog = new FileDialog((Frame)null, "", FileDialog.LOAD);
			dialog.setVisible(true);
			String dirname = dialog.getDirectory();
			String filename = dialog.getFile();
			if( filename != null ) {
				this.addFilesRecursively(dirname+filename);
			}
		}

		// Get and display unique list of field from input CTFile, if no field name
		if(this.m_ID == null){
			// get list of unique fields
			LinkedList<String> fields = new LinkedList<String>();
			for(String ctfilepath : this.ctfilepaths){
				File file = new File(ctfilepath);
				try {
					FileReader fr = new FileReader(file);
					br = new BufferedReader(fr);
					String line;
					while( (line = br.readLine()) != null){
						if(line.length()>=6 && line.substring(0, 6).trim().equals("M  END")){
//							ctfile.totalRecodeNum++;
							continue;
						}
						if(line.length() == 0) continue;

						if(line.substring(0, 1).equals(">")){
							// line.split("<")[0] = "> 25 ", line.split("<")[1] = "ALTERNATE.NAMES>"
							// line.split("<")[1].split(">")[0] = "ALTERNATE.NAMES"
							String DataHeader = line.split("<")[1].split(">")[0];
							if(!fields.contains(DataHeader)) fields.add(DataHeader);
						}
					}
					br.close();
					fr.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if(fields.size() != 0){
				// display fields
				int No = 0;
				for(String ID : fields){
					System.err.println((No) + " : " + ID);
					No++;
				}
				System.err.print("Prease select ID field : ");

				// input selected field
				String inputdata = null;
				try {
					br = new BufferedReader(new InputStreamReader(System.in), 1);
					inputdata = br.readLine();
					No = Integer.parseInt(inputdata);
					this.m_ID = (No < fields.size()) ? fields.get(No) : null;
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NumberFormatException e){
					this.m_ID = null;
				}
			}
		}
	}

	public ArrayList<String> getCTfileList() {
		return this.ctfilepaths;
	}

	//----------------------------
	// Private method
	//----------------------------
	private void addFilesRecursively(String filename){
		File fp = new File(filename);
		this.addFilesRecursively(fp);
	}

	private void addFilesRecursively(File fp){
		if(fp.isFile()){
			if(!(fp.getPath().endsWith(".mol")) && !(fp.getPath().endsWith(".sdf"))) return;
			this.ctfilepaths.add(fp.getPath());
		}else if(fp.isDirectory()){
			File[] filelist = fp.listFiles();
			for(int jj=0; jj<filelist.length; jj++){
				this.addFilesRecursively(filelist[jj]);
			}
		}
	}

}
