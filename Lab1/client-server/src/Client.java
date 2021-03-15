import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;


class Client{

    private static boolean verifySignature(String message, String digitalSignature, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] messageHash = md.digest(messageBytes);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] receivedHash = cipher.doFinal(Base64.getDecoder().decode(digitalSignature));


        return Arrays.equals(messageHash,receivedHash);
    }

    public static void main(String args[])throws Exception{
        Socket s=new Socket("localhost",3333);
        DataInputStream din=new DataInputStream(s.getInputStream());
        DataOutputStream dout=new DataOutputStream(s.getOutputStream());
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));

        byte[] keyBytes = Files.readAllBytes(Paths.get("public"));

        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(spec);
        String digitalSignature = null;
        String message = null;

        String str="",str2="";
        while(!str.equals("stop")){
            str=br.readLine();
            dout.writeUTF(str);
            dout.flush();
            str2=din.readUTF();
            message = str2.split("Message:")[1].split(" Digital")[0];
            System.out.println("Message:" + message);
            digitalSignature = str2.split("Digital Signature:")[1];
            System.out.println("Digital Signature:" + digitalSignature);

            //VERIFIES THE SIGNATURE
            if(verifySignature(message,digitalSignature,publicKey)){
                System.out.println("Signature Verified!");
            }else{
                System.out.println("Signature Not Verified!");
            }
        }

        dout.close();
        s.close();
    }}
