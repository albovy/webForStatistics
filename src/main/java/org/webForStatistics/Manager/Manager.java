package org.webForStatistics.Manager;



import org.bdp4j.util.CSVDatasetWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.webForStatistics.Spot.Hotel;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class Manager {
    private URL urlHotel;
    private Document docHotel;
    private Hotel hotel;
    private CSVDatasetWriter csvDatasetWriter;

    public Manager(URL url) throws IOException {
        this.urlHotel = url;
        WebDriver driver = new PhantomJSDriver();
        try {
            driver.get(url.toString());
            this.docHotel = Jsoup.parse(driver.getPageSource());
        } finally {
            driver.close();
        }

        this.csvDatasetWriter = new CSVDatasetWriter("prueba3.csv");
    }

    private AtomicInteger getNumPagesFromHotel(Document doc) {
        Elements elementsNumPages = doc.getElementsByClass("pageNum");
        return new AtomicInteger(1);//new AtomicInteger(Integer.parseInt(elementsNumPages.last().text()));
    }

    private AtomicInteger getNumPagesHotels() {
        Elements elementsNumPages = this.docHotel.getElementsByClass("pageNum");
        return new AtomicInteger(1); //new AtomicInteger(Integer.parseInt(elementsNumPages.last().text()));
    }

    private void teeNormalHotelValues(Object... values) {
        List<Object> allValues = new ArrayList<>(Arrays.asList(values));
        csvDatasetWriter.addRow(allValues.toArray());
        csvDatasetWriter.flushAndClose();
    }

    public void run() throws IOException {
        /*System.out.println("cargando");
        CSVDatasetReader csvReader = new CSVDatasetReader("postalcat.csv");
        Dataset set = csvReader.loadFile();*/

        HashMap<String, Object> defCsv = new LinkedHashMap<>();
        defCsv.put("idComment", 0);
        defCsv.put("#Location", 0);
        defCsv.put("nameLocation", 0);
        defCsv.put("nameProvince", 0);
        defCsv.put("#Hotel", 0);
        defCsv.put("nameHotel", 0);
        defCsv.put("catHotel", 0);
        defCsv.put("totalValoration", 0);
        defCsv.put("individualValoration", 0);
        defCsv.put("#comment", 0);
        defCsv.put("stayType", 0);
        defCsv.put("comment", 0);
        defCsv.put("consejos","");
        defCsv.put("date", 0);

        String[] columns = defCsv.keySet().toArray(new String[0]);
        /*List<String> l2 = new ArrayList<>(defCsv.keySet());
        for (int i= 5; i<defCsv.size()-2;i++){
            System.out.println(l2.get(i));
        }
        String[] columns = defCsv.keySet().toArray(new String[0]);
        for (String s: defCsv.keySet()){
            System.out.println(s);
        }*/
        csvDatasetWriter.addColumns(columns, defCsv.values().toArray());


        //Split the URL for go to all pages
        String[] urlSplitted = this.urlHotel.toString().split("-");
        //Num pages hotels
        AtomicInteger numPagesHotels = this.getNumPagesHotels();
        int n;
        String nextHotelPageString;
        URL nextHotelPage;
        Document docHotels;

        WebDriver driver;
        //ID autoincremental of the comment
        AtomicInteger idComment = new AtomicInteger();
        while ((n = numPagesHotels.getAndDecrement()) >= 1) {
            nextHotelPageString = urlSplitted[0] + "-" + urlSplitted[1] + "-oa" + (n * 30 - 30) + "-" + urlSplitted[2];
            System.out.println(nextHotelPageString);
            nextHotelPage = new URL(nextHotelPageString);

            driver = new PhantomJSDriver();
            try {
                driver.get(nextHotelPage.toString());
                docHotels = Jsoup.parse(driver.getPageSource());
            } finally {
                driver.close();
            }
            URL hotel;
            Document hotelDoc;
            for (Element e : docHotels.getElementsByClass("property_title")) {
                hotel = new URL(String.format("%s://%s%s", this.urlHotel.getProtocol(), this.urlHotel.getHost(), e.attr("href")));
                System.out.println(hotel.toString());

                hotelDoc = Jsoup.connect(hotel.toExternalForm()).userAgent("Chrome").timeout(100000).get();
                String province = hotelDoc.getElementsByClass("link").text().split(" ")[5];
                defCsv.put("nameProvince", province);

                //Split for get info
                urlSplitted = hotel.toString().split("-");
                //Id of the location
                Integer idLocation = Integer.parseInt(urlSplitted[1].substring(1));
                defCsv.put("#Location", idLocation);
                //Name location
                String nameLocation = hotelDoc.getElementsByClass("ui_pill inverted").text();
                defCsv.put("nameLocation", nameLocation);
                ///////////////
                //Id hotel
                Integer idHotel = Integer.parseInt(urlSplitted[2].substring(1));
                defCsv.put("#Hotel", idHotel);
                //Name hotel
                String nameHotel = hotelDoc.getElementsByClass("ui_header h1").text();
                defCsv.put("nameHotel", nameHotel);
                //Category hotel
                Elements catElement = hotelDoc.getElementsByClass("ui_star_rating");
                Integer catHotel = 0;
                if (catElement.size() > 0) {
                     catHotel = Integer.parseInt(catElement
                            .get(0)
                            .className()
                            .split(" ")[1]
                            .split("_")[1]) / 10;
                }
                defCsv.put("catHotel", catHotel);
                //List of hotel services
                System.out.println("hola?");
                List<String> hotelServices = new ArrayList<>();
                Elements allHotelServices = hotelDoc.getElementsByClass("hotels-hr-about-amenities-Amenity__name--3MfNu");
                for (Element element : allHotelServices) {
                    hotelServices.add(element.text());
                    System.out.println(element.text());
                }

                defCsv.remove("totalValoration");
                defCsv.remove("individualValoration");
                defCsv.remove("#comment");
                defCsv.remove("stayType");
                defCsv.remove("comment");
                defCsv.remove("consejos");
                defCsv.remove("date");
                List<String> list = new ArrayList<>(defCsv.keySet());
                String key;
                for (int i = 7; i < list.size(); i++) {
                    key = list.get(i);
                    if (hotelServices.contains(key)) {
                        defCsv.put(key, 1);
                        hotelServices.remove(key);
                    } else {
                        defCsv.put(key, 0);
                    }
                }
                for (String str : hotelServices) {
                    csvDatasetWriter.insertColumnAt(str, 0, csvDatasetWriter.getColumnCount() - 7);
                    defCsv.put(str, 1);
                }
                defCsv.put("totalValoration", 0);
                defCsv.put("individualValoration", 0);
                defCsv.put("#comment", 0);
                defCsv.put("stayType", 0);
                defCsv.put("comment", 0);
                defCsv.put("consejos","");
                defCsv.put("date", 0);
                System.out.println("llegoooooooooooooooooooooooooooooooo");
                //Opinions total evaluation
                float evaluation = 0;
                Elements ele = hotelDoc.getElementsByClass("hotels-hotel-review-about-with-photos-Reviews__overallRating--vElGA");
                if (ele.size() > 0) {
                    evaluation = Float.parseFloat(ele.text().substring(0, 1) + "." + ele.text().substring(2));
                    System.out.println(evaluation+" valoración");
                }
                defCsv.put("totalValoration", evaluation);
                //    evaluation = Integer.parseInt(hotelDoc.getElementsByClass("hotels-hotel-review-about-with-photos-Reviews__overallRating--vElGA").text().substring(0, 1));

                AtomicInteger numPages = this.getNumPagesFromHotel(hotelDoc);
                //Split the url to go to next pages with comment
                urlSplitted = hotel.toString().split("Reviews");
                //Creating the URL
                URL nextPageUrl;
                //Need a document for parse the new page
                Document nextPageDoc;
                /*System.out.println(idLocation);
                System.out.println(nameLocation);
                System.out.println(idHotel);
                System.out.println(nameHotel);
                System.out.println(catHotel);
                System.out.println(evaluation);*/

                //Url of a comment
                URL commentPage;
                //Parser of a Comment Page
                Document commentPageDoc;
                //Date of the stay
                String date;

                //Personal evaluation
                Integer personalEvaluation;
                //Type of stay
                String typeStay;
                //Content of the comment
                String commentContent;
                //ID in the URL comment
                Integer idUrlComment;
                while ((n = numPages.getAndDecrement()) >= 1) {
                    //Assignation of the page

                    nextPageUrl = new URL(urlSplitted[0] + "Reviews-or" + (n * 5 - 5) + urlSplitted[1]);
                    //Declaration of the parser
                    nextPageDoc = Jsoup.connect(nextPageUrl.toExternalForm()).userAgent("Chrome").timeout(100000).get();
                    for (Element element : nextPageDoc.getElementsByClass("hotels-review-list-parts-SingleReview__mainCol--2XgHm")) {

                        //Create the URL of a comment
                        commentPage = new URL(String.format("%s://%s%s", this.urlHotel.getProtocol(), this.urlHotel.getHost(), element.getElementsByClass("hotels-review-list-parts-ReviewTitle__reviewTitleText--3QrTy").attr("href")));
                        System.out.println(commentPage);
                        commentPageDoc = Jsoup.connect(commentPage.toExternalForm()).userAgent("Chrome").timeout(100000).get();
                        System.out.println(commentPageDoc.getElementById("HEADING").text());
                        /*driver.get(commentPage.toString());
                        //Document of the Comment Page
                        //driver = new PhantomJSDriver();
                        commentPageDoc = Jsoup.parse(driver.getPageSource());
                        driver.close();*/

                        Elements consejos = commentPageDoc.getElementsByClass("reviewItem inlineRoomTip");
                        String consejosS = "";
                        if (consejos.size() >0){
                            System.out.println("TIENEEEEEE");
                            consejosS = consejos.last().text().split(":")[1];
                            System.out.println(consejosS);
                        }
                        defCsv.put("consejos",consejosS);

                        Elements cont = element.getElementsByClass("social-sections-SocialStatisticsBar__counts--35oyz social-sections-SocialStatisticsBar__item--3Fm5r");
                        if(cont.size()>0) {

                            System.out.println(cont.text());

                        }else{
                            System.out.println("NO TIENE VOTOS");
                        }

                        //Get date
                        date = commentPageDoc.getElementsByClass("prw_rup prw_reviews_stay_date_hsx").last().text().split(":")[1];
                        defCsv.put("date", date);
                        //Get rating
                        personalEvaluation = Integer.parseInt(commentPageDoc.getElementsByClass("ui_bubble_rating").get(0).className()
                                .split(" ")[1].split("_")[1]) / 10;
                        defCsv.put("individualValoration", personalEvaluation);
                        //System.out.println(personalEvaluation);
                        typeStay = commentPageDoc.getElementsByClass("recommend-titleInline").last().text().split(":")[1];
                        defCsv.put("stayType", typeStay);
                        //System.out.println(typeStay);
                        commentContent = commentPageDoc.getElementsByClass("fullText").first().text();
                        defCsv.put("comment", commentContent);
                        //System.out.println(commentContent);
                        //ID comment
                        idUrlComment = Integer.parseInt(commentPage.toString().split("-")[3].substring(1));
                        defCsv.put("#comment", idUrlComment);
                        //System.out.println(idUrlComment);

                        defCsv.put("idComment", idComment.getAndIncrement());
                        csvDatasetWriter.addRow(defCsv.values().toArray());
                        System.out.println("ESCRITOO");
                        csvDatasetWriter.flushAndClose();


                    }

                }
            }
        }
    }
}
