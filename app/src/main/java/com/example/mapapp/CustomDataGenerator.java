package com.example.mapapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomDataGenerator {

    public static final List<CustomItem> ITEMS = new ArrayList<CustomItem>();
    public static final Map<String, CustomItem> ITEM_MAP = new HashMap<String, CustomItem>();

    public static void generateItem(CustomItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static class CustomItem {
        public final String id;
        public final String title;
        public final String info;
        public final String content;
        public final boolean isEnabled;

        public CustomItem(String id, String title, String info, String content, boolean isEnabled) {
            this.id = id;
            this.title = title;
            this.info = info;
            this.content = content;
            this.isEnabled = isEnabled;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
