package org.webForStatistics;

import org.webForStatistics.Manager.Manager;
import org.webForStatistics.Manager.ManagerBooking;

import java.io.IOException;
import java.net.URL;

public class Main2 {
    public static void main(String[] args) throws IOException {
        System.setProperty("webdriver.gecko.driver","src/main/resources/geckodriver.exe");
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        System.setProperty("phantomjs.binary.path", "src/main/resources/phantomjs/bin/phantomjs.exe");
        final URL url2 = new URL("https://www.booking.com/searchresults.es.html?aid=397594&label=gog235jc-1DCAEoggI46AdIClgDaEaIAQGYAQq4ARfIAQzYAQPoAQH4AQKIAgGoAgO4AqKnuusFwAIB&sid=029c99cb2c65f0650b7f773436fe832d&tmpl=searchresults&ac_click_type=b&ac_position=1&class_interval=1&clear_ht_id=1&dest_id=770&dest_type=region&from_sf=1&group_adults=2&group_children=0&label_click=undef&no_rooms=1&raw_dest_type=region&room1=A%2CA&sb_price_type=total&search_selected=1&shw_aparth=1&slp_r_match=0&src=index&srpvid=ce58781b99740143&ss=Ourense%2C%20España&ss_raw=ourense&ssb=empty");
        ManagerBooking man = new ManagerBooking(url2);
        man.run();
    }
}
