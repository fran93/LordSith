package com.fran.lordsith.services;

import com.fran.lordsith.enums.TechnologyEnum;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MilitaryService {

  private static final int MIN_CARGOS_TO_ATTACK = 10;
  @Autowired
  @Lazy
  FleetService fleetService;
  @Autowired
  @Lazy
  FirefoxClient firefox;
  @Autowired
  @Lazy
  MessageSource messageSource;

  Logger log = LoggerFactory.getLogger(MilitaryService.class);

  private void processMessages() throws InterruptedException {
    List<String> messagesIds = new ArrayList<>();
    getMessages().forEach(msg -> messagesIds.add(msg.getAttribute("data-msg-id")));

    for (String id : messagesIds) {
      processMessage(id);
    }
  }

  private List<WebElement> getMessages() throws InterruptedException {
    firefox.loading(2);
    try {
      return firefox.get().findElements(By.className("msg")).stream()
          .filter(msg -> msg.getAttribute("data-msg-id") != null && !msg.getAttribute("data-msg-id").trim().isEmpty()).collect(Collectors.toList());
    } catch (StaleElementReferenceException ex) {
      log.info("getMessages " + ex.getMessage());
    }

    return new ArrayList<>();
  }

  private void processMessage(String id) throws InterruptedException {
    try {
      firefox.loading(2);
      Optional<WebElement> message = getMessages().stream().filter(msg -> msg.getAttribute("data-msg-id").equals(id)).findFirst();
      if (message.isPresent()) {
        List<WebElement> rows = message.get().findElements(By.className("compacting"));
        if (rows.size() >= 5) {
          int necesaryFleet = getNecesaryFleet(rows);
          String rawDefenses = rows.get(4).findElement(By.className("tooltipRight")).getText().split(":")[1];
          String rawFlotten = rows.get(4).findElement(By.className("tooltipLeft")).getText().split(":")[1];
          long defenses = parseDefenses(rawDefenses);
          defenses += parseDefenses(rawFlotten);

          WebElement iconAttack = message.get().findElement(By.className("icon_attack"));
          boolean notAttackedYet = iconAttack.findElements(By.className("fright")).isEmpty();

          if (necesaryFleet >= MIN_CARGOS_TO_ATTACK && notAttackedYet) {
            firefox.jsClick(iconAttack);

            firefox.loading(1);
            if (fleetService.isFleetAvailable()) {
              sendAttack(id, necesaryFleet, defenses);
            }
            openMessages();
          } else {
            log.info(messageSource.getMessage("fleet.discard", null, Locale.ENGLISH));
            firefox.jsClick(message.get().findElement(By.className("icon_refuse")));
            firefox.loading(1);
          }
        }
      }
    } catch (StaleElementReferenceException ex) {
      log.info("processMessage " + ex.getMessage());
    }
  }

  private long parseDefenses(String rawDefenses) {
    long defenses;
    if (rawDefenses.contains("M")) {
      rawDefenses = rawDefenses.replaceFirst("M", "");
      defenses = Long.parseLong(rawDefenses.split(",")[0].trim());
      defenses *= 1000000;
    } else {
      defenses = Long.parseLong(rawDefenses.trim().replaceAll("\\.", ""));
    }
    return defenses;
  }

  private void sendAttack(String id, int necesaryFleet, long defenses) throws InterruptedException {
    try {
      if (defenses == 0) {
        firefox.get().findElement(By.name("transporterSmall")).sendKeys(String.valueOf(necesaryFleet));
        log.info(messageSource.getMessage("fleet.attack", new Object[]{necesaryFleet, TechnologyEnum.KLEINER_TRANSPORTER.name()}, Locale.ENGLISH));
      } else if (defenses < 1000000) {
        int countKreuzer = fleetService.numberOfShips(TechnologyEnum.KREUZER.getId());
        long militaryFleet = defenses / 2000;
        if (countKreuzer >= militaryFleet) {
          firefox.get().findElement(By.name("transporterSmall")).sendKeys(String.valueOf(necesaryFleet));
          firefox.get().findElement(By.name("cruiser")).sendKeys(String.valueOf(militaryFleet));
          log.info(messageSource.getMessage("fleet.attack", new Object[]{militaryFleet, TechnologyEnum.KREUZER.name()}, Locale.ENGLISH));
        }
      } else {
        int countBalls = fleetService.numberOfShips(TechnologyEnum.TODESSTERN.getId());
        long militaryFleet = defenses / 500000;
        firefox.loading(1);
        if (countBalls >= militaryFleet) {
          firefox.get().findElement(By.name("deathstar")).sendKeys(String.valueOf(militaryFleet));
          log.info(messageSource.getMessage("fleet.attack", new Object[]{militaryFleet, TechnologyEnum.TODESSTERN.name()}, Locale.ENGLISH));
        }
      }

      if (fleetService.canContinue(FleetService.CONTINUE_TO_FLEET2)) {
        fleetService.weiterWeiter(FleetService.CONTINUE_TO_FLEET2);
        if (fleetService.canContinue(FleetService.CONTINUE_TO_FLEET3)) {
          fleetService.weiterWeiter(FleetService.CONTINUE_TO_FLEET3);
          if (fleetService.canContinue(FleetService.SEND_FLEET)) {
            fleetService.weiterWeiter(FleetService.SEND_FLEET);

            firefox.loading(1);
            openMessages();

            if (firefox.get().getCurrentUrl().trim().endsWith("messages")) {
              firefox.jsClick(firefox.get().findElement(By.xpath("//li[@data-msg-id=" + id + "]")).findElement(By.className("icon_refuse")));
            }
          }
        }
      }
    } catch (ElementClickInterceptedException | NoSuchElementException ex) {
      log.info("sendAttack: " + ex.getMessage());
    }
  }

  private int getNecesaryFleet(List<WebElement> rows) {
    StringBuilder title = new StringBuilder(rows.get(2).findElement(By.className("tooltipClose")).getAttribute("title"));
    title.delete(0, title.indexOf("am202="));
    title.delete(0, 6);
    title.delete(title.indexOf("\""), title.length());
    return Integer.parseInt(title.toString());
  }

  public void hunting() throws InterruptedException {
    openMessages();
    processMessages();
  }

  private void openMessages() {
    if (!firefox.get().getCurrentUrl().contains("page=messages")) {
      try {
        firefox.loading(1);
        firefox.loading(By.className("messages"));
        firefox.get().findElement(By.className("messages")).click();
      } catch (NoSuchElementException | TimeoutException | InterruptedException ex) {
        log.info("openMessages: " + ex.getMessage());
      }
    }
  }

}
