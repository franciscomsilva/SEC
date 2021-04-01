package Utils;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;


public final class Utils {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String MODE = "OFB";
    private static final String IV_FILE = "Keys/iv";
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

        File iv_file = new File(IV_FILE);
        if(!Files.exists(iv_file.toPath())){
            /*WRITES NEW IV TO FILE*/
            new SecureRandom().nextBytes(iv);
            ivSpec = new IvParameterSpec(iv);
            Files.write(iv_file.toPath(), ivSpec.getIV());

            return ivSpec;
        }
        iv = Files.readAllBytes(iv_file.toPath());
        ivSpec = new IvParameterSpec(iv);
        Files.write(iv_file.toPath(), ivSpec.getIV());

        return ivSpec;
    }

    public static String encryptMessage(String keyFile, String message) throws GeneralSecurityException, IOException {
        Key key = read(keyFile);
        Cipher cipher = Cipher.getInstance("AES/" + MODE + "/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, generateIv());
        byte[] cipherText = cipher.doFinal(message.getBytes());

        return Base64.getEncoder()
                .encodeToString(cipherText);
    }

    public static String decryptMessage(String keyFile, String message) throws GeneralSecurityException, IOException {
        Key key = read(keyFile);
        Cipher cipher = Cipher.getInstance("AES/" + MODE + "/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, generateIv());

        byte[] cipheredBytes = Base64.getDecoder().decode(message);

        byte[] decipheredBytes = cipher.doFinal(cipheredBytes);

        return new String(decipheredBytes);
    }

    public static Key read(String keyPath) throws GeneralSecurityException, IOException {
        FileInputStream fis = new FileInputStream(keyPath);
        byte[] encoded = new byte[fis.available()];
        fis.read(encoded);
        fis.close();

        return new SecretKeySpec(encoded, 0, 16, "AES");
    }

}
