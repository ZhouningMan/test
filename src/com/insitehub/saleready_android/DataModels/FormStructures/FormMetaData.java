package com.insitehub.saleready_android.DataModels.FormStructures;

import java.io.Serializable;

public class FormMetaData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String FORM_SETS = "formsets";
	public static final String ASSESSMENT_FORMS = "assessmentForms";
	public static final String CERTIFICATION_FORMS = "certificationForms";
	public static final String COACHING_FORMS = "coachingForms";
	public static final String MAIN_FORM = "mainform";
	public static final String SAVED_FORM = "savedform";
	public static final String CREATED_AT = "createdAt";
	public static final String OBJECT_ID = "objectId";
	public static final String FROM_CLASS = "formClass";
	public static final String NAME = "name";
	public static final String COACHING_SET = "set";
	
	
	
	private String formClass;
	private String name;
	private String objectId;
	private String dateOfCreation;
	
	public FormMetaData(){
		
	}
	
	
	public FormMetaData(String name, String objectId, String dateOfCreation, String formClass) {
		super();
		this.name = name;
		this.objectId = objectId;
		this.dateOfCreation = dateOfCreation;
		this.formClass = formClass;
	}
	
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
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	public String getDateOfCreation() {
		return dateOfCreation;
	}
	public void setDateOfCreation(String dateOfCreation) {
		this.dateOfCreation = dateOfCreation;
	}
	
}
