package com.fran.lordsith.schedule;

import com.fran.lordsith.services.CommanderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class Tasks {

	@Autowired
	@Lazy
	CommanderService commanderService;

	@Scheduled(cron = "3 */20 6-21 * * *")
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
