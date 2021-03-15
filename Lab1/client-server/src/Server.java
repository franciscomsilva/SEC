import javax.crypto.Cipher;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

class Server{
    public static void main(String args[])throws Exception{
        ServerSocket ss=new ServerSocket(3333);
        Socket s=ss.accept();
        DataInputStream din=new DataInputStream(s.getInputStream());
        DataOutputStream dout=new DataOutputStream(s.getOutputStream());
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));


        byte[] keyBytes = Files.readAllBytes(Paths.get("priv"));

        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(spec);

        String str="",str2="";
        while(!str.equals("stop")){
            str=din.readUTF();
            System.out.println(str);

            //SIGN THE MESSAGE
            byte[] messageBytes = str.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageHash = md.digest(messageBytes);
           // messageHash[4] = 0xf;

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE,privateKey );
            byte[] digitalSignature = cipher.doFinal(messageHash);

            //SEND THE MESSAGE
            StringBuilder sb = new StringBuilder();
            sb.append("Message:" + str);
            sb.append(" Digital Signature:");
            sb.append(new String(Base64.getEncoder().encode(digitalSignature)));
            dout.writeUTF(sb.toString());
            dout.flush();
        }
        din.close();
        s.close();
        ss.close();
    }}