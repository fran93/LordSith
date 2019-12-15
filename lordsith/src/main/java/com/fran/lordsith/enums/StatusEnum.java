package com.fran.lordsith.enums;

public enum StatusEnum {

	ON("on"),
	OFF("off"),
	ACTIVE("active"),
	DISABLED("disabled");
	
	private String value;
	
	StatusEnum(String value){
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
}
