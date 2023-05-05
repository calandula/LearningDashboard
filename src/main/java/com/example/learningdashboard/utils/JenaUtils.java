package com.example.learningdashboard.utils;

import java.util.UUID;

public class JenaUtils {
    public static String parseId(String id) {
        UUID uuid = UUID.fromString(id.substring(id.lastIndexOf("#") + 1));
        return uuid.toString();
    }
}
