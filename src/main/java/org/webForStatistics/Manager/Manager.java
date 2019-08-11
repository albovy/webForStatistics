package org.webForStatistics.Manager;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.bdp4j.util.CSVDatasetWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import java.io.IOException;
import java.net.URL;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manager of hotels
 *
 * @author Alejandro Borrajo Viéitez
 */

public class Manager {
    private URL urlHotel;
    private Document docHotel;
    private CSVDatasetWriter csvDatasetWriter;
    private CSVDatasetWriter csvUbicationWriter;
    private final int COLUMNS_AFTER_SERVICES = 17;

    /**
     * Constructs a new instance of {@link Manager}
     *
     * @param url the URL of the province
     */
    public Manager(URL url) {
        this.urlHotel = url;
        WebDriver driver = new FirefoxDriver();
        try {
            driver.get(url.toString());
            this.docHotel = Jsoup.parse(driver.getPageSource()).normalise();
        } finally {
            driver.quit();
        }
        this.csvDatasetWriter = new CSVDatasetWriter("prueba2.csv");
        this.csvUbicationWriter = new CSVDatasetWriter("prueba2-ubication.csv");
    }

    /**
     * Returns the all the opinions pages from a hotel
     *
     * @param doc the DOM of the hotel
     * @return a integer with the pages
     */
    private AtomicInteger getNumPagesFromHotel(Document doc) {
        Elements elementsNumPages = doc.getElementsByClass("pageNum");
        if (elementsNumPages.size() > 0) {
            return new AtomicInteger(Integer.parseInt(elementsNumPages.last().text()));
        } else return new AtomicInteger(1);
    }

    /**
     * Returns all the pages from the province
     *
     * @return a integer with the pages
     */
    private AtomicInteger getNumPagesHotels() {
        Elements elementsNumPages = this.docHotel.getElementsByClass("pageNum");
        return new AtomicInteger(Integer.parseInt(elementsNumPages.last().text()));
    }

    /**
     * Initialize a map with the default values
     *
     * @param map the map to be initialized
     */
    private void initializeCSV(Map<String, Object> map) {
        map.put("idComentario", 0);
        map.put("idTripLocalizacion", 0);
        map.put("nombreLocalizacion", 0);
        map.put("nombreProvincia", 0);
        map.put("idTripHotel", 0);
        map.put("nombreHotel", 0);
        map.put("categoriaHotel", 0);
        map.put("valoracionTotal", 0);
        map.put("valoracionUbicacion", 0);
        map.put("valoracionLimpieza", 0);
        map.put("valoracionServicio", 0);
        map.put("valoracionCalidadPrecio", 0);
        map.put("valoracionIndividual", 0);
        map.put("idTripComentario", 0);
        map.put("tituloComentario", csvDatasetWriter.getStrVoidField());
        map.put("tipoViaje", 0);
        map.put("comentario", 0);
        map.put("consejos", csvDatasetWriter.getStrVoidField());
        map.put("votosUtilesComentario", 0);
        map.put("fecha", 0);
        map.put("usuario", 0);
        map.put("contribucionesUsuario", 0);
        map.put("votosUtilesUsuario", 0);
        map.put("nivelColaboracion", 0);


        String[] columns = map.keySet().toArray(new String[0]);
        csvDatasetWriter.addColumns(columns, map.values().toArray());
    }

    private void initializeUbicationCSV(Map<String,Object> map) {
        map.put("nombreHotel", "");
        map.put("lat", "");
        map.put("lng", "");

        String[] columns = map.keySet().toArray(new String[0]);
        csvUbicationWriter.addColumns(columns, map.values().toArray());
    }

    /**
     * Adds the needed services in the right positions of the map
     *
     * @param map      map where services are added
     * @param services a list of services
     */
    private void addServicesToCsv(Map<String, Object> map, List<String> services) {
        map.remove("valoracionTotal");
        map.remove("valoracionUbicacion");
        map.remove("valoracionLimpieza");
        map.remove("valoracionServicio");
        map.remove("valoracionCalidadPrecio");
        map.remove("valoracionIndividual");
        map.remove("idTripComentario");
        map.remove("tituloComentario");
        map.remove("tipoViaje");
        map.remove("comentario");
        map.remove("consejos");
        map.remove("votosUtilesComentario");
        map.remove("fecha");
        map.remove("usuario");
        map.remove("contribucionesUsuario");
        map.remove("votosUtilesUsuario");
        map.remove("nivelColaboracion");
        List<String> list = new ArrayList<>(map.keySet());
        String key;
        for (int i = 7; i < list.size(); i++) {
            key = list.get(i);
            if (services.contains(key)) {
                map.put(key, 1);
                services.remove(key);
            } else {
                map.put(key, 0);
            }
        }
        for (String str : services) {
            csvDatasetWriter.insertColumnAt(str, 0, csvDatasetWriter.getColumnCount() - COLUMNS_AFTER_SERVICES);
            map.put(str, 1);
        }
        this.addAfterServices(map);
    }

    /**
     * Add the needed values after the poitions of all the services
     *
     * @param map the map where to add the values
     */
    private void addAfterServices(Map<String, Object> map) {
        map.put("valoracionTotal", csvDatasetWriter.getStrVoidField());
        map.put("valoracionUbicacion", csvDatasetWriter.getStrVoidField());
        map.put("valoracionLimpieza", csvDatasetWriter.getStrVoidField());
        map.put("valoracionServicio", csvDatasetWriter.getStrVoidField());
        map.put("valoracionCalidadPrecio", csvDatasetWriter.getStrVoidField());
        map.put("valoracionIndividual", csvDatasetWriter.getStrVoidField());
        map.put("idTripComentario", csvDatasetWriter.getStrVoidField());
        map.put("tituloComentario", csvDatasetWriter.getStrVoidField());
        map.put("tipoViaje", csvDatasetWriter.getStrVoidField());
        map.put("comentario", csvDatasetWriter.getStrVoidField());
        map.put("consejos", csvDatasetWriter.getStrVoidField());
        map.put("votosUtilesComentario", csvDatasetWriter.getStrVoidField());
        map.put("fecha", csvDatasetWriter.getStrVoidField());
        map.put("usuario", csvDatasetWriter.getStrVoidField());
        map.put("contribucionesUsuario", csvDatasetWriter.getStrVoidField());
        map.put("votosUtilesUsuario", csvDatasetWriter.getStrVoidField());
        map.put("nivelColaboracion", csvDatasetWriter.getStrVoidField());
    }

    /**
     * Transform a month to integer
     *
     * @param month a string with the month
     * @return a integer associated to the month
     */
    private int transformDate(String month) {
        HashMap<String, Integer> dates = new HashMap<>();
        dates.put("enero", 1);
        dates.put("febrero", 2);
        dates.put("marzo", 3);
        dates.put("abril", 4);
        dates.put("mayo", 5);
        dates.put("junio", 6);
        dates.put("julio", 7);
        dates.put("agosto", 8);
        dates.put("septiembre", 9);
        dates.put("octubre", 10);
        dates.put("noviembre", 11);
        dates.put("diciembre", 12);
        return dates.get(month);
    }

    /**
     * Transform a type of stay to a integer
     *
     * @param text the string of the stay
     * @return a integer associated to the string
     */
    private int transformTypeStay(String text) {
        HashMap<String, Integer> type = new HashMap<>();
        type.put("viajépornegocios", 1);
        type.put("viajéconpareja", 2);
        type.put("viajéconmifamilia", 3);
        type.put("viajéconamigos", 4);
        type.put("viajésolo", 5);
        return type.get(text.toLowerCase().replace(" ", ""));
    }

    private String cleanString(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCOMBINING_DIACRITICAL_MARKS}]", "");
        s = s.replaceAll(" ", "");
        return s;
    }

    /**
     * Starts processing all the data from hotel
     *
     * @throws IOException if something bad happens writing to CSV
     */
    public void run() throws IOException {
        Map<String, Object> defCsv = new LinkedHashMap<>();
        Map<String,Object> ubicationCsv = new LinkedHashMap<>();
        this.initializeCSV(defCsv);
        this.initializeUbicationCSV(ubicationCsv);
        //Return all pages of hotels
        AtomicInteger numPagesHotels = this.getNumPagesHotels();
        WebDriver driver;
        //Autoincrement value
        AtomicInteger idComment = new AtomicInteger();
        // List of all hotels that will be processed
        List<Integer> idHotels = new ArrayList<>();
        int num;
        while ((num = numPagesHotels.getAndDecrement()) >= 1) {
            //Split the URL to access all the hotels
            String[] urlSplitted = this.urlHotel.toString().split("-");
            String nextHotelPageString = String.format("%s-%s-oa%d-%s", urlSplitted[0], urlSplitted[1], num * 30 - 30, urlSplitted[2]);
            System.out.println(nextHotelPageString);
            //Create the url of the pages of hotels
            URL nextHotelPage = new URL(nextHotelPageString);
            //Opens a Chrome
            driver = new FirefoxDriver();
            Document docHotels;
            try {
                driver.get(nextHotelPage.toString());
                docHotels = Jsoup.parse(driver.getPageSource()).normalise();
            } finally {
                driver.quit();
            }
            //loop for all hotels
            for (Element e : docHotels.getElementsByClass("property_title")) {
                //Creates a URL of a hotel
                URL hotel = new URL(String.format("%s://%s%s", this.urlHotel.getProtocol(), this.urlHotel.getHost(), e.attr("href")));
                System.out.println(hotel.toString());
                //Split for get info
                urlSplitted = hotel.toString().split("-");
                //Id hotel
                int idHotel = Integer.parseInt(urlSplitted[2].substring(1));

                if (!idHotels.contains(idHotel)) {
                    idHotels.add(idHotel);
                    defCsv.put("idTripHotel", idHotel);
                    Document hotelDoc = null;
                    Document hotelDocTrusting = null;
                    boolean stop2;
                    do {
                        stop2 = true;
                        try {
                            driver = new FirefoxDriver();
                            driver.get(hotel.toString());
                            //Waits for JavaScript
                            WebDriverWait wait2 = new WebDriverWait(driver, 30);
                            wait2.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("rebrand_2017")));
                            wait2.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("HEADING")));
                            wait2.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("taplc_hr_community_content_0")));
                            wait2.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("react-container")));
                            wait2.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("hotels-hotel-review-layout-Section__reorg--2f8Ro")));

                            hotelDoc = Jsoup.parse(driver.getPageSource());
                            hotelDocTrusting = Jsoup.connect(hotel.toString()).get();

                        } catch (Exception exc) {
                            stop2 = false;
                        }
                        driver.quit();
                    } while (!stop2);

                    //Province of the hotel
                    String province = hotelDoc.getElementsByClass("link").text().split(" ")[5];
                    defCsv.put("nombreProvincia", province);


                    //Id of the location
                    int idLocation = Integer.parseInt(urlSplitted[1].substring(1));
                    defCsv.put("idTripLocalizacion", idLocation);
                    //Name location
                    String nameLocation = hotelDoc.getElementsByClass("ui_pill inverted").text();
                    defCsv.put("nombreLocalizacion", nameLocation);

                    //Name hotel
                    String nameHotel = hotelDoc.getElementById("HEADING").text();
                    defCsv.put("nombreHotel", nameHotel);
                    ubicationCsv.put("nombreHotel",nameHotel);

                    String street = hotelDoc.getElementsByClass("public-business-listing-ContactInfo__ui_link--1_7Zp public-business-listing-ContactInfo__level_4--3JgmI").text();
                    System.out.println(street);
                    //Ubication
                    WebDriver ubicationDriver = new FirefoxDriver();

                    URL ubicationUrl = new URL("https://www.latlong.net/");
                    ubicationDriver.get(ubicationUrl.toString());
                    WebElement input = ubicationDriver.findElement(By.id("place"));
                    input.sendKeys(street);
                    WebElement find = ubicationDriver.findElement(By.id("btnfind"));
                    find.click();
                    WebDriverWait waitUbication = new WebDriverWait(ubicationDriver,10);
                    waitUbication.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("col-7")));
                    Document ubicationDoc = Jsoup.parse(ubicationDriver.getPageSource());
                    ubicationDriver.quit();
                    Element ubiElement = ubicationDoc.getElementById("latlngspan");
                    String textUbi = ubiElement.text().replaceAll("[()]","");
                    textUbi = textUbi.replaceAll(" ","");

                    ubicationCsv.put("lat",textUbi.split(",")[0]);
                    ubicationCsv.put("lng",textUbi.split(",")[1]);


                    csvUbicationWriter.addRow(ubicationCsv.values().toArray());
                    System.out.println("UBICACION ESCRITA");
                    csvUbicationWriter.flushAndClose();

                    //Category hotel
                    Elements catElement = hotelDoc.getElementsByClass("ui_star_rating");
                    Object catHotel = csvDatasetWriter.getStrVoidField();
                    if (catElement.size() > 0) {
                        catHotel = Integer.parseInt(catElement
                                .get(0)
                                .className()
                                .split(" ")[1]
                                .split("_")[1]) / 10;
                    }
                    defCsv.put("categoriaHotel", catHotel);

                    //List of hotel services
                    List<String> hotelServices = new ArrayList<>();
                    Elements allHotelServices = hotelDoc.getElementsByClass("hotels-hr-about-amenities-Amenity__amenity--3fbBj");
                    for (Element element : allHotelServices) {
                        hotelServices.add(cleanString(element.text()));
                    }
                    this.addServicesToCsv(defCsv, hotelServices);
                    //Opinions total evaluation
                    float evaluation = 0;
                    Elements evaluationElements = hotelDoc.getElementsByClass("hotels-hotel-review-about-with-photos-Reviews__overallRating--vElGA");
                    if (evaluationElements.size() > 0) {
                        evaluation = Float.parseFloat(evaluationElements.text().substring(0, 1) + "." + evaluationElements.text().substring(2));
                    }
                    //Ubication evaluation
                    evaluationElements = hotelDoc.getElementsByClass("ui_bubble_rating");
                    float ubicationEvaluation = 0, cleanEvaluation = 0, serviceEvaluation = 0, worthEvaluation = 0;
                    if (evaluation != 0) {
                        ubicationEvaluation = Float.parseFloat(evaluationElements.get(2).className().split(" ")[1].split("_")[1]) / 10;
                        //Clean evaluation
                        if (evaluationElements.size() > 3) {
                            cleanEvaluation = Float.parseFloat(evaluationElements.get(3).className().split(" ")[1].split("_")[1]) / 10;
                            //Service evaluation
                            if (evaluationElements.size() > 4) {
                                serviceEvaluation = Float.parseFloat(evaluationElements.get(4).className().split(" ")[1].split("_")[1]) / 10;
                                defCsv.put("valoracionServicio", serviceEvaluation);
                                //worth evaluation
                                if (evaluationElements.size() > 5) {
                                    worthEvaluation = Float.parseFloat(evaluationElements.get(5).className().split(" ")[1].split("_")[1]) / 10;
                                }
                            }
                        }
                    }
                    defCsv.put("valoracionUbicacion", ubicationEvaluation);
                    defCsv.put("valoracionLimpieza", cleanEvaluation);
                    defCsv.put("valoracionServicio", serviceEvaluation);
                    defCsv.put("valoracionCalidadPrecio", worthEvaluation);

                    defCsv.put("valoracionTotal", evaluation);
                    //Pages from a hotel
                    AtomicInteger numPages = this.getNumPagesFromHotel(hotelDocTrusting);
                    Elements countOpinionsElement = hotelDoc.getElementsByClass("reviewCount ui_link");
                    if (countOpinionsElement.size() == 0 && numPages.get() == 1) {
                        this.addAfterServices(defCsv);
                        csvDatasetWriter.addRow(defCsv.values().toArray());
                        System.out.println("ESCRITOO");
                        csvDatasetWriter.flushAndClose();
                        idComment.getAndIncrement();
                    } else {
                        //Split the url to go to next pages with comment
                        urlSplitted = hotel.toString().split("Reviews");
                        while ((num = numPages.get()) >= 1) {
                            System.out.println("PAGINA: " + num);
                            //Assignation of the page
                            URL nextPageUrl = new URL(urlSplitted[0] + "Reviews-or" + (num * 5 - 5) + urlSplitted[1]);
                            //Declaration of the parser
                            boolean stop3 = true;
                            Document nextPageDoc = null;
                            do {
                                stop3 = true;
                                try {
                                    nextPageDoc = Jsoup.connect(nextPageUrl.toExternalForm()).userAgent("Mozilla/5.0").get();
                                    if (nextPageDoc.getElementsByClass("hotels-review-list-parts-SingleReview__mainCol--2XgHm").size() == 0) {
                                        throw new Exception();
                                    }
                                } catch (Exception ep) {
                                    stop3 = false;
                                }
                            } while (!stop3);
                            Elements allCommentsElements = nextPageDoc.getElementsByClass("hotels-review-list-parts-SingleReview__mainCol--2XgHm");
                            for (Element element : allCommentsElements) {
                                //Create the URL of a comment
                                URL commentPage = new URL(String.format("%s://%s%s", this.urlHotel.getProtocol(), this.urlHotel.getHost(), element.getElementsByClass("hotels-review-list-parts-ReviewTitle__reviewTitleText--3QrTy").attr("href")));
                                System.out.println(commentPage);
                                boolean stop;
                                Document commentPageDoc = null;
                                do {
                                    stop = true;
                                    try {
                                        driver = new FirefoxDriver();
                                        driver.get(commentPage.toString());
                                        WebDriverWait wait = new WebDriverWait(driver, 10);
                                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("reviewSelector")));
                                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("date_picker_modal")));
                                        Actions builder = new Actions(driver);
                                        driver.findElement(By.id("taplc_trip_planner_breadcrumbs_0")).click();
                                        WebElement webElement = driver.findElements(By.className("info_text")).get(0);

                                        builder.moveToElement(webElement).click().build().perform();
                                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("g10n")));
                                        commentPageDoc = Jsoup.parse(driver.getPageSource());
                                    } catch (Exception exception) {
                                        stop = false;
                                    }
                                    driver.quit();
                                } while (!stop);
                                //Title of a comment
                                String titleComment = commentPageDoc.getElementById("HEADING").text();
                                if (titleComment.contains(";")) {
                                    titleComment = titleComment.replaceAll(";", ",");
                                }
                                defCsv.put("tituloComentario", titleComment);

                                //Advices of a comment
                                Elements adviceElements = commentPageDoc.getElementsByClass("reviewItem inlineRoomTip");
                                String advice = csvDatasetWriter.getStrVoidField();
                                if (adviceElements.size() > 0) {
                                    advice = adviceElements.last().text().split(":")[1];
                                    advice = advice.substring(1);
                                }
                                defCsv.put("consejos", advice);

                                //Votes from a comment
                                Elements utilVotesElement = element.getElementsByClass("social-sections-SocialStatisticsBar__counts--35oyz social-sections-SocialStatisticsBar__item--3Fm5r");
                                int utilVotesComment = 0;
                                if (utilVotesElement.size() > 0) {
                                    utilVotesComment = Integer.parseInt(utilVotesElement.text().split(" ")[0]);

                                }
                                defCsv.put("votosUtilesComentario", utilVotesComment);
                                //Get date
                                Elements dateElement = commentPageDoc.getElementsByClass("prw_rup prw_reviews_stay_date_hsx");
                                String date;
                                try {

                                    date = dateElement.first().text().split(":")[1];
                                    System.out.println(dateElement.text());
                                    defCsv.put("fecha", String.format("%d/%d", transformDate(date.split(" ")[1]), Integer.parseInt(date.split(" ")[3])));
                                    System.out.println(defCsv.get("fecha"));
                                } catch (Exception exp) {
                                    System.out.println("Sin fecha");
                                    date = csvDatasetWriter.getStrVoidField();
                                    defCsv.put("fecha", date);
                                }

                                //Get rating
                                int personalEvaluation = Integer.parseInt(commentPageDoc.getElementsByClass("ui_bubble_rating").get(0).className()
                                        .split(" ")[1].split("_")[1]) / 10;
                                defCsv.put("valoracionIndividual", personalEvaluation);
                                //Stay type
                                Elements typeStayElement = commentPageDoc.getElementsByClass("recommend-titleInline");
                                Object typeStay = csvDatasetWriter.getStrVoidField();
                                if (typeStayElement.size() > 0) {
                                    typeStay = transformTypeStay(typeStayElement.last().text().split(":")[1]);
                                }
                                defCsv.put("tipoViaje", typeStay);
                                //Content of a comment
                                String commentContent = commentPageDoc.getElementsByClass("fullText").first().text();
                                if (commentContent.contains(";")) {
                                    commentContent = commentContent.replaceAll(";", ",");
                                }
                                defCsv.put("comentario", commentContent);
                                //ID comment
                                int idUrlComment = Integer.parseInt(commentPage.toString().split("-")[3].substring(1));
                                defCsv.put("idTripComentario", idUrlComment);
                                defCsv.put("idComentario", idComment.getAndIncrement());

                                //data user
                                Element userElement = commentPageDoc.getElementsByClass("prw_rup prw_reviews_member_info_resp_sur").get(0);
                                try {
                                    //Username
                                    String username = commentPageDoc.getElementsByClass("memberOverlayRedesign g10n").get(0).child(0).attr("href").split("/")[2];
                                    defCsv.put("usuario", username);
                                } catch (Exception e1) {
                                    defCsv.put("usuario", csvDatasetWriter.getStrVoidField());
                                }
                                //Votes from a user
                                int userVotes = 0;
                                if (userElement.getElementsByClass("badgetext").size() == 2) {
                                    userVotes = Integer.parseInt(userElement.getElementsByClass("badgetext").get(1).text());
                                }
                                //Contributions from a user
                                Elements userContriElements = userElement.getElementsByClass("badgetext");
                                int userContributions = 0;
                                if (userContriElements.size() > 0) {
                                    userContributions = Integer.parseInt(userContriElements.get(0).text());
                                }
                                defCsv.put("votosUtilesUsuario", userVotes);
                                defCsv.put("contribucionesUsuario", userContributions);
                                //Collaboration level
                                Elements colaborationLevelElement = commentPageDoc.getElementsByClass("badgeinfo");
                                int colabLevel = 0;
                                if (colaborationLevelElement.size() > 0) {
                                    colabLevel = Integer.parseInt(colaborationLevelElement.text().split(" ")[3]);
                                    System.out.println(colaborationLevelElement.text());
                                }
                                defCsv.put("nivelColaboracion", colabLevel);

                                csvDatasetWriter.addRow(defCsv.values().toArray());
                                System.out.println("ESCRITOO");
                                csvDatasetWriter.flushAndClose();
                            }


                            numPages.decrementAndGet();
                        }
                    }
                }
            }
        }
    }
}
