package org.webForStatistics.Manager;

import org.bdp4j.util.CSVDatasetWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.net.URL;

public class ManagerBooking {
    private URL urlProvince;
    private Document docHotel;
    private CSVDatasetWriter csvDatasetWriter;
    private CSVDatasetWriter csvUbicationWriter;
    private final int COLUMNS_AFTER_SERVICES = 17;

    public ManagerBooking(URL url){
        this.urlProvince = url;
        WebDriver driver = new FirefoxDriver();
        try {
            driver.get(url.toString());
            this.docHotel = Jsoup.parse(driver.getPageSource()).normalise();
        } finally {
            driver.quit();
        }
        this.csvDatasetWriter = new CSVDatasetWriter("pruebaBooking.csv");
        this.csvUbicationWriter = new CSVDatasetWriter("prueba-ubicationBooking.csv");
    }
    //sr-card_details__inner
}
