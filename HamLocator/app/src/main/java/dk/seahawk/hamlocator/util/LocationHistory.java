package dk.seahawk.hamlocator.util;

import java.util.ArrayList;
import java.util.List;

public class LocationHistory {
    // https://stackoverflow.com/questions/18393600/android-singleton-becomes-null

    //TODO Read/Write object to memory, Permissions
    // Settings stored via object / userinfo in shared preferences (Settings menu)
    public static List<PlaceholderItem> items = new ArrayList<>();

    // Create instance
    private static LocationHistory instance = new LocationHistory();

    // Protect class from being instantiated
    private LocationHistory() {}

    // Return singleton instance of LocationHistory
    public static LocationHistory getInstance(){
        return instance;
    }

    /**
     * Add elements to list
     * @param item
     */
    public void addItemToList(PlaceholderItem item) {
        items.add(item);
    }

    public void addHistoryList(List<PlaceholderItem> placeholderItemList) {
        items = placeholderItemList;
    }

    public List<PlaceholderItem> getHistoryList() {
        return items;
    }

    public static class PlaceholderItem {
        public final String jidId, locTime, utcTime, lat, lon, alt;

        public PlaceholderItem(String jidId, String locTime, String utcTime, String latitude, String longitude, String altitude) {
            this.jidId = jidId;
            this.locTime = locTime;
            this.utcTime = utcTime;
            this.lat = latitude;
            this.lon = longitude;
            this.alt = altitude;
        }

        public String getJidId() {
            return jidId;
        }

        public String getLocTime() {
            return locTime;
        }

        public String getUtcTime() {
            return utcTime;
        }

        public String getLat() {
            return lat;
        }

        public String getLon() {
            return lon;
        }

        public String getAlt() {
            return alt;
        }

    }

}