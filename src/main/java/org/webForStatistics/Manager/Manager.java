package org.webForStatistics.Manager;

import org.bdp4j.util.CSVDatasetWriter;
import org.bdp4j.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


public class Manager {
    private URL urlHotel;
    private Document docHotel;
    private CSVDatasetWriter csvDatasetWriter;
    private final int COLUMNS_AFTER_SERVICES = 17;

    public Manager(URL url) {
        this.urlHotel = url;
        WebDriver driver = new FirefoxDriver();
        try {
            driver.get(url.toString());
            this.docHotel = Jsoup.parse(driver.getPageSource()).normalise();
        } finally {
            driver.quit();
        }
        this.csvDatasetWriter = new CSVDatasetWriter("prueba4.csv");
    }

    private AtomicInteger getNumPagesFromHotel(Document doc) {
        Elements elementsNumPages = doc.getElementsByClass("pageNum");
        if (elementsNumPages.size() > 0) {
            return new AtomicInteger(Integer.parseInt(elementsNumPages.last().text()));
        } else return new AtomicInteger(1);
    }

    private AtomicInteger getNumPagesHotels() {
        Elements elementsNumPages = this.docHotel.getElementsByClass("pageNum");
        return new AtomicInteger(3); //new AtomicInteger(Integer.parseInt(elementsNumPages.last().text()));
    }

    private void initializeCST(Map<String, Object> map) {
        map.put("idComentario", 0);
        map.put("#Localizacion", 0);
        map.put("nombreLocalizacion", 0);
        map.put("nombreProvincia", 0);
        map.put("#Hotel", 0);
        map.put("nombreHotel", 0);
        map.put("categoriaHotel", 0);
        insertDefaultAfterServicesValues(map);


        String[] columns = map.keySet().toArray(new String[0]);
        csvDatasetWriter.addColumns(columns, map.values().toArray());
    }

    private void addServicesToCsv(Map<String, Object> map, List<String> services) {
        map.remove("valoracionTotal");
        map.remove("valoracionUbicacion");
        map.remove("valoracionLimpieza");
        map.remove("valoracionServicio");
        map.remove("valoracionCalidadPrecio");
        map.remove("valoracionIndividual");
        map.remove("#Comentario");
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
        insertDefaultAfterServicesValues(map);
    }

    private void insertDefaultAfterServicesValues(Map<String, Object> map) {
        map.put("valoracionTotal", 0);
        map.put("valoracionUbicacion", 0);
        map.put("valoracionLimpieza", 0);
        map.put("valoracionServicio", 0);
        map.put("valoracionCalidadPrecio", 0);
        map.put("valoracionIndividual", 0);
        map.put("#Comentario", 0);
        map.put("tituloComentario", "");
        map.put("tipoViaje", 0);
        map.put("comentario", 0);
        map.put("consejos", "");
        map.put("votosUtilesComentario", 0);
        map.put("fecha", 0);
        map.put("usuario", 0);
        map.put("contribucionesUsuario", 0);
        map.put("votosUtilesUsuario", 0);
        map.put("nivelColaboracion", 0);
    }

    private int transformDate(String mes) {
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
        return dates.get(mes);
    }

    private int transformTypeStay(String text) {
        HashMap<String, Integer> type = new HashMap<>();
        type.put("viajépornegocios", 1);
        type.put("viajéconpareja", 2);
        type.put("viajéconmifamilia", 3);
        type.put("viajéconamigos", 4);
        type.put("viajésolo", 5);
        return type.get(text.toLowerCase().replace(" ", ""));
    }


    public void run() throws IOException {
        HashMap<String, Object> defCsv = new LinkedHashMap<>();
        this.initializeCST(defCsv);
        AtomicInteger idComment = new AtomicInteger();
        AtomicInteger numPagesHotels = this.getNumPagesHotels();
        List<Integer> idHotels = new ArrayList<>();
        int num;
        while ((num = numPagesHotels.getAndDecrement()) >= 1) {
            String[] urlSplitted = this.urlHotel.toString().split("-");
            String nextHotelPageString = String.format("%s-%s-oa%d-%s", urlSplitted[0], urlSplitted[1], num * 30 - 30, urlSplitted[2]);
            System.out.println(nextHotelPageString);
            URL nextHotelPage = new URL(nextHotelPageString);

            WebDriver driver = new FirefoxDriver();
            Document docHotels;
            try {
                driver.get(nextHotelPage.toString());
                docHotels = Jsoup.parse(driver.getPageSource()).normalise();
            } finally {
                driver.quit();
            }
            for (Element e : docHotels.getElementsByClass("property_title")) {
                URL hotel = new URL(String.format("%s://%s%s", this.urlHotel.getProtocol(), this.urlHotel.getHost(), e.attr("href")));
                System.out.println(hotel.toString());
                //Split for get info
                urlSplitted = hotel.toString().split("-");
                //Id hotel
                int idHotel = Integer.parseInt(urlSplitted[2].substring(1));

                if (!idHotels.contains(idHotel)) {
                    idHotels.add(idHotel);
                    defCsv.put("#Hotel", idHotel);
                    Document hotelDoc = null;
                    boolean stop2;
                    do {
                        stop2 = true;
                        try {
                            driver = new FirefoxDriver();
                            driver.get(hotel.toString());
                            WebDriverWait wait2 = new WebDriverWait(driver, 25);
                            wait2.until(ExpectedConditions.visibilityOfElementLocated(By.className("rebrand_2017")));
                            wait2.until(ExpectedConditions.visibilityOfElementLocated(By.id("HEADING")));
                            wait2.until(ExpectedConditions.visibilityOfElementLocated(By.className("hotels-review-list-parts-SingleReview__reviewContainer--d54T4")));
                            hotelDoc = Jsoup.parse(driver.getPageSource());
                        } catch (Exception exc) {
                            stop2 = false;
                        }
                        driver.quit();
                    } while (!stop2);

                    String province = hotelDoc.getElementsByClass("link").text().split(" ")[5];
                    defCsv.put("nombreProvincia", province);


                    //Id of the location
                    int idLocation = Integer.parseInt(urlSplitted[1].substring(1));
                    defCsv.put("#Localizacion", idLocation);
                    //Name location
                    String nameLocation = hotelDoc.getElementsByClass("ui_pill inverted").text();
                    defCsv.put("nombreLocalizacion", nameLocation);
                    ///////////////

                    //Name hotel
                    String nameHotel = hotelDoc.getElementById("HEADING").text();
                    defCsv.put("nombreHotel", nameHotel);
                    //Category hotel
                    Elements catElement = hotelDoc.getElementsByClass("ui_star_rating");
                    int catHotel = 0;
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
                        hotelServices.add(element.text());
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
                    float ubicationEvaluation = Float.parseFloat(evaluationElements.get(2).className().split(" ")[1].split("_")[1]) / 10;
                    defCsv.put("valoracionUbicacion", ubicationEvaluation);
                    //Clean evaluation
                    float cleanEvaluation = Float.parseFloat(evaluationElements.get(3).className().split(" ")[1].split("_")[1]) / 10;
                    defCsv.put("valoracionLimpieza", cleanEvaluation);
                    //Service evaluation
                    float serviceEvaluation = Float.parseFloat(evaluationElements.get(4).className().split(" ")[1].split("_")[1]) / 10;
                    defCsv.put("valoracionServicio", serviceEvaluation);
                    //worth evaluation
                    float worthEvaluation = Float.parseFloat(evaluationElements.get(5).className().split(" ")[1].split("_")[1]) / 10;
                    defCsv.put("valoracionCalidadPrecio", worthEvaluation);

                    defCsv.put("valoracionTotal", evaluation);
                    AtomicInteger numPages = this.getNumPagesFromHotel(hotelDoc);
                    //Split the url to go to next pages with comment
                    urlSplitted = hotel.toString().split("Reviews");
                    while ((num = numPages.get()) >= 1) {
                        //Assignation of the page
                        URL nextPageUrl = new URL(urlSplitted[0] + "Reviews-or" + (num * 5 - 5) + urlSplitted[1]);
                        //Declaration of the parser
                        Document nextPageDoc = Jsoup.connect(nextPageUrl.toExternalForm()).userAgent("Mozilla/5.0").get().normalise();

                        Elements elements = nextPageDoc.getElementsByClass("hotels-review-list-parts-SingleReview__mainCol--2XgHm");
                        System.out.println(elements.size());
                        if(elements.size() == 0 && num == 1){
                            this.insertDefaultAfterServicesValues(defCsv);
                            addRowCSV(defCsv.values().toArray());
                        }else {
                            elements.stream().parallel().forEach((Element aux) -> {
                                //Create the URL of a comment
                                URL commentPage = null;
                                try {
                                    commentPage = new URL(String.format("%s://%s%s", this.urlHotel.getProtocol(), this.urlHotel.getHost(), aux.getElementsByClass("hotels-review-list-parts-ReviewTitle__reviewTitleText--3QrTy").attr("href")));
                                } catch (MalformedURLException e1) {
                                    e1.printStackTrace();
                                }
                                System.out.println(commentPage);
                                boolean stop;
                                WebDriver driver1;
                                Document commentPageDoc = null;
                                do {
                                    stop = true;
                                    //commentPageDoc = Jsoup.connect(commentPage.toExternalForm()).userAgent("Mozilla/5.0").get();
                                    driver1 = new FirefoxDriver();
                                    try {
                                        driver1.get(commentPage.toString());
                                        WebDriverWait wait = new WebDriverWait(driver1, 10);
                                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("reviewSelector")));
                                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("date_picker_modal")));
                                        Actions builder = new Actions(driver1);
                                        driver1.findElement(By.id("taplc_trip_planner_breadcrumbs_0")).click();
                                        WebElement webElement = driver1.findElements(By.className("info_text")).get(0);

                                        builder.moveToElement(webElement).click().build().perform();
                                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("g10n")));
                                        commentPageDoc = Jsoup.parse(driver1.getPageSource());
                                    } catch (Exception exp) {
                                        stop = false;
                                    }
                                    driver1.quit();
                                } while (!stop);

                                String titleComment = commentPageDoc.getElementById("HEADING").text();
                                defCsv.put("tituloComentario", titleComment);

                                Elements adviceElements = commentPageDoc.getElementsByClass("reviewItem inlineRoomTip");
                                String advice = csvDatasetWriter.getStrVoidField();
                                if (adviceElements.size() > 0) {
                                    advice = adviceElements.last().text().split(":")[1];
                                }
                                defCsv.put("consejos", advice);

                                Elements utilVotesElement = aux.getElementsByClass("social-sections-SocialStatisticsBar__counts--35oyz social-sections-SocialStatisticsBar__item--3Fm5r");
                                int utilVotesComment = 0;
                                if (utilVotesElement.size() > 0) {
                                    utilVotesComment = Integer.parseInt(utilVotesElement.text().split(" ")[0]);

                                }
                                defCsv.put("votosUtilesComentario", utilVotesComment);
                                //Get date
                                String date = commentPageDoc.getElementsByClass("prw_rup prw_reviews_stay_date_hsx").first().text().split(":")[1];
                                defCsv.put("fecha", String.format("%d/%d", transformDate(date.split(" ")[1]), Integer.parseInt(date.split(" ")[3])));
                                //Get rating
                                int personalEvaluation = Integer.parseInt(commentPageDoc.getElementsByClass("ui_bubble_rating").get(0).className()
                                        .split(" ")[1].split("_")[1]) / 10;
                                defCsv.put("valoracionIndividual", personalEvaluation);
                                int typeStay = transformTypeStay(commentPageDoc.getElementsByClass("recommend-titleInline").last().text().split(":")[1]);
                                defCsv.put("tipoViaje", typeStay);
                                String commentContent = commentPageDoc.getElementsByClass("fullText").first().text();
                                defCsv.put("comentario", commentContent);
                                //ID comment
                                int idUrlComment = Integer.parseInt(commentPage.toString().split("-")[3].substring(1));
                                defCsv.put("#Comentario", idUrlComment);
                                defCsv.put("idComentario", idComment.getAndIncrement());
                                Element userElement = commentPageDoc.getElementsByClass("prw_rup prw_reviews_member_info_resp_sur").get(0);
                                String username = commentPageDoc.getElementsByClass("memberOverlayRedesign g10n").get(0).child(0).attr("href").split("/")[2];
                                defCsv.put("usuario", username);
                                int userVotes = 0;
                                if (userElement.getElementsByClass("badgetext").size() == 2) {
                                    userVotes = Integer.parseInt(userElement.getElementsByClass("badgetext").get(1).text());

                                }
                                int userContributions = Integer.parseInt(userElement.getElementsByClass("badgetext").get(0).text());
                                defCsv.put("votosUtilesUsuario", userVotes);
                                defCsv.put("contribucionesUsuario", userContributions);
                                Elements colaborationLevelElement = commentPageDoc.getElementsByClass("badgeinfo");
                                int colabLevel = 0;
                                if (colaborationLevelElement.size() > 0) {
                                    colabLevel = Integer.parseInt(colaborationLevelElement.text().split(" ")[3]);
                                }
                                defCsv.put("nivelColaboracion", colabLevel);
                                addRowCSV(defCsv.values().toArray());
                            });
                        }
                        numPages.decrementAndGet();
                    }
                }
            }
        }
    }

    synchronized private void addRowCSV(Object[] objects) {
        csvDatasetWriter.addRow(objects);
        System.out.println("ESCRITOO");
        csvDatasetWriter.flushAndClose();

    }
}
