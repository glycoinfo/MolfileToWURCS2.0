package chemicalgraph;

import java.util.LinkedList;

/**
 * Class for atom list
 * @author KenichiTanaka
 */
public class AtomList extends LinkedList<Atom>{
	//----------------------------
	// Member variable
	//----------------------------
	private static final long serialVersionUID = 1L;

	//----------------------------
	// Public method (void)
	//----------------------------
	public void setAromatic(final boolean val){
		for(Atom atom : this){
			atom.isAromatic = val;
		}
	}
	
	public void setECnumber(final int val){
		for(Atom atom : this){
			atom.subgraphECnumber = val;
		}
	}
	
	public void setTmp(final int val){
		for(Atom atom : this){
			atom.tmp = val;
		}
	}
	
	public void copyTmpToECNumber(){
		for(Atom atom : this){
			atom.subgraphECnumber = atom.tmp;
		}
	}
	
	//----------------------------
	// Public method (non void)
	//----------------------------
	/**
	 * (for debug) Return the string of the molfile atom number list.
	 * @return the string of the molfile atom number list
	 */
	public String molfileAtomNos(){
		String molfileAtomNos = "";
		for(Atom atom : this){
			if(atom != this.getFirst()) molfileAtomNos += "-";
			molfileAtomNos += "" + atom.molfileAtomNo;
		}
		return molfileAtomNos;
	}

	/**
	 * Return the number of unique tmp in list
	 * (Use when generate EC number)
	 * @return the number of unique tmp in list
	 */
	public int countUniqTmp(){
		int ii;
		int jj;
		int uniqNum = 0;
		int num = this.size();
		for(ii=0; ii<num; ii++){
			for(jj=ii+1; jj<num; jj++){
				if(this.get(ii).tmp == this.get(jj).tmp) break;
			}
			if(jj==num){
				uniqNum++;
			}
		}
		return uniqNum;
	}

	/**
	 * Return true if the member of this list satisfied the huckels rule.
	 * @return true if the member of this list satisfied the huckels rule
	 */
	public boolean isSatisfiedHuckelsRule(){
		AtomList uniqAtom = new AtomList();
		for(Atom atom : this){
			if(uniqAtom.contains(atom))continue;
			uniqAtom.add(atom);
		}
		
		int pi_num = 0;
		for(Atom atom : uniqAtom){
			pi_num += atom.pi;
		}
		
		return ((pi_num - 2) % 4 == 0) ? true : false;
	}
}
