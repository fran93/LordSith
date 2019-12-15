package com.fran.lordsith.enums;

public enum PagesEnum {

	LOBBY("https://lobby.ogame.gameforge.com/de_DE/");
	
	private String url;
	
	PagesEnum(String url) {
		this.url=url;
	}

	public String getUrl() {
		return url;
	}
	
}
