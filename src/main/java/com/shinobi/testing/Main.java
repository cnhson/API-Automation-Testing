package com.shinobi.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        // Character CHAR = (char) 65;
        // System.out.println("\nHere: " + CHAR);
        String input = "api.test.main.IndependentTest";
        String[] parts = input.split("\\.");
        String lastElement = parts[parts.length - 1];
        System.out.println(lastElement);
        // co ly thuyet, nhung ko co thuc hanh
    }
}
