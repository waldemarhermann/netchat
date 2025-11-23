package de.thb.netchat.util;

import java.math.BigInteger;
import java.security.MessageDigest;

public class SecurityUtil {

    // Diese Methode macht aus "hallo" -> "59d9a6df06b9f610..." (SHA-256)
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());

            // Der "Trick": BigInteger wandelt die Bytes direkt in einen Hex-String um
            return new BigInteger(1, hash).toString(16);

        } catch (Exception e) {
            return null;
        }
    }
}