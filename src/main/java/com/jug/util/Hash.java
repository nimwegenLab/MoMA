package com.jug.util;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * Calculates the specified checksum of the provided input file to the method
 * checksum(...).
 *
 * Implementation was found here: https://memorynotfound.com/calculate-file-checksum-java/
 */

public enum Hash {

    MD5("MD5"),
    SHA1("SHA1"),
    SHA256("SHA-256"),
    SHA512("SHA-512");

    private String name;

    Hash(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Calculate the desired checksum as a byte array. For example to calculate
     * a SHA256 checksum do:
     * byte[] hash = Hash.SHA256.checksum(new File(modelFile))
     *
     * You can use the provided static method toHex(...) to convert this
     * byte-array to the more common hex-checksum, which is also returned
     * by Linux tools.
     *
     * @param input
     * @return
     */
    public byte[] checksum(File input) {
        try (InputStream in = new FileInputStream(input)) {
            MessageDigest digest = MessageDigest.getInstance(getName());
            byte[] block = new byte[4096];
            int length;
            while ((length = in.read(block)) > 0) {
                digest.update(block, 0, length);
            }
            return digest.digest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toHex(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }
}