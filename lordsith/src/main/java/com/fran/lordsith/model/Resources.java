package com.fran.lordsith.model;

public class Resources {

	private double metall;
	private double kristall;
	private double deuterium;
	private double energie;

	public Resources(double metall) {
		super();
		this.metall = metall;
	}
	
	public Resources(double metall, double kristall) {
		super();
		this.metall = metall;
		this.kristall = kristall;
	}
	
	public Resources(double metall, double kristall, double deuterium) {
		super();
		this.metall = metall;
		this.kristall = kristall;
		this.deuterium = deuterium;
	}
	
	public Resources(double metall, double kristall, double deuterium, double energie) {
		super();
		this.metall = metall;
		this.kristall = kristall;
		this.deuterium = deuterium;
		this.energie = energie;
	}

	public double getMetall() {
		return metall;
	}

	public void setMetall(double metall) {
		this.metall = metall;
	}

	public double getKristall() {
		return kristall;
	}

	public void setKristall(double kristall) {
		this.kristall = kristall;
	}

	public double getDeuterium() {
		return deuterium;
	}

	public void setDeuterium(double deuterium) {
		this.deuterium = deuterium;
	}

	public double getEnergie() {
		return energie;
	}

	public void setEnergie(double energie) {
		this.energie = energie;
	}

}
