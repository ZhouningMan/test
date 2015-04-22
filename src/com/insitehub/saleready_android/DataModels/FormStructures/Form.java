package com.insitehub.saleready_android.DataModels.FormStructures;

import java.util.LinkedList;
import java.util.List;

public class Form{
	/**
	 * 
	 */
	public final static String FORM = "form";
	public final static String COMPETENCYLIST = "competencylist";
	public final static String FORMCLASS = "formClass";
	public final static String NAME = "name";
	public final static String STATUS = "status";
	public final static String OBJECT_ID = "objectId";
	
	private String formClass;
	private String name;
	private String status;
	private String userid;
	private String objectID;
	private boolean writable;
	
	
	public boolean isWritable() {
		return writable;
	}

	public void setWritable(boolean writable) {
		this.writable = writable;
	}

	private List<CompetencyListItem> competencyList = new LinkedList<CompetencyListItem>();

	public String getFormClass() {
		return formClass;
	}

	public void setFormClass(String formClass) {
		this.formClass = formClass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<CompetencyListItem> getCompetencyList() {
		return competencyList;
	}

	public void setCompetencyList(List<CompetencyListItem> competencyList) {
		this.competencyList = competencyList;
	}
	
	public void addCompetencyListItem(CompetencyListItem item){
		competencyList.add(item);
	}
	
	@Override
	public String toString() {
		String form = name + "\n" + status + "\n" + competencyList.size();
		return form;
	}
	
	public void setUserID(String userid){
		this.userid = userid;
	}
	
	public String getUserID(){
		return this.userid;
	}
	
	public void setObjectID(String objectID){
		this.objectID = objectID;
	}
	
	public String getObjectID(){
		return this.objectID;
	}
	



	
	
	
}
