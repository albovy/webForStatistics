package org.webForStatistics.Spot;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Hotel {
    private Integer idLocation;
    private String nameLocation;
    //duda
    private String nameProvince;
    private Integer idHotel;
    private String nameHotel;
    private Integer catHotel;
    private List<String> hotelServices;
    private List<String> roomServices;
    private Integer evaluation;
    private AtomicInteger numComments;
    private List<Comment> comments;

}
