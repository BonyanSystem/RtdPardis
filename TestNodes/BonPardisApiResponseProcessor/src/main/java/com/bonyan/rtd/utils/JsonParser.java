package com.bonyan.rtd.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonParser {

    public static List<String> getJsonValues(String json) {
        json = json.trim().replaceAll("[{\"}\\s]", "");
        return new ArrayList<>(Arrays.asList(json.split(",")));
    }
}
