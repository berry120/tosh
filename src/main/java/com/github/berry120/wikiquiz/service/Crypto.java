package com.github.berry120.wikiquiz.service;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@ApplicationScoped
public class Crypto {

    public String getCaseInsensitiveMd5(String toHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(toHash.toLowerCase().getBytes());
            byte[] digest = md.digest();
            return DatatypeConverter.printHexBinary(digest).toLowerCase();
        } catch (NoSuchAlgorithmException ex) {
            throw new AssertionError("MD5 not available");
        }
    }

}
