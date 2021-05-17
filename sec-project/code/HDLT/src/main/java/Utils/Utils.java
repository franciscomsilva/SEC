package Utils;


import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;


public class Utils {
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
    /* s must be an even-length string. */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
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

    public static byte[] signMessage(String keypath, String password,String message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnrecoverableKeyException, KeyStoreException {
        /*LOADS KEYSTORE*/
        KeyStore ks = null;
        try{
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(keypath), password.toCharArray());
        }catch(Exception e){
            System.err.println(e.getMessage());
        }

        /*LOADS PRIVATE KEY FROM KEYSTORE*/
        Key privateKey = ks.getKey("1", password.toCharArray());


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


    public static byte[] decryptMessageAssymetric(String keyFile, String password,String message) throws GeneralSecurityException, IOException {

        /*LOADS KEYSTORE*/
        KeyStore ks = null;
        try{
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(keyFile), password.toCharArray());
        }catch(Exception e){
            System.err.println(e.getMessage());
        }

        /*LOADS PRIVATE KEY FROM KEYSTORE*/
        Key privateKey = ks.getKey("1", password.toCharArray());


        /*DECRYPTS THE MESSAGE*/

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

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



    public static PublicKey readPub(String keyPath) throws GeneralSecurityException, IOException {
        File f = new File(keyPath);
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int)f.length()];
        dis.readFully(keyBytes);
        dis.close();


        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }


    public static String getPasswordInput(){
        System.out.print("Please enter the password to unlock the Key file: ");
        Scanner scanner = new Scanner(System. in);
        return scanner. nextLine();
    }

    public static String computeSHA256(String msg) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(msg.getBytes(StandardCharsets.UTF_8));
        String sha256hex = bytesToHex(hash);
        return sha256hex;
    }

}
