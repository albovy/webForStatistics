package org.webForStatistics;

import org.webForStatistics.Manager.Manager;


import java.io.IOException;
import java.net.URL;

public class Main {
    public static void main(String[] args) throws IOException {
        System.setProperty("webdriver.gecko.driver","src/main/resources/geckodriver");
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        System.setProperty("phantomjs.binary.path", "src/main/resources/phantomjs/bin/phantomjs.exe");
        final URL url2 = new URL("https://www.tripadvisor.es/Hotels-g1768741-Province_of_Ourense_Galicia-Hotels.html");
        Manager man = new Manager(url2);
        man.run();
    }
}
