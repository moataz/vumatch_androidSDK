package com.vufind.android.vumatch;

public class VuMatchRecommendation {
	
	private String id;
	private float score;
	
	public VuMatchRecommendation() {
		
	}
	
	public VuMatchRecommendation(String id, float score) {
		this.id = id;
		this.score = score;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public float getScore() {
		return score;
	}
	
	public void setScore(float score) {
		this.score = score;
	}
}
