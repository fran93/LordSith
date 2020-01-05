package com.fran.lordsith.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fran.lordsith.services.CommanderService;
import com.fran.lordsith.services.LoginService;


@Component
public class Tasks {
	
	@Autowired @Lazy
	private CommanderService commanderService;
	
	@Autowired @Lazy
	private LoginService loginService;

	@Scheduled(cron = "3 */15 6-21 * * *")
	public void daily() throws InterruptedException {
		commanderService.command();
	}
	
	@Scheduled(cron = "0 0 0 * * *")
	public void midnight() throws InterruptedException {
		commanderService.command();
	}
	
	@Scheduled(cron = "0 0 3 * * *")
	public void devilTime() throws InterruptedException {
		commanderService.command();
	}

}
