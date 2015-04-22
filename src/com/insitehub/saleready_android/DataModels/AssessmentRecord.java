package com.insitehub.saleready_android.DataModels;

public class AssessmentRecord {
	
	public static final String COMPETENCIES = "competencies";
	public static final String NAME = "name";
	public static final String SUM = "sum";
	public static final String COUNT = "count";
	public static final String AVERAGE = "average";
	public static final String ACTIONS = "actions";
	
	
	
	private String name;
	private String sum;
	private String count;
	private String average;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSum() {
		return sum;
	}
	public void setSum(String sum) {
		this.sum = sum;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public String getAverage() {
		return average;
	}
	public void setAverage(String average) {
		this.average = average;
	}
	
}
