package com.fran.lordsith.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fran.lordsith.services.CommanderService;
import com.fran.lordsith.services.LoginService;


@Component
public class Home {
	
	@Autowired @Lazy
	private CommanderService commanderService;
	
	@Autowired @Lazy
	private LoginService loginService;
	
	Logger log = LoggerFactory.getLogger(Home.class);

	@Scheduled(cron = "3 */15 6-21 * * *")
	public void daily() {
		log.info("Daily job");
		commanderService.command();
	}
	
	@Scheduled(cron = "0 0 0 * * *")
	public void midnight() {
		log.info("Midnight");
		commanderService.command();
	}
	
	@Scheduled(cron = "0 0 3 * * *")
	public void devilTime() {
		log.info("DevilTime");
		commanderService.command();
	}

}
