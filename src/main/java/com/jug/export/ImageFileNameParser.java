package com.jug.export;

import org.apache.commons.lang.NotImplementedException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageFileNameParser {
    public void parseFileName(String filename) {
//        Pattern positionPattern = Pattern.compile("([-,1-9]*Pos\\d+)_GL[0-9]*.tif");
        Pattern positionPattern = Pattern.compile("([-1-9]*Pos\\d+)_(GL[0-9]*)\\.tif");
//        imagePath = Paths.get(configurationManager.getInputImagePath());
        Matcher positionMatcher = positionPattern.matcher(filename);
        positionMatcher.find();
        positionMatcher.group();
        String positionId = positionMatcher.group(0); // group(0) is the whole match; group(1) is just the number, which is what we want
        String GlId = positionMatcher.group(1); // group(0) is the whole match; group(1) is just the number, which is what we want
        String positionNumber = positionMatcher.group(1); // group(0) is the whole match; group(1) is just the number, which is what we want

        Pattern growthlanePattern = Pattern.compile("GL(\\d+)");
        Matcher growthlaneMatcher = growthlanePattern.matcher(filename);
        growthlaneMatcher.find();
        String growthlaneNumber = growthlaneMatcher.group(1); // group(0) is the whole match; group(1) is just the number, which is what we want

        String laneID = "pos_" + positionNumber + "_GL_" + growthlaneNumber;
    }

    String getPositionNumber() {
//        return laneID;
        throw new NotImplementedException();
    }


    String getGlNumber() {
//        return laneID;
        throw new NotImplementedException();
    }
}
