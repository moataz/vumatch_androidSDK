package com.vufind.android.vumatch;

/**
 * This class describes VuMatch reccommendation. Each object has two attributes id (SKUid of item) and score (recommendation score). 
 * @author Vufind
 * 
 */
public class VuMatchRecommendation {
	
	private String id;
	private float score;
	
	public VuMatchRecommendation() {
		
	}
	
	/**
	 * Constructs the reccommendation object and sets its id and score values
	 * @param id
	 * @param score
	 */
	public VuMatchRecommendation(String id, float score) {
		this.id = id;
		this.score = score;
	}
	
	/**
	 * Get the SKU if of the item recommended
	 * @return SKU id of the item recommended
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Set the value of SKU id of the item recommended
	 * @param id SKU id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Get the score of recommendation
	 * @return a floting point value between 0 and 1 represents the score recommendation
	 */
	public float getScore() {
		return score;
	}
	
	/**
	 * Set the value of score of recommendation
	 * @param score
	 */
	public void setScore(float score) {
		this.score = score;
	}
}
