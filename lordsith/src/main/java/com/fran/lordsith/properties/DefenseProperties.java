package com.fran.lordsith.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "per.million.points")
public class DefenseProperties {

  private int raketenwerfer;

  private int leichteslasergeschutz;

  private int schwereslasergeschutz;

  private int ionengeschuz;

  private int gausskanone;

  private int plasmawerfer;

  private int abfangrakete;

  public int getRaketenwerfer() {
    return raketenwerfer;
  }

  public void setRaketenwerfer(int raketenwerfer) {
    this.raketenwerfer = raketenwerfer;
  }

  public int getLeichteslasergeschutz() {
    return leichteslasergeschutz;
  }

  public void setLeichteslasergeschutz(int leichteslasergeschutz) {
    this.leichteslasergeschutz = leichteslasergeschutz;
  }

  public int getSchwereslasergeschutz() {
    return schwereslasergeschutz;
  }

  public void setSchwereslasergeschutz(int schwereslasergeschutz) {
    this.schwereslasergeschutz = schwereslasergeschutz;
  }

  public int getIonengeschuz() {
    return ionengeschuz;
  }

  public void setIonengeschuz(int ionengeschuz) {
    this.ionengeschuz = ionengeschuz;
  }

  public int getGausskanone() {
    return gausskanone;
  }

  public void setGausskanone(int gausskanone) {
    this.gausskanone = gausskanone;
  }

  public int getPlasmawerfer() {
    return plasmawerfer;
  }

  public void setPlasmawerfer(int plasmawerfer) {
    this.plasmawerfer = plasmawerfer;
  }

  public int getAbfangrakete() {
    return abfangrakete;
  }

  public void setAbfangrakete(int abfangrakete) {
    this.abfangrakete = abfangrakete;
  }
}
