package com.insitehub.saleready_android.DataModels.FormStructures;

import android.widget.RadioButton;

public class Option {
	/**
	 * 
	 */
	public static final String NAME = "name";
	public static final String SORTORDER = "sortOrder";
	private String name;
	private int sortOrder;
	private boolean selected = false;
	private RadioButton view;
	
	
	
	public RadioButton getView() {
		return view;
	}
	public void setView(RadioButton view) {
		this.view = view;
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
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	
}
