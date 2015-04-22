package com.insitehub.saleready_android.DataModels.FormStructures;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class CompetencyListItem implements Serializable{

	private static final long serialVersionUID = 1L;
	public static final String ACTIONLIST = "actionlist";
	public static final String NAME = "name";
	public static final String SORTORDER = "sortOrder";
	public static final String COMPETENCY = "competency";
	
	
	private List<ActionItem> actionlist = new LinkedList<ActionItem>();
	private String name;
	private int sortOrder;
	public List<ActionItem> getActionlist() {
		return actionlist;
	}

	public void addAction(ActionItem action){
		actionlist.add(action);
	}
	
	public List<ActionItem> getActionListItems(){
		return actionlist;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public int getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
	
}
