package ecommerce.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class PasswordUtil {

    private PasswordUtil() {}

    public static String hacher(String motDePasse) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(motDePasse.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithme SHA-256 non disponible", e);
        }
    }

    public static boolean verifier(String motDePasse, String hash) {
        return hacher(motDePasse).equals(hash);
    }

    public static boolean estAssezSolide(String motDePasse) {
        if (motDePasse == null || motDePasse.length() < 8) return false;
        boolean hasUpper = motDePasse.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = motDePasse.chars().anyMatch(Character::isDigit);
        return hasUpper && hasDigit;
    }
}