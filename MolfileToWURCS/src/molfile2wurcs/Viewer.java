package molfile2wurcs;

import glycan.Glycan;

import javax.swing.JFrame;

import chemicalgraph.Atom;
import chemicalgraph.Bond;
import chemicalgraph.ChemicalGraph;
import chemicalgraph.Connection;
import chemicalgraph.subgraph.backbone.Backbone;
import chemicalgraph.subgraph.backbone.BackboneList;
import chemicalgraph.subgraph.modification.Modification;
import chemicalgraph.subgraph.molecule.Molecule;
import chemicalgraph.subgraph.molecule.MoleculeList;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * WURCS変換結果の可視化
 * @author KenichiTanaka
 */
public class Viewer extends JFrame implements KeyListener{
	//----------------------------
	// Member variable
	//----------------------------
	private static final long serialVersionUID = 1L;
	public MoleculeList mols;
	public int startMolNo = 0;
	public int selectMolNo = 0;
	public int colNum = 5;
	public int rowNum = 4;
	private boolean showInformation = true;
	private boolean showFullCOLIN = true;
	private boolean showMolfileAtomNumber = true;
	private boolean showInitialECnumber;
	private boolean showCoordinate;
	private int showStereo = 1;
	private boolean showHybridOrbital;
	private boolean showHiddenHydrogen;

	//----------------------------
	// Constructor
	//----------------------------
	public Viewer(final MoleculeList in_mols) {
		this.mols = in_mols;
		this.setTitle("MolfileToWURCS");
		this.setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addKeyListener(this);
	}

	//----------------------------
	// Public method
	//----------------------------
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(final KeyEvent e) {
		int diff = this.selectMolNo - this.startMolNo;
		boolean flg_repaint=true;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:      this.selectMolNo--; break;
		case KeyEvent.VK_RIGHT:     this.selectMolNo++; break;
		case KeyEvent.VK_UP:        this.selectMolNo -= this.colNum; break;
		case KeyEvent.VK_DOWN:      this.selectMolNo += this.colNum; break;
		case KeyEvent.VK_PAGE_UP:   this.selectMolNo -= this.colNum * this.rowNum; break;
		case KeyEvent.VK_PAGE_DOWN: this.selectMolNo += this.colNum * this.rowNum; break;
		case KeyEvent.VK_F1:        this.showInformation = (this.showInformation) ? false : true; break;
		case KeyEvent.VK_F2:        this.showFullCOLIN = (this.showFullCOLIN) ? false : true; break;
		case KeyEvent.VK_F3:        this.showMolfileAtomNumber = (this.showMolfileAtomNumber) ? false : true; break;
		case KeyEvent.VK_F4:        this.showInitialECnumber = (this.showInitialECnumber) ? false : true; break;
		case KeyEvent.VK_F5:        this.showCoordinate = (this.showCoordinate) ? false : true; break;
		case KeyEvent.VK_F6:        this.showStereo++; if(this.showStereo>2){this.showStereo=0;} break;
		case KeyEvent.VK_F7:        this.showHybridOrbital = (this.showHybridOrbital) ? false : true; break;
		case KeyEvent.VK_F8:        this.showHiddenHydrogen = (this.showHiddenHydrogen) ? false : true; break;
		default: 					flg_repaint = false; break;
		}
		if(flg_repaint==false) return;
		
		if(this.selectMolNo < 0) this.selectMolNo = 0;
		if(this.selectMolNo > mols.size() - 1) this.selectMolNo = mols.size() - 1;
		
		if(this.selectMolNo >= this.startMolNo + this.colNum * this.rowNum || this.selectMolNo < this.startMolNo){
			this.startMolNo = this.selectMolNo - diff;
			if(this.startMolNo < 0) this.startMolNo = 0;
		}
		
		repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(final KeyEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(final KeyEvent e) {
	}

	/**
	 * @param g2
	 * @param atom
	 * @param zoom
	 * @param color
	 * @param bgcolor
	 */
	public void drawAtom(final Graphics2D g2, final Atom atom, final double zoom, final Color color, final Color bgcolor){
		double point[] = new double[2];
		point[0] =  atom.coordinate[0] * zoom * 0.8;
		point[1] = -atom.coordinate[1] * zoom * 0.8;

		g2.setColor(bgcolor);
		g2.fill(new Ellipse2D.Double(point[0]-5, point[1]-5, 13, 13));

		// Symbol
		g2.setColor(color);
		g2.drawString(atom.symbol, (int)point[0]-3, (int)point[1]+6);
		
		// Charge
		if(atom.charge < 0){
			int num = Math.abs(atom.charge);
			for(int ii=0; ii<num; ii++){
				g2.draw(new Line2D.Double(5 + point[0] + ii*8,     - 5 + point[1],     5 + point[0] + ii*8 + 6, - 5 + point[1]));
			}
		}else if(atom.charge > 0){
			int num = atom.charge;
			for(int ii=0; ii<num; ii++){
				g2.draw(new Line2D.Double(5 + point[0] + ii*8,     - 5 + point[1],     5 + point[0] + ii*8 + 6, - 5 + point[1]));
				g2.draw(new Line2D.Double(5 + point[0] + ii*8 + 3, - 5 + point[1] + 3, 5 + point[0] + ii*8 + 3, - 5 + point[1] - 3));
			}
		}
		
		// Information
		String text = "";
		if(this.showMolfileAtomNumber) text += " " + atom.molfileAtomNo;
		if(this.showHybridOrbital) text += " " + atom.hybridOrbital();
		if(this.showStereo==1){
			if(atom.stereoMolecule!=null) text += " (" + atom.stereoMolecule + ")";
		}else if(this.showStereo==2){
			if(atom.stereoModification!=null) text += " (" + atom.stereoModification + ")";
		}
		if(this.showInitialECnumber) text += " " + atom.initialECnumber;
		g2.setColor(color);
		g2.drawString(text, (int)point[0]+5, (int)point[1]+10);
		
		if(this.showCoordinate){
			text = " (" +
					String.format("%4.2f", atom.coordinate[0]) + ", " +
					String.format("%4.2f", atom.coordinate[1]) + ", " +
					String.format("%4.2f", atom.coordinate[2]) + ")";
			g2.setColor(color);
			g2.drawString(text, (int)point[0]+5, (int)point[1]+23);
		}
	}

	/**
	 * @param g2
	 * @param bond
	 * @param zoom
	 * @param color
	 */
	public void drawBond(final Graphics2D g2, final Bond bond, final double zoom, final Color color){
    	double point[][] = new double[2][2];
		point[0][0] =  bond.atoms[0].coordinate[0] * zoom * 0.8;
		point[0][1] = -bond.atoms[0].coordinate[1] * zoom * 0.8;
		point[1][0] =  bond.atoms[1].coordinate[0] * zoom * 0.8;
		point[1][1] = -bond.atoms[1].coordinate[1] * zoom * 0.8;
		double len = Math.sqrt((point[1][0] - point[0][0]) * (point[1][0] - point[0][0]) + (point[1][1] - point[0][1]) * (point[1][1] - point[0][1]));
		
		double[] unit = new double[2];
		unit[0] = (point[1][0] - point[0][0]) / len;
		unit[1] = (point[1][1] - point[0][1]) / len;
		
		double cyokkaku[] = new double[2];
		cyokkaku[0] =  unit[1] * 3;
		cyokkaku[1] = -unit[0] * 3;
		
		g2.setColor(color);
		if(bond.type == 1){
			if(bond.stereo == 0){
				g2.draw(new Line2D.Double(point[0][0], point[0][1], point[1][0], point[1][1]));
			}else if(bond.stereo == 1){
				// up
				int[] xpoints = { (int)point[0][0], (int)(point[1][0]+cyokkaku[0]), (int)(point[1][0]-cyokkaku[0])};
				int[] ypoints = { (int)point[0][1], (int)(point[1][1]+cyokkaku[1]), (int)(point[1][1]-cyokkaku[1])};
				Polygon polygon = new Polygon(xpoints, ypoints, xpoints.length);
				g2.fill(polygon);
			}else if(bond.stereo == 6){
				// down
				int[] xpoints = { (int)point[0][0], (int)(point[1][0]+cyokkaku[0]), (int)(point[1][0]-cyokkaku[0])};
				int[] ypoints = { (int)point[0][1], (int)(point[1][1]+cyokkaku[1]), (int)(point[1][1]-cyokkaku[1])};
				Polygon polygon = new Polygon(xpoints, ypoints, xpoints.length);
				g2.draw(polygon);
			}
		}else if(bond.type == 2){
			if(bond.stereo == 3){
				g2.draw(new Line2D.Double(point[0][0]+cyokkaku[0], point[0][1]+cyokkaku[1], point[1][0]-cyokkaku[0], point[1][1]-cyokkaku[1]));
				g2.draw(new Line2D.Double(point[0][0]-cyokkaku[0], point[0][1]-cyokkaku[1], point[1][0]+cyokkaku[0], point[1][1]+cyokkaku[1]));
			}else{
				g2.draw(new Line2D.Double(point[0][0]+cyokkaku[0], point[0][1]+cyokkaku[1], point[1][0]+cyokkaku[0], point[1][1]+cyokkaku[1]));
				g2.draw(new Line2D.Double(point[0][0]-cyokkaku[0], point[0][1]-cyokkaku[1], point[1][0]-cyokkaku[0], point[1][1]-cyokkaku[1]));
			}
		}else if(bond.type == 3){
			g2.draw(new Line2D.Double(point[0][0]+cyokkaku[0], point[0][1]+cyokkaku[1], point[1][0]+cyokkaku[0], point[1][1]+cyokkaku[1]));
			g2.draw(new Line2D.Double(point[0][0], point[0][1], point[1][0], point[1][1]));
			g2.draw(new Line2D.Double(point[0][0]-cyokkaku[0], point[0][1]-cyokkaku[1], point[1][0]-cyokkaku[0], point[1][1]-cyokkaku[1]));
		}else{
			g2.draw(new Line2D.Double(point[0][0], point[0][1], point[1][0], point[1][1]));
		}
		
		String text = "";
		if(this.showStereo == 1){
			if(bond.stereoMolecule!=null) text += " (" + bond.stereoMolecule + ")";
		}else if(this.showStereo == 2){
			if(bond.stereoModification!=null) text += " (" + bond.stereoModification + ")";
		}
		g2.setColor(color);
		g2.drawString(text, (int)((point[0][0]+point[1][0])/2 + cyokkaku[0]*5), (int)((point[0][1]+point[1][1])/2 + cyokkaku[1]*5));
	}
	
	/**
	 * @param g2
	 * @param mol
	 * @param range
	 * @param molNo
	 */
	public void drawmol(final Graphics2D g2, final Molecule mol, final int range[][], final int molNo){
		// Back ground
		Color bgcolor = Color.WHITE;
		for(BackboneList backbones : mol.candidateBackboneGroups){
			if(backbones.size() > 1 && backbones.get(1).isBackbone == true){
				bgcolor = Color.GRAY;
			}
		}
		g2.setColor(bgcolor);
		g2.fill(new Rectangle2D.Double(0, 0, range[1][0], range[1][1]));
		
		// Information
		int lineNo = 0;
		g2.setColor(Color.BLACK);
		lineNo++; g2.drawString((molNo+1) + " / " + this.mols.size(), 5, 15*lineNo);
		lineNo++; g2.drawString("# of detected monosaccharide : " + mol.candidateBackboneGroups.size(), 5, 15*lineNo);
		lineNo++; g2.drawString(mol.ID + " : ID", 5, 15*lineNo);
		for(Glycan glycan : mol.glycans){
			lineNo++; g2.drawString(glycan.WURCS, 5, 15*lineNo);
		}
		
		if(this.showInformation){
			for(BackboneList backbones : mol.candidateBackboneGroups){
				for(Backbone backbone : backbones){
					if(!backbone.isBackbone) continue;
					String info = "";
					info += "Backbone(" + (mol.candidateBackboneGroups.indexOf(backbones)+1) + ":" + (backbones.indexOf(backbone)+1) + "/" + backbones.size() + ") ";
					info += " : " + backbone.skeletonCode;
					info += " : " + backbone.molfileAtomNos();
					info += " : Type(" + backbone.type + ")";
					info += " : " + backbone.coOCOSequence;
					info += " : " + backbone.oxidationSequence;
					lineNo++; g2.drawString(info, 5, 15*lineNo);
				}
			}

			for(Modification mod : mol.modifications){
				String info = "";
				info += "Modification(" + (mol.modifications.indexOf(mod)+1) + ")";
				info += " : " + mod.toCOLIN(mol.backbones, this.showFullCOLIN);
				info += mod.paths.toALIN();
				info += " : " + mod.paths.atoms().molfileAtomNos();
				lineNo++; g2.drawString(info, 5, 15*lineNo);
			}
		}
		
		if(mol.atoms.size() == 0) return;

		// Translate
		// Zoomや反転処理をここでやると、drawAtomした際に、文字が巨大になる＆反転するので、ここではやらない。
		// 文字列描画の際に、座標のみを利用して、文字はその場に表示する方法があるはずなので、見つかったらそれを実装。
		range[0][1] += 15 * (lineNo + 1);
		double mol_size[][] = mol.getMoleculeSize();
		double zoom = 1;
		if(((range[1][0]-range[0][0]) / (mol_size[1][0]-mol_size[0][0])) > (range[1][1]-range[0][1]) / (mol_size[1][1]-mol_size[0][1])){
			zoom = (range[1][1]-range[0][1]) / (mol_size[1][1]-mol_size[0][1]);
		}else{
			zoom = (range[1][0]-range[0][0]) / (mol_size[1][0]-mol_size[0][0]);
		}
		g2.translate(
				(range[1][0] - range[0][0])/2 - (mol_size[1][0] + mol_size[0][0]) * zoom * 0.8 / 2,
				(range[1][1] - range[0][1])/2 + (mol_size[1][1] + mol_size[0][1]) * zoom * 0.8 / 2 + 15 * (lineNo + 1)
				);

		// Chain background
		Bond dummy = new Bond();
		g2.setStroke(new BasicStroke(10.0f));
		for(BackboneList backbones : mol.candidateBackboneGroups){
			Backbone backbone = backbones.get(0);
			int backboneLength = backbone.size();
			for(int ii=0; ii<backboneLength-1; ii++){
				dummy.atoms[0] = backbone.get(ii);
				dummy.atoms[1] = backbone.get(ii+1);
				this.drawBond(g2, dummy, zoom, Color.GREEN);
			}
		}
		
		// Bond
		g2.setStroke(new BasicStroke(1.0f));
		for(Bond bond : mol.bonds){
			this.drawBond(g2, bond, zoom, Color.BLACK);
		}

		// hiddenHydrogen
		if(this.showHiddenHydrogen){
			for(Atom atom : mol.atoms){
				for(Connection connection : atom.connections){
					if(!mol.atoms.contains(connection.atom)){
						this.drawBond(g2, connection.bond, zoom, Color.BLACK);
					}
				}
			}
			
			for(Atom atom : mol.atoms){
				for(Connection connection : atom.connections){
					if(!mol.atoms.contains(connection.atom)){
						Color atombgcolor = bgcolor;
						this.drawAtom(g2, connection.atom, zoom, Color.BLACK, atombgcolor);
					}
				}
			}
		}

		// Atom
		for(Atom atom : mol.atoms){
			Color atombgcolor = bgcolor;
			for(BackboneList backbones : mol.candidateBackboneGroups){
				if(backbones.size() == 0) continue;
				if(!backbones.get(0).contains(atom)) continue;
				atombgcolor = (backbones.get(0).get(0) == atom) ? Color.RED : Color.GREEN;
			}
			this.drawAtom(g2, atom, zoom, Color.BLACK, atombgcolor);
		}
	}

	/**
	 * @param g2
	 */
	public void draw(final Graphics2D g2){
		String Title = "MolfileToWURCS";
		Title += " F1:Information" + (this.showInformation ? "(on)" : "(off)");
		Title += " F2:FullCOLIN" + (this.showFullCOLIN ? "(on)" : "(off)");;
		Title += " F3:MolfileAtomNumber" + (this.showMolfileAtomNumber ? "(on)" : "(off)");;
		Title += " F4:InitialECnumber" + (this.showInitialECnumber ? "(on)" : "(off)");;
		Title += " F5:Coordinate" + (this.showCoordinate ? "(on)" : "(off)");;
		Title += " F6:Stereo";
		if(this.showStereo == 0){
			Title += "(off)";
		}else if(this.showStereo == 1){
			Title += "(Molecule)";
		}else if(this.showStereo == 2){
			Title += "(Modification)";
		}
		Title += " F7:HybridOrbital" + (this.showHybridOrbital ? "(on)" : "(off)");;
		Title += " F8:HiddenHydrogen" + (this.showHiddenHydrogen ? "(on)" : "(off)");;
		this.setTitle(Title);
		
		int range[][] = new int[2][3];
		Insets insets = getInsets();
		int cell_size[] = new int[2];
		cell_size[0] = (this.getWidth()  - (insets.right + insets.left)) / this.colNum;
		cell_size[1] = (this.getHeight() - (insets.top + insets.bottom)) / this.rowNum;
		
		// Standard out
		Molecule selectMol = this.mols.get(this.selectMolNo);
		System.err.println(selectMol.ID);
		for(BackboneList backbones : selectMol.candidateBackboneGroups){
			for(Backbone backbone : backbones){
				String info = (backbone.isBackbone) ? "o" : "x";
				info += " Type(" + backbone.type + ")";
				info += " : " + backbone.coOCOSequence;
				info += " : " + backbone.oxidationSequence;
				info += " : " + backbone.skeletonCode;
				info += " : " + backbone.molfileAtomNos();
				System.err.println(info);
			}
		}
		for(Modification mod : selectMol.modifications){
			String info = "";
			info += "Modification(" + (selectMol.modifications.indexOf(mod)+1) + ")";
			info += " : " + mod.toCOLIN(selectMol.backbones, this.showFullCOLIN);
			info += mod.paths.toALIN();
			info += " : " + mod.paths.atoms().molfileAtomNos();
			System.err.println();
			System.err.println(info);
			mod.paths.debug((ChemicalGraph)mod);
		}
		System.err.println();

		for(int row=0; row<this.rowNum; row++){
			for(int col=0; col<this.colNum; col++){
				int molNo = this.startMolNo + row*this.colNum + col;
				if(molNo > this.mols.size() - 1) return;
				range[0][0] = insets.left + cell_size[0] * col;
				range[0][1] = insets.top + cell_size[1] * row;
				range[0][2] = 0;
				range[1][0] = insets.left + cell_size[0] * (col+1);
				range[1][1] = insets.top + cell_size[1] * (row+1);
				range[1][2] = 0;
				
				AffineTransform saveAT = g2.getTransform();
				g2.translate(range[0][0], range[0][1]);
				this.drawmol(g2, this.mols.get(molNo), range, molNo);
				g2.setTransform(saveAT);
			}
		}

		// Frame
		for(int row=0; row<this.rowNum; row++){
			for(int col=0; col<this.colNum; col++){
				int molNo = this.startMolNo + row*this.colNum + col;
				if(molNo == this.selectMolNo){
					g2.setStroke(new BasicStroke(2.0f));
				}else{
					g2.setStroke(new BasicStroke(1.0f));
				}
				g2.setColor(Color.BLACK);
				g2.draw(new Rectangle2D.Double(insets.left + (cell_size[0] * col), insets.top + (cell_size[1] * row), cell_size[0], cell_size[1]));
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.Window#paint(java.awt.Graphics)
	 */
	public void paint(final Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		this.draw(g2);
	}
}
