package com.fran.lordsith.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "expedition")
public class ExpeditionProperties {

  private int level0;
  private int level1;
  private int level2;
  private int level3;
  private int level4;
  private int level5;
  private int level6;
  private int level7;

  public int getLevel0() {
    return level0;
  }

  public void setLevel0(int level0) {
    this.level0 = level0;
  }

  public int getLevel1() {
    return level1;
  }

  public void setLevel1(int level1) {
    this.level1 = level1;
  }

  public int getLevel2() {
    return level2;
  }

  public void setLevel2(int level2) {
    this.level2 = level2;
  }

  public int getLevel3() {
    return level3;
  }

  public void setLevel3(int level3) {
    this.level3 = level3;
  }

  public int getLevel4() {
    return level4;
  }

  public void setLevel4(int level4) {
    this.level4 = level4;
  }

  public int getLevel5() {
    return level5;
  }

  public void setLevel5(int level5) {
    this.level5 = level5;
  }

  public int getLevel6() {
    return level6;
  }

  public void setLevel6(int level6) {
    this.level6 = level6;
  }

  public int getLevel7() {
    return level7;
  }

  public void setLevel7(int level7) {
    this.level7 = level7;
  }
}
