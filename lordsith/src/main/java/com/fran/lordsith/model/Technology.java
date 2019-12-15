package com.fran.lordsith.model;

public class Technology {

	private int id;
	private int level;
	private String status;
	private Resources cost;
	private double totalCost;
	
	public Technology(int id, int level, String status) {
		super();
		this.id = id;
		this.level = level;
		this.status = status;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public double getTotalCost() {
		return totalCost;
	}
	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}
	public Resources getCost() {
		return cost;
	}
	public void setCost(Resources cost) {
		this.cost = cost;
	}
	
}
