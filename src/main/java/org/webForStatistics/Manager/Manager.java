package org.webForStatistics.Manager;


import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
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
import java.util.stream.Collectors;

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
        HashMap<String,Object> defCsv = new LinkedHashMap<>();
        defCsv.put("idComment",0);
        defCsv.put("#Location",0);
        defCsv.put("nameLocation",0);
        defCsv.put("nameProvince",0);
        defCsv.put("#Hotel",0);
        defCsv.put("nameHotel",0);
        defCsv.put("catHotel",0);
        defCsv.put("totalValoration",0);
        defCsv.put("individualValoration",0);
        defCsv.put("#comment",0);
        defCsv.put("commentType",0);
        defCsv.put("comment",0);
        defCsv.put("date",0);
        String[] columns = defCsv.keySet().toArray(new String[0]);
        /*List<String> l2 = new ArrayList<>(defCsv.keySet());
        for (int i= 5; i<defCsv.size()-2;i++){
            System.out.println(l2.get(i));
        }
        String[] columns = defCsv.keySet().toArray(new String[0]);
        for (String s: defCsv.keySet()){
            System.out.println(s);
        }*/
        csvDatasetWriter.addColumns(columns,defCsv.values().toArray());


        //Split the URL for go to all pages
        String[] urlSplitted = this.urlHotel.toString().split("-");
        //Num pages hotels
        AtomicInteger numPagesHotels = this.getNumPagesHotels();
        int n;
        String nextHotelPageString;
        URL nextHotelPage;
        Document docHotels;

        WebDriver driver;
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
                hotelDoc = Jsoup.connect(hotel.toExternalForm()).userAgent("Mozilla/5.0").timeout(100000).get();



                //ID autoincremental of the comment
                AtomicInteger idComment = new AtomicInteger();


                //Split for get info
                urlSplitted = hotel.toString().split("-");
                //Id of the location
                Integer idLocation = Integer.parseInt(urlSplitted[1].substring(1));
                //Name location
                String nameLocation = hotelDoc.getElementsByClass("ui_pill inverted").text();
                ///////////////
                //Id hotel
                Integer idHotel = Integer.parseInt(urlSplitted[2].substring(1));
                //Name hotel
                String nameHotel = hotelDoc.getElementsByClass("ui_header h1").text();
                //Category hotel
                Integer catHotel = Integer.parseInt(hotelDoc.getElementsByClass("ui_star_rating")
                        .get(0)
                        .className()
                        .split(" ")[1]
                        .split("_")[1]) / 10;
                //List of hotel services
                List<String> hotelServices = new ArrayList<>();
                Elements allHotelServices = hotelDoc.getElementsByClass("hotels-hr-about-amenities-subset-Amenity__amenity--34gMU");
                for (Element element : allHotelServices) {
                    System.out.println(element.text());
                    hotelServices.add(element.text());
                }

                defCsv.remove("totalValoration");
                defCsv.remove("individualValoration");
                defCsv.remove("#comment");
                defCsv.remove("commentType");
                defCsv.remove("remove");
                defCsv.remove("date");
                List<String> list = new ArrayList<>(defCsv.keySet());
                String key;
                for (int i = 7 ; i<list.size();i++){
                     key = list.get(i);
                    if (hotelServices.contains(key)){
                        defCsv.put(key,1);
                        hotelServices.remove(key);
                    }
                }
                for (String str: hotelServices){
                    csvDatasetWriter.insertColumnAt(str,0,csvDatasetWriter.getColumnCount()-6);
                    defCsv.put(str,1);
                }
                defCsv.put("totalValoration",0);
                defCsv.put("individualValoration",0);
                defCsv.put("#comment",0);
                defCsv.put("commentType",0);
                defCsv.put("remove",0);
                defCsv.put("date",0);
                System.out.println("llegoooooooooooooooooooooooooooooooo");
                //Opinions total evaluation
                Integer evaluation = 0;
                Elements ele = hotelDoc.getElementsByClass("hotels-hotel-review-about-with-photos-Reviews__overallRating--vElGA");
                if(ele.size() > 0){
                    evaluation = Integer.parseInt(hotelDoc.getElementsByClass("hotels-hotel-review-about-with-photos-Reviews__overallRating--vElGA").text().substring(0, 1));
                }
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
                    nextPageDoc = Jsoup.connect(nextPageUrl.toExternalForm()).userAgent("Mozilla/5.0").timeout(100000).get();
                    for (Element element : nextPageDoc.getElementsByClass("hotels-review-list-parts-ReviewTitle__reviewTitleText--3QrTy")) {

                        //Create the URL of a comment
                        commentPage = new URL(String.format("%s://%s%s", this.urlHotel.getProtocol(), this.urlHotel.getHost(), element.attr("href")));
                        System.out.println(commentPage);
                        commentPageDoc = Jsoup.connect(commentPage.toExternalForm()).userAgent("Mozilla/5.0").timeout(100000).get();
                        /*driver.get(commentPage.toString());

                        //Document of the Comment Page
                        //driver = new PhantomJSDriver();
                        commentPageDoc = Jsoup.parse(driver.getPageSource());
                        driver.close();*/
                        //Get date
                        date = commentPageDoc.getElementsByClass("prw_rup prw_reviews_stay_date_hsx").last().text().split(":")[1];
                        //Get rating
                        personalEvaluation = Integer.parseInt(commentPageDoc.getElementsByClass("ui_bubble_rating").get(0).className()
                                .split(" ")[1].split("_")[1]) / 10;
                        //System.out.println(personalEvaluation);
                        typeStay = commentPageDoc.getElementsByClass("recommend-titleInline").last().text().split(":")[1];
                        System.out.println(typeStay);
                        commentContent = commentPageDoc.getElementsByClass("fullText").first().text();
                        System.out.println(commentContent);
                        //ID comment
                        idUrlComment = Integer.parseInt(commentPage.toString().split("-")[3].substring(1));
                        System.out.println(idUrlComment);



                    }

                }
            }
        }
    }
}
