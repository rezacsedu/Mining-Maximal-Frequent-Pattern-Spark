package InPrime.deri.MFP;

import java.util.ArrayList;

public class ASP_Tree {
	private int value;
	private int count;
	private int global;
	private ArrayList<ASP_Tree> parentList = new ArrayList<>();
	private ArrayList<ASP_Tree> childList = new ArrayList<>();
	private ASP_Tree mainParent;
	private boolean isTaken=false;
	public ASP_Tree() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ASP_Tree(int value, int count, int global) {
		super();
		this.value = value;
		this.count = count;
		this.global = global;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getGlobal() {
		return global;
	}

	public void setGlobal(int global) {
		this.global = global;
	}
	public void incrementGlobalValue(int count){
		global = global+count;
	}
	public ArrayList<ASP_Tree> getParentList() {
		return parentList;
	}

	public void setParentList(ArrayList<ASP_Tree> parentList) {
		if(parentList ==null)
			this.parentList.clear();
		else
			this.parentList = parentList;
	}
	public void addParent(ASP_Tree parent) {
		parentList.add(parent);
	}

	
	public ArrayList<ASP_Tree> getChildList() {
		return childList;
	}

	public void setChildList(ArrayList<ASP_Tree> childList) {
		if(childList==null)
			this.childList.clear();
		else
			this.childList = childList;
	}
	public void addChild(ASP_Tree child) {
		childList.add(child);
	}
	
	
	public ASP_Tree getMainParent() {
		return mainParent;
	}

	public void setMainParent(ASP_Tree mainParent) {
		this.mainParent = mainParent;
	}
	
	public boolean isTaken() {
		return isTaken;
	}

	public void setTaken(boolean isTaken) {
		this.isTaken = isTaken;
	}

	@Override
	public String toString() {
		return "Node [value=" + value + ", count=" + count + ", global="
				+ global + ", mainParent=" + mainParent + "]";
	}

}
