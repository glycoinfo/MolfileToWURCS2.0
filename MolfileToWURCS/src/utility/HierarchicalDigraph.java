package utility;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import chemicalgraph.Atom;
import chemicalgraph.AtomList;
import chemicalgraph.ChemicalGraph;
import chemicalgraph.Connection;

/**
 * HierarchicalDigraphの構築および、CIP順位則によるHierarchicalDigraphの比較<br>
 * 奇数個の原子からなるイオン性共役系は考慮していない。
 * @author KenichiTanaka
 * @see <a href=http://homepage1.nifty.com/nomenclator/text/seqrule.htm>化合物命名法談義</a>
 */
public class HierarchicalDigraph {
	//----------------------------
	// Member variable
	//----------------------------
	public Atom atom;
	public double averageAtomicNumber;
	public LinkedList<HierarchicalDigraph> children = null;
	public boolean isUniqOrder;
	private static final double EPS = 0.000000001;
	public boolean isCompletedFullSearch;

	//----------------------------
	// Constructor
	//----------------------------
	/**
	 * 探索済み原子と探索深度を設定し、atomを起点として、HierarchicalDigraphの構築を行う。
	 * @param atom 探索開始ノード
	 * @param depth 探索深度、負に設定することで制限なしとなる。
	 * @param averageAtomicNumber 平均原子番号：共益系に対応する為
	 * @param ancestors 探索済み原子のリスト
	 */
	public HierarchicalDigraph(final ChemicalGraph targetgraph, final Atom atom, final int depth, final double averageAtomicNumber, final AtomList ancestors, final boolean EZRScheck){
		this.isCompletedFullSearch = true;
		this.depthSearch(targetgraph, atom, depth, averageAtomicNumber, ancestors, EZRScheck);
		if(this.children != null){
			for(HierarchicalDigraph child : this.children){
				if(child.isCompletedFullSearch==false){
					this.isCompletedFullSearch = false;
				}
			}
		}
	}
	
	//----------------------------
	// Public method (void)
	//----------------------------
	/**
	 * Output HierarchicalDigraph to PrintStream.
	 */
	public void print(PrintStream ps){
		ps.println();
		this.print(new LinkedList<Boolean>(), ps);
	}
	
	//----------------------------
	// Public method (non void)
	//----------------------------
	/**
	 * Compare Hierarchical digraph by CIP priority rule
	 * @param target 比較対象
	 * @return -1:自分が優位, 1:相手が優位, 0:等しい
	 */
	public int compareTo(final HierarchicalDigraph target, final boolean EZRScheck){
		LinkedList<HierarchicalDigraph> widthsearch1 = new LinkedList<HierarchicalDigraph>();
		LinkedList<HierarchicalDigraph> widthsearch2 = new LinkedList<HierarchicalDigraph>();
		
		// 原子番号の比較
		// (1)直接結合する原子の原子番号が大きい方を優位とする。
		// (2)前項で決まらないときは、最初の原子に結合している原子（すなわち中心から2番目の原子）について (i) の基準で比べる。2番目の原子が最初の原子に複数結合しているときは、原子番号の大きい順に候補を1つずつ出して違いのあった時点で決める。
		// (3)前項で決まらないときは、中心から2番目の原子（ただし、(ii)で除外された原子は除く）に結合している原子で比べる。以降、順に中心から離れた原子を比べる。
		widthsearch1.addLast(this);
		widthsearch2.addLast(target);
		while(widthsearch1.size()!=0 && widthsearch2.size()!=0){
			HierarchicalDigraph check1 = widthsearch1.removeFirst();
			HierarchicalDigraph check2 = widthsearch2.removeFirst();
			
			// 原子番号が大きい方を優位
			if(Math.abs(check1.averageAtomicNumber - check2.averageAtomicNumber) > EPS){
				return (check1.averageAtomicNumber > check2.averageAtomicNumber) ? -1 : 1;
			}
			
			// 子供の比較
			int minChildNum = Math.min(check1.children.size(), check2.children.size());
			for(int ii=0; ii<minChildNum; ii++){
				double averageAtomicNumber1 = check1.children.get(ii).averageAtomicNumber;
				double averageAtomicNumber2 = check2.children.get(ii).averageAtomicNumber;
				if(Math.abs(averageAtomicNumber1 - averageAtomicNumber2) > EPS){
					return (averageAtomicNumber1 > averageAtomicNumber2) ? -1 : 1;
				}
			}
			
			// 子供の数が異なる場合、少ない方に空原子を追加するが、空原子は空でない原子より劣勢である為、この時点で順位がつく
			if(check1.children.size() != check2.children.size()) return check2.children.size() - check1.children.size();
			
			// 子供をリストに追加
			for(HierarchicalDigraph child1 : check1.children){
				widthsearch1.addLast(child1);
			}
			for(HierarchicalDigraph child2 : check2.children){
				widthsearch2.addLast(child2);
			}
		}
		
		// (4)2つの基が位相的に等しい（構成する原子の元素、個数、結合順序が等しい）が、質量数が異なる原子を含む場合、質量数の大きい原子を含む基を優位とする。
		// WURCS生成時には、正規化の段階で同位体情報は削除されるので、この比較は行わない。

		// (5)2つの基が物質的かつ位相的に等しい（構成する原子の元素、個数、結合順序、質量数が等しい）が、立体化学が異なる場合。 
		// まず二重結合の幾何異性に関して、ZをEより優位とする。 
		// 次いでジアステレオ異性に関して、like （R,R またはS,S）を unlike （R,S またはS,R）より優位とする。 
		// 次いで、鏡像異性に関して、RをS より優位とする。 
		// 最後に、擬似不斉原子に関して、rをs より優位とする。
		if(EZRScheck) return this.compareEZRSTo(target);

		return 0;
	}
	
	private int compareEZRSTo(final HierarchicalDigraph target){
		LinkedList<HierarchicalDigraph> widthsearch1 = new LinkedList<HierarchicalDigraph>();
		LinkedList<HierarchicalDigraph> widthsearch2 = new LinkedList<HierarchicalDigraph>();
		widthsearch1.addLast(this);
		widthsearch2.addLast(target);
		while(widthsearch1.size()!=0 && widthsearch2.size()!=0){
			HierarchicalDigraph check1 = widthsearch1.removeFirst();
			HierarchicalDigraph check2 = widthsearch2.removeFirst();
			
			if(check1.atom==null&&check2.atom==null) continue;
			
			// 子供の比較
			int minChildNum = Math.min(check1.children.size(), check2.children.size());
			for(int ii=0; ii<minChildNum; ii++){
				HierarchicalDigraph child1 = check1.children.get(ii);
				HierarchicalDigraph child2 = check2.children.get(ii);
				
				if(child1.atom!=null&&child2.atom!=null){
					Connection connection1 = check1.atom.connections.getConnect(child1.atom);
					Connection connection2 = check2.atom.connections.getConnect(child2.atom);
					
					// まず二重結合の幾何異性に関して、ZをEより優位とする。
					if( connection1.bond.stereoTmp!=null && connection2.bond.stereoTmp!=null){
						if( connection1.bond.stereoTmp.equals("Z") && connection2.bond.stereoTmp.equals("E")) return -1;
						if( connection1.bond.stereoTmp.equals("E") && connection2.bond.stereoTmp.equals("Z")) return 1;
					}
					
					if( connection1.atom.stereoTmp!=null && connection2.atom.stereoTmp!=null){
						// 次いでジアステレオ異性に関して、like （R,R またはS,S）を unlike （R,S またはS,R）より優位とする。 
						if( check1.atom.stereoTmp!=null && check2.atom.stereoTmp!=null){
							if( connection1.atom.stereoTmp.equals(check1.atom.stereoTmp) && !connection2.atom.stereoTmp.equals(check2.atom.stereoTmp)) return -1;
							if(!connection1.atom.stereoTmp.equals(check1.atom.stereoTmp) &&  connection2.atom.stereoTmp.equals(check2.atom.stereoTmp)) return 1;
						}
						
						// 次いで、鏡像異性に関して、RをS より優位とする。 
						if( connection1.atom.stereoTmp.equals("R") && connection2.atom.stereoTmp.equals("S")) return -1;
						if( connection1.atom.stereoTmp.equals("S") && connection2.atom.stereoTmp.equals("R")) return 1;
						
						// 最後に、擬似不斉原子に関して、rをs より優位とする。
						if( connection1.atom.stereoTmp.equals("r") && connection2.atom.stereoTmp.equals("s")) return -1;
						if( connection1.atom.stereoTmp.equals("s") && connection2.atom.stereoTmp.equals("r")) return 1;
					}
				}
			}
			
			// 子供をリストに追加
			for(HierarchicalDigraph child1 : check1.children){
				widthsearch1.addLast(child1);
			}
			for(HierarchicalDigraph child2 : check2.children){
				widthsearch2.addLast(child2);
			}
		}
		
		return 0;
	}
	
	//----------------------------
	// Private method
	//----------------------------
	/**
	 * 深さ優先探索を用いて、HierarchicalDigraphの構築を行う。
	 * @param atom 探索中の原子
	 * @param averageAtomicNumber　平均原子番号：共益系に対応する為
	 * @param ancestors 探索済み原子のリスト、該当原子に到達したら引き返す。
	 * @param depth 探索する深さに制限を付ける場合に利用
	 */
	private void depthSearch(final ChemicalGraph targetgraph, final Atom atom, final int depth, final double averageAtomicNumber, final AtomList ancestors, final boolean EZRScheck){
		if( atom!=null && !atom.symbol.equals("H") && !targetgraph.contains(atom) ) return;
		this.atom = atom;
		this.averageAtomicNumber = averageAtomicNumber;
		this.children = new LinkedList<HierarchicalDigraph>();
		if(this.atom == null) return;
		if(ancestors.contains(this.atom)) return;
		if(depth == 0){
			this.isCompletedFullSearch = false;
			return;
		}

		// Add Children
		int num = 0;
		int sumAtomicNumber = 0;
		ancestors.addLast(this.atom);
		for(Connection connection : this.atom.connections){
			if( !connection.atom.symbol.equals("H") && !targetgraph.contains(connection.atom) ) continue;
			this.children.add(new HierarchicalDigraph(targetgraph, connection.atom, depth-1, (double)connection.atom.atomicNumber(), ancestors, EZRScheck));
			// 共益系や多重結合の場合、同一の原子が重複しているとみなす。
			if(this.atom.isAromatic && connection.atom.isAromatic){
				num++;
				sumAtomicNumber+=(double)connection.atom.atomicNumber();
			}else if(connection.bond.type==2 || connection.bond.type==3){
				for(int ii=connection.bond.type; ii>1; ii--){
					this.children.add(new HierarchicalDigraph(targetgraph, null, depth-1, (double)connection.atom.atomicNumber(), ancestors, EZRScheck));
				}
			}
		}
		// 共益系 connect 
		if(num!=0){
			this.children.add(new HierarchicalDigraph(targetgraph, null, depth-1, (double)sumAtomicNumber/(double)num, ancestors, EZRScheck));
		}
		ancestors.removeLast();
		
		// Sort Children
		Collections.sort(this.children, new Comparator<HierarchicalDigraph>() {
			public int compare(HierarchicalDigraph tree1, HierarchicalDigraph tree2) {
				return tree1.compareTo(tree2, EZRScheck);
			}
		});
		
		// CIPorderがユニークにならない要素にfalseを立てる
		for(HierarchicalDigraph child : this.children){
			child.isUniqOrder = true;
		}
		int childrenNum = this.children.size();
		for(int ii=0; ii<childrenNum-1; ii++){
			HierarchicalDigraph tree1 = this.children.get(ii);
			HierarchicalDigraph tree2 = this.children.get(ii+1);
			if(tree1.compareTo(tree2, EZRScheck)!=0) continue;
			tree1.isUniqOrder = false;
			tree2.isUniqOrder = false;
		}
	}
	
	/**
	 * Output HierarchicalDigraph to PrintStream.
	 * @param historys
	 * @param ps 
	 */
	private void print(LinkedList<Boolean> historys, PrintStream ps){
		
		int ii=0;
		for(Boolean history : historys){
			if(ii>=historys.size()-1) break;
			ps.print(history ? " |" : "  ");
			ii++;
		}
		if(historys.size()>0){
			ps.print(" +");
		}
		ps.print("-" + ((this.atom==null)?"null":(this.atom.symbol + "(" + this.atom.molfileAtomNo + ")")) + "(" + this.averageAtomicNumber + ")" + " : ");
		
		if(this.children==null) return;
		
		for(HierarchicalDigraph child : this.children){
			ps.print((this.children.indexOf(child)+1) + "(" + (child.isUniqOrder?"o":"x") + ")" + "." + ((child.atom==null)?"null":(child.atom.symbol + "(" + child.atom.molfileAtomNo + ")")) + "(" + child.averageAtomicNumber + "), ");
		}
		ps.println();
		
		for(HierarchicalDigraph child : children){
			historys.addLast(children.getLast()!=child);
			child.print(historys, ps);
			historys.removeLast();
		}
	}
}
