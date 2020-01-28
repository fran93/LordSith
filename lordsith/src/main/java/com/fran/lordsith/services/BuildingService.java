package com.fran.lordsith.services;

import com.fran.lordsith.enums.MenuEnum;
import com.fran.lordsith.enums.StatusEnum;
import com.fran.lordsith.enums.TechnologyEnum;
import com.fran.lordsith.model.Resources;
import com.fran.lordsith.model.Technology;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class BuildingService {

    private static final String DATA_STATUS = "data-status";
    private static final String DATA_VALUE = "data-value";
    private static final String LEVEL = "level";
    private static final String DATA_RAW = "data-raw";
    private static final String TITLE = "title";
    private static final int FACILITIES_COST_MULTIPLIER = 3;
    private static final int MAX_SOLARKRAFTWERK_LEVEL = 26;
    private static final int MAX_ROBOTERFABRIK_LEVEL = 10;
    private static final int MAX_RAUMSCHIFFSWERFT_LEVEL = 12;

    @Autowired
    @Lazy
    FirefoxClient firefox;

    @Autowired
    @Lazy
    MessageSource messageSource;

    @Autowired
    @Lazy
    PlanetService planetService;

    @Autowired
    @Lazy
    HangarService hangarService;

    @Autowired
    @Lazy
    MenuService menuService;

    @Autowired
    @Lazy
    TechnologyService technologyService;

    Logger log = LoggerFactory.getLogger(BuildingService.class);

    public boolean buildMinesOrFacilities() throws InterruptedException {
        ArrayList<Technology> mines = new ArrayList<>();
        ArrayList<Technology> powerPlants = new ArrayList<>();
        ArrayList<Technology> storages = new ArrayList<>();
        ArrayList<Technology> facilities = new ArrayList<>();
        AtomicBoolean building = new AtomicBoolean(false);
        try {
            menuService.openPage(MenuEnum.VERSORGUNG);

            if (menuService.isOnPage(MenuEnum.VERSORGUNG)) {
                parseMines(mines, powerPlants, storages, building);

                if (!building.get()) {
                    menuService.openPage(MenuEnum.ANLAGEN);

                    parseFacilities(facilities, building);
                    chooseWhatToBuild(mines, powerPlants, storages, facilities, building, parseResources(), parseStorage());
                }
            }
        } catch (NoSuchElementException ex) {
            log.info("buildMinesOrFacilities: " + ex.getMessage());
        }

        return building.get();
    }

    private Resources parseStorage() {
        double metallStorage = processStorage(firefox.get().findElement(By.id("metal_box")).getAttribute(TITLE));
        double kristallStorage = processStorage(firefox.get().findElement(By.id("crystal_box")).getAttribute(TITLE));
        double deuteriumStorage = processStorage(firefox.get().findElement(By.id("deuterium_box")).getAttribute(TITLE));
        return new Resources(metallStorage, kristallStorage, deuteriumStorage, 0);
    }

    public Resources parseResources() {
        double energy = Double.parseDouble(firefox.get().findElement(By.id("resources_energy")).getAttribute(DATA_RAW));
        double metall = Double.parseDouble(firefox.get().findElement(By.id("resources_metal")).getAttribute(DATA_RAW));
        double kristall = Double.parseDouble(firefox.get().findElement(By.id("resources_crystal")).getAttribute(DATA_RAW));
        double deuterium = Double.parseDouble(firefox.get().findElement(By.id("resources_deuterium")).getAttribute(DATA_RAW));
        return new Resources(metall, kristall, deuterium, energy);
    }

    private void parseFacilities(ArrayList<Technology> facilities, AtomicBoolean building) {
        firefox.get().findElements(By.className("technology")).forEach(technology -> {
            int id = Integer.parseInt(technology.getAttribute("data-technology"));
            String status = technology.getAttribute(DATA_STATUS);
            if (status.equals(StatusEnum.ACTIVE.getValue())) {
                building.set(true);
            }

            if ((id == TechnologyEnum.RAKETENSILO.getId() && !status.equals(StatusEnum.OFF.getValue())) || (id == TechnologyEnum.FORSCHUNGSLABOR.getId() && status.equals(StatusEnum.ON.getValue()))
                    || (id == TechnologyEnum.NANITENFABRIK.getId() && status.equals(StatusEnum.ON.getValue()))
                    || (id == TechnologyEnum.RAUMSCHIFFSWERFT.getId() && status.equals(StatusEnum.ON.getValue())
                    && Integer.parseInt(technology.findElement(By.className(LEVEL)).getAttribute(DATA_VALUE)) < MAX_RAUMSCHIFFSWERFT_LEVEL)
                    || (id == TechnologyEnum.ROBOTERFABRIK.getId() && !status.equals(StatusEnum.OFF.getValue())
                    && Integer.parseInt(technology.findElement(By.className(LEVEL)).getAttribute(DATA_VALUE)) < MAX_ROBOTERFABRIK_LEVEL)) {
                parseTechnology(facilities, technology, id, status);
            }

        });
    }

    private void parseMines(ArrayList<Technology> mines, ArrayList<Technology> powerPlants, ArrayList<Technology> storages, AtomicBoolean building) {
        firefox.get().findElements(By.className("technology")).forEach(technology -> {
            int id = Integer.parseInt(technology.getAttribute("data-technology"));
            String status = technology.getAttribute(DATA_STATUS);
            if (status.equals(StatusEnum.ACTIVE.getValue())) {
                building.set(true);
            }

            if (id <= TechnologyEnum.DEUTERIUMSYNTHETISIERER.getId()) {
                parseTechnology(mines, technology, id, status);
            } else if (id == TechnologyEnum.SOLARKRAFTWERK.getId() && Integer.parseInt(technology.findElement(By.className(LEVEL)).getAttribute(DATA_VALUE)) < MAX_SOLARKRAFTWERK_LEVEL) {
                parseTechnology(powerPlants, technology, id, status);
            } else if (id == TechnologyEnum.FUSIONKRAFTWERK.getId() && !status.equals(StatusEnum.OFF.getValue())) {
                parseTechnology(powerPlants, technology, id, status);
            } else if (id >= TechnologyEnum.METALLSPEICHER.getId() && id <= TechnologyEnum.DEUTERIUMTANK.getId()) {
                parseTechnology(storages, technology, id, status);
            }
        });
    }

    private void chooseWhatToBuild(ArrayList<Technology> mines, ArrayList<Technology> powerPlants, ArrayList<Technology> storages, ArrayList<Technology> facilities, AtomicBoolean building,
                                   Resources resources, Resources storage) throws InterruptedException {

        if (!planetService.hasFreeFields()) {
            buildTerraformer(building, resources);
        } else if (!building.get()) {
            Optional<Technology> naniten = facilities.stream().filter(facility -> facility.getId() == TechnologyEnum.NANITENFABRIK.getId()).findFirst();
            if (naniten.isPresent()) {
                upTechnology(naniten.get(), building);
            } else if (storage.getDeuterium() < resources.getDeuterium()) {
                resourcesOverflow(storages, building, TechnologyEnum.DEUTERIUMTANK.getId());
            } else if (storage.getKristall() < resources.getKristall()) {
                resourcesOverflow(storages, building, TechnologyEnum.KRISTALLSPEICHER.getId());
            } else if (storage.getMetall() < resources.getMetall()) {
                resourcesOverflow(storages, building, TechnologyEnum.METALLSPEICHER.getId());
            } else if (resources.getEnergie() < 0) {
                menuService.openPage(MenuEnum.VERSORGUNG);

                powerPlants.sort(Comparator.comparingDouble(Technology::getTotalCost));
                upTechnology(powerPlants.get(0), building);
            } else {
                mines.sort(Comparator.comparingDouble(Technology::getTotalCost));
                facilities.sort(Comparator.comparingDouble(Technology::getTotalCost));
                if (!facilities.isEmpty() && facilities.get(0).getTotalCost() * FACILITIES_COST_MULTIPLIER < mines.get(0).getTotalCost()) {
                    upTechnology(facilities.get(0), building);
                } else {
                    menuService.openPage(MenuEnum.VERSORGUNG);

                    upTechnology(mines.get(0), building);
                }
            }
        }
    }

    private void buildTerraformer(AtomicBoolean building, Resources resources) throws InterruptedException {
        WebElement terraformer = firefox.get().findElement(By.xpath("//li[@data-technology= " + TechnologyEnum.TERRAFORMER.getId() + "]"));
        int level = Integer.parseInt(terraformer.findElement(By.className(LEVEL)).getAttribute(DATA_VALUE));
        String status = terraformer.getAttribute(DATA_STATUS);
        Technology techno = new Technology(TechnologyEnum.TERRAFORMER.getId(), level, status);
        technologyService.calculateCost(techno);

        if (planetService.hasFields()) {
            if (status.equals(StatusEnum.ON.getValue())) {
                upTechnology(techno, building);
            } else {
                building.set(true);
                if (resources.getEnergie() < techno.getCost().getEnergie()) {
                    hangarService.buildSolarSatelliteFleet();
                }
            }
        }
    }

    private void resourcesOverflow(ArrayList<Technology> storages, AtomicBoolean building, int id) throws InterruptedException {
        menuService.openPage(MenuEnum.VERSORGUNG);

        Optional<Technology> optional = storages.stream().filter(ss -> ss.getId() == id).findFirst();
        if (optional.isPresent())
            upTechnology(optional.get(), building);
    }

    private void parseTechnology(ArrayList<Technology> mines, WebElement technology, int id, String status) {
        int level = Integer.parseInt(technology.findElement(By.className(LEVEL)).getAttribute(DATA_VALUE));
        Technology techno = new Technology(id, level, status);
        technologyService.calculateCost(techno);
        mines.add(techno);
    }

    private void upTechnology(Technology tech, AtomicBoolean building) {
        if (tech.getStatus().equals(StatusEnum.ON.getValue())) {
            firefox.get().findElement(By.xpath("//li[@data-technology=" + tech.getId() + "]")).findElement(By.tagName("button")).click();
            building.set(true);

            log.info(messageSource.getMessage("building.build", new Object[]{TechnologyEnum.getById(tech.getId()).name(), tech.getLevel()}, Locale.ENGLISH));
        }
    }

    private double processStorage(String rawData) {
        StringBuilder sb = new StringBuilder(rawData);
        sb.delete(0, sb.indexOf("</tr>"));
        sb.delete(0, sb.indexOf("class"));
        sb.delete(0, sb.indexOf(">") + 1);
        sb.delete(sb.indexOf("<"), sb.length());
        return Double.parseDouble(sb.toString().replaceAll("\\.", ""));
    }

}
