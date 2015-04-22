package com.insitehub.saleready_android.DataModels.FormStructures;


public class PreloadedForms {
	public static final Form assessmentForm = new Form();
	public static final Form certificationForm = new Form();
	public static final Form preCoachingForm = new Form();
	public static final Form fieldCoachingForm = new Form();
	public static final Form postCoachingForm = new Form();
	
	
	private static boolean assessmentFormLoaded = false;
	private static boolean certificationFormLoaded = false;
	private static boolean preCoachingFormLoaded = false;
	private static boolean fieldCoachingFormLoaded = false;
	private static boolean postCoachingFormLoaded = false;
	
	
	public static boolean isAssessmentFormLoaded() {
		return assessmentFormLoaded;
	}
	public static void setAssessmentFormLoaded(boolean assessmentFormLoaded) {
		PreloadedForms.assessmentFormLoaded = assessmentFormLoaded;
	}
	public static boolean isCertificationFormLoaded() {
		return certificationFormLoaded;
	}
	public static void setCertificationFormLoaded(boolean certificationFormLoaded) {
		PreloadedForms.certificationFormLoaded = certificationFormLoaded;
	}
	public static boolean isPreCoachingFormLoaded() {
		return preCoachingFormLoaded;
	}
	public static void setPreCoachingFormLoaded(boolean preCoachingFormLoaded) {
		PreloadedForms.preCoachingFormLoaded = preCoachingFormLoaded;
	}
	public static boolean isFieldCoachingFormLoaded() {
		return fieldCoachingFormLoaded;
	}
	public static void setFieldCoachingFormLoaded(boolean fieldCoachingFormLoaded) {
		PreloadedForms.fieldCoachingFormLoaded = fieldCoachingFormLoaded;
	}
	public static boolean isPostCoachingFormLoaded() {
		return postCoachingFormLoaded;
	}
	public static void setPostCoachingFormLoaded(boolean postCoachingFormLoaded) {
		PreloadedForms.postCoachingFormLoaded = postCoachingFormLoaded;
	}
}
