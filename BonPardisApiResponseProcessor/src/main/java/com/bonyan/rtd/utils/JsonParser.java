package com.bonyan.rtd.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonParser {

    public static List<String> getJsonValues(String json) {
        json = json.trim().replaceAll("[\\[\\]{\"}\\s]", "");
        json = json.trim().replaceAll("smsIdList:", "");
        return new ArrayList<>(Arrays.asList(json.split(",")));
    }
}
