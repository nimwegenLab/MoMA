package com.jug.export;

import org.apache.commons.lang.NotImplementedException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageFileNameParser {
    String positionId;
    String glId;

    public void parseFileName(String filename) {
        Pattern positionPattern = Pattern.compile("([-1-9]*Pos\\d+)_(GL[0-9]*)\\.tif");
        Matcher positionMatcher = positionPattern.matcher(filename);
        positionMatcher.find();
        positionMatcher.group();
        positionId = positionMatcher.group(1); // group(0) is the whole match; group(1) is just the number, which is what we want
        glId = positionMatcher.group(2); // group(0) is the whole match; group(1) is just the number, which is what we want
    }

    String getPositionId() {
        return positionId;
    }


    String getGlId() {
        return glId;
    }
}
