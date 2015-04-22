package com.insitehub.saleready_android.DataModels.FormStructures;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import android.widget.EditText;

import com.insitehub.saleready_android.DataModels.ParseExtensionClasses.Asset;

//Use to add widgets
public class ActionItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String NAME = "name";
	public static final String SORTORDER = "sortOrder";
	public static final String TYPE = "type";
	public static final String OPTIONLIST = "optionlist";
	public static final String ASSETLIST = "assetlist";
	public static final String ACTION = "action";

	private List<Asset> assestlist = new LinkedList<Asset>();
	private List<Option> optionlist = new LinkedList<Option>();
	private String name;
	private int sortOrder;
	private String type;
	private String value = "";
	private int checkedRadioButtonId = -1;
	
	private EditText view;

	public List<Asset> getAssestlist() {
		return assestlist;
	}

	public void addAsset(Asset asset) {
		assestlist.add(asset);

	}

	public void addOption(Option option) {
		optionlist.add(option);
	}

	public List<Option> getOptionlist() {
		return optionlist;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		if (type.equals("none")) {
			// for none type, value equals name
			return name;
		}
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}



	public EditText getView() {
		return view;
	}

	public void setView(EditText view) {
		this.view = view;
	}

	public int getCheckedRadioButtonId() {
		return checkedRadioButtonId;
	}

	public void setCheckedRadioButtonId(int checkedRadioButtonId) {
		this.checkedRadioButtonId = checkedRadioButtonId;
	}

}