package com.jug.export;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexParser {
    private final String regex;
    private String match;
    public RegexParser(String regex) {
        this.regex = regex;
    }

    public void parse(String filename) {
        Matcher matcher = Pattern.compile(this.regex).matcher(filename);
        matcher.find();
        matcher.group();
        match = matcher.group(1); // group(0) is the whole match; group(1) is just the number, which is what we want
    }

    String getMatch() {
        return match;
    }
}
