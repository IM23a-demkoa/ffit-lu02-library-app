package ch.bzz;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordHandler {

    public static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public static byte[] hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        return md.digest(password.getBytes());
    }

    public static boolean verifyPassword(String password, byte[] hash, byte[] salt) throws NoSuchAlgorithmException {
        byte[] newHash = hashPassword(password, salt);
        if (newHash.length != hash.length) return false;
        for (int i = 0; i < hash.length; i++) {
            if (newHash[i] != hash[i]) return false;
        }
        return true;
    }
}
