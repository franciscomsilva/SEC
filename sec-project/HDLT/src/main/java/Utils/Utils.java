package Utils;


import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;


public final class Utils {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String MODE = "OFB";
    private static IvParameterSpec iv;

    static {
        try {
            iv = generateIv();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
    public static boolean verifySignature(String message, String digitalSignature, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] messageHash = md.digest(messageBytes);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] receivedHash = cipher.doFinal(Base64.getDecoder().decode(digitalSignature));


        return Arrays.equals(messageHash,receivedHash);
    }

    public static byte[] signMessage(PrivateKey privateKey, String message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] messageHash = md.digest(messageBytes);

        /*UNCOMMENT THE LINE BELOW TO CHECK IF THE VERIFICATION IS WORKING*/
        //messageHash[4] = 0xf;

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE,privateKey );
        return cipher.doFinal(messageHash);
    }
    public static IvParameterSpec generateIv() throws IOException {
        byte[] iv = new byte[16];
        IvParameterSpec ivSpec = null;

        new SecureRandom().nextBytes(iv);
        ivSpec = new IvParameterSpec(iv);

        return ivSpec;
    }

    public static String encryptMessageSymmetric(Key key, String message,IvParameterSpec ivSpec) throws GeneralSecurityException, IOException {

        Cipher cipher = Cipher.getInstance("AES/" + MODE + "/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] cipherText = cipher.doFinal(message.getBytes());

        return Base64.getEncoder()
                .encodeToString(cipherText);
    }

    public static String decryptMessageSymmetric(Key key, String message, byte[] iv) throws GeneralSecurityException, IOException {
        IvParameterSpec ivSpec = null;
        ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/" + MODE + "/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

        byte[] cipheredBytes = Base64.getDecoder().decode(message);

        byte[] decipheredBytes = cipher.doFinal(cipheredBytes);

        return new String(decipheredBytes);
    }

    public static SecretKey generateSymmetricKey(byte[] keyBytes){
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
    }

    public static String encryptMessageAssymetric(Key key, String message) throws GeneralSecurityException, IOException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] resultBytes = cipher.doFinal(message.getBytes());

        return Base64.getEncoder()
                .encodeToString(resultBytes);
    }


    public static byte[] decryptMessageAssymetric(String keyFile, String message) throws GeneralSecurityException, IOException {
        PrivateKey key = readPriv(keyFile);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] cipheredBytes = Base64.getDecoder().decode(message);

        byte[] resultBytes = cipher.doFinal(cipheredBytes);

        return resultBytes;
    }

    public static String encryptSymmetricKey(Key key, SecretKey secretKey) throws GeneralSecurityException, IOException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] resultBytes = cipher.doFinal(secretKey.getEncoded());

        return Base64.getEncoder()
                .encodeToString(resultBytes);
    }

    public static SecretKey decryptSymmetricKey(String keyFile, String message) throws GeneralSecurityException, IOException {
        PrivateKey key = readPriv(keyFile);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] cipheredBytes = Base64.getDecoder().decode(message);

        byte[] resultBytes = cipher.doFinal(cipheredBytes);

        return  generateSymmetricKey(resultBytes);
    }


    public static PrivateKey readPriv(String keyPath) throws GeneralSecurityException, IOException {
        byte[] keyBytes = Files.readAllBytes(Paths.get(keyPath));

        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
    public static PublicKey readPub(String keyPath) throws GeneralSecurityException, IOException {
        byte[] keyBytes = Files.readAllBytes(Paths.get(keyPath));

        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
