package org.webForStatistics.Manager;

import org.bdp4j.util.CSVDatasetWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ManagerBooking {
    private URL urlProvince;
    private Document docHotel;
    private CSVDatasetWriter csvDatasetWriter;
    private CSVDatasetWriter csvUbicationWriter;
    private final int COLUMNS_AFTER_SERVICES = 17;

    public ManagerBooking(URL url) {
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

    private int getNumPagesHotels() {
        Elements elementsNumPages = this.docHotel.getElementsByClass("bui-u-inline");
        return Integer.parseInt(elementsNumPages.last().text());
    }

    private int getNumPagesComments(Document doc) {
        Elements elements = doc.getElementsByClass("bui-pagination__link");
        return Integer.parseInt(elements.last().text().split(" ")[0]);
    }

    //sr-card_details__inner
    //https://www.booking.com/searchresults.es.html?aid=397594&label=gog235jc-1DCAEoggI46AdIClgDaEaIAQGYAQq4ARfIAQzYAQPoAQH4AQKIAgGoAgO4AqKnuusFwAIB&sid=029c99cb2c65f0650b7f773436fe832d&tmpl=searchresults&class_interval=1&dest_id=-394670&dest_type=city&dtdisc=0&from_sf=1&group_adults=2&group_children=0&inac=0&index_postcard=0&label_click=undef&no_rooms=1&postcard=0&room1=A%2CA&sb_price_type=total&shw_aparth=1&slp_r_match=0&src=index&src_elem=sb&srpvid=321573532f0b02e8&ss=ourense&ss_all=0&ssb=empty&sshis=0&rows=15&offset=0
    public void run() {
        Map<String, Object> defCsv = new LinkedHashMap<>();
        Map<String, Object> ubicationCsv = new LinkedHashMap<>();
        System.out.println(getNumPagesHotels());
        AtomicInteger numPagesHotels = new AtomicInteger(1);
        int num = getNumPagesHotels();
        List<String> hotelsAlready = new ArrayList<>();
        while (numPagesHotels.get() <= num) {
            String nextPageOfHotel = String.format("%s&rows=%d&offset=%d", this.urlProvince.toString(), 15, numPagesHotels.intValue() * 15);
            numPagesHotels.getAndIncrement();
            try {
                URL urlNexPageOfHotel = new URL(nextPageOfHotel);
                WebDriver driver = new FirefoxDriver();
                try {
                    driver.get(urlNexPageOfHotel.toString());
                    this.docHotel = Jsoup.parse(driver.getPageSource()).normalise();
                } finally {
                    driver.quit();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            for (Element element : this.docHotel.getElementsByClass("sr_item_content sr_item_content_slider_wrapper ")) {
                String nameHotel = element.getElementsByClass("sr-hotel__name").text();
                System.out.println("hotel: " + nameHotel);
                System.out.println(hotelsAlready);
                if (!hotelsAlready.contains(nameHotel)) {
                    hotelsAlready.add(nameHotel);
                    Element linkLoc = element.getElementsByClass("bui-link").first();
                    String location = linkLoc.text();
                    float lng = Float.parseFloat(linkLoc.attr("data-coords").split(",")[0]);
                    float lat = Float.parseFloat(linkLoc.attr("data-coords").split(",")[1]);
                    System.out.println("lng: " + lng);
                    System.out.println("lat: " + lat);
                    String province = "provincia: Ourense";
                    location = location.split(" ")[0];
                    System.out.println("ciudad: " + location);
                    float totalEvaluation = 0;
                    if (element.getElementsByClass("bui-review-score__badge").size() > 0) {
                        totalEvaluation = Float.parseFloat(element.getElementsByClass("bui-review-score__badge").text().replace(",", "."));
                    }
                    System.out.println("valoración total: " + totalEvaluation);
                    int category = 0;
                    if(element.getElementsByClass("bk-icon-wrapper").size() > 0){
                        category = Integer.parseInt(element.getElementsByClass("bk-icon-wrapper").attr("title").split(" ")[2]);
                    }
                    System.out.println("categoria: "+ category);

                    String hotel = String.format("https://booking.com%s", element.getElementsByClass("hotel_name_link url").attr("href").replaceAll("\n", ""));
                    boolean moreThanOne = false;
                    try {
                        URL hotelUrl = new URL(hotel);
                        WebDriver driver = new FirefoxDriver();
                        Document docFromHotel = null;
                        boolean stop;
                        int numComments = 0;
                        do {
                            stop = true;
                            try {

                                driver.get(hotelUrl.toString());
                                WebElement webElement = driver.findElement(By.id("show_reviews_tab"));
                                WebDriverWait wait = new WebDriverWait(driver, 15);
                                wait.until(ExpectedConditions.visibilityOf(webElement));
                                webElement.click();
                                WebDriverWait wait2 = new WebDriverWait(driver, 15);
                                wait2.until(ExpectedConditions.visibilityOfElementLocated(By.id("review_list_score_container")));
                                numComments = 0;
                                if(driver.findElements(By.className("bui-review-score__text")).size() > 0) {
                                    numComments = Integer.parseInt(driver.findElements(By.className("bui-review-score__text")).get(0).getText().split(" ")[0].replace(".", ""));
                                    if (numComments > 10) {
                                        moreThanOne = true;
                                    }
                                }
                                if (moreThanOne) {
                                    wait2.until(ExpectedConditions.visibilityOfElementLocated(By.className("bui-pagination__link")));
                                }
                                docFromHotel = Jsoup.parse(driver.getPageSource());
                            } catch (Exception exception) {
                                stop = false;
                            }
                            driver.quit();
                        } while (!stop);
                        HashMap<String,Object> valorations = new LinkedHashMap<>();
                        valorations.put("Personal","");
                        valorations.put("Intalaciones y servicios","");
                        valorations.put("Limpieza","");
                        valorations.put("Confort","");
                        valorations.put("Relación calidad - precio","");
                        valorations.put("Ubicación ","");
                        valorations.put("WiFi gratis","");
                        if(numComments > 0){

                        }
                        if (!moreThanOne) {
                            Elements elements = docFromHotel.getElementsByClass("review_list_new_item_block");
                            if (elements.size() > 0) {
                                for (Element e : elements) {

                                }
                            } else {

                            }

                        } else {
                            int numPages = getNumPagesComments(docFromHotel);
                            String page = docFromHotel.getElementsByClass("bui-pagination__link").get(2).attr("href");
                            //System.out.println(page);
                            //System.out.println(page.substring(0,page.length()-17));
                            for (int i = 1; i <= numPages; i++) {
                                URL urlPageComment = new URL(String.format("https://www.booking.com%soffset=%d;rows=10", page.substring(0, page.length() - 17), i * 10 - 10));
                                Document commentDoc = Jsoup.connect(urlPageComment.toString()).get();
                                System.out.println(urlPageComment);
                                for(Element element1: commentDoc.getElementsByClass("review_list_new_item_block")){
                                    String title = element1.getElementsByClass("c-review-block__title").text();
                                    String positiveComment= "";
                                    if (element1.getElementsByClass("c-review__prefix c-review__prefix--color-green").size() > 0){
                                        positiveComment = element1.getElementsByClass("c-review__prefix c-review__prefix--color-green").get(0).siblingElements().text();
                                        System.out.println("Comentario positivo: "+positiveComment);
                                    }
                                    String negativeComment = "";
                                    if (element1.getElementsByClass("c-review__prefix").size() > 1){
                                        negativeComment = element1.getElementsByClass("c-review__prefix").get(1).siblingElements().text();
                                        System.out.println("Comentario negativo: "+ negativeComment);
                                    }
                                    float infividualValoration = 0;
                                    infividualValoration = Float.parseFloat(element1.getElementsByClass("bui-review-score__badge").text().replace(",","."));
                                    System.out.println("Valoración individual: "+infividualValoration);
                                    String date = element1.getElementsByClass("c-review-block__date").get(1).text();
                                    System.out.println("fecha: "+date);
                                    if (element1.getElementsByClass("review-helpful__vote-feedback-message review-helpful-heart-vote-feedback-message review-helpful__vote-others-helpful ").size() >0 &&
                                    !element1.getElementsByClass("review-helpful__vote-feedback-message review-helpful-heart-vote-feedback-message review-helpful__vote-others-helpful ").get(0).hasAttr("style")) {
                                        System.out.println(element1.getElementsByClass("review-helpful__vote-feedback-message review-helpful-heart-vote-feedback-message review-helpful__vote-others-helpful ").get(0).getElementsByTag("strong").text());
                                    }else{
                                        System.out.println("no hay");
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println(hotel);
                    //
                }
            }


        }

    }
}
