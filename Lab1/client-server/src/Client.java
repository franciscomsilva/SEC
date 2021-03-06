import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import Utils.Utils;

import javax.crypto.spec.IvParameterSpec;


class Client{


    public static void main(String args[])throws Exception{
        /*STARTS INPUT AND OUPUT SOCKET*/
        Socket s=new Socket("localhost",3333);
        System.out.println("Connection to Server Established!");
        DataInputStream din=new DataInputStream(s.getInputStream());
        DataOutputStream dout=new DataOutputStream(s.getOutputStream());
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));

        /*READS PRIVATE AND PUBLIC KEY KEY TO SIGN AND VERIFY MESSAGE*/
        byte[] privKeyBytes = Files.readAllBytes(Paths.get("Keys/priv_rsa"));
        PKCS8EncodedKeySpec specPriv =
                new PKCS8EncodedKeySpec(privKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(specPriv);

        byte[] publicKeyBytes = Files.readAllBytes(Paths.get("Keys/public_rsa"));
        X509EncodedKeySpec specPublic =
                new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = kf.generatePublic(specPublic);


        String input="",recv="", message="", digitalSignatureRecv="";

        /*READS INPUT UNTIL STRING='stop'*/
        while(!input.equals("stop")){
            /*READS THE USER INPUT*/
            input=br.readLine();

            /*SIGNS THE INPUT*/
            byte[] digitalSignatureSent = Utils.signMessage(privateKey,input);

            /*JOINS THE STRING AND THE SIGNATURE*/
            StringBuilder sb = new StringBuilder();
            sb.append("Message:" + input);
            sb.append(" Digital Signature:");
            sb.append(new String(Base64.getEncoder().encode(digitalSignatureSent)));

            /*ENCRYPT THE WHOLE MESSAGE*/
            String encrypted = Utils.encryptMessage("Keys/key_aes",sb.toString());

            /*SENDS THE MESSAGE AND THE SIGNATURE*/
            System.out.println("Sending encrypted message: " + encrypted);
            dout.writeUTF(encrypted);
            dout.flush();
            System.out.println("----------------------------------------------------------------------------------------------------------------------------");




            /*RECEIVES AND PROCESSES THE RECEIVED INPUT*/
            recv=din.readUTF();

            /*DECRYPTS THE RECEIVED MESSAGE*/
            System.out.println("Received Encrypted Message: " + recv);
            String decrypted = Utils.decryptMessage("Keys/key_aes",recv);


            /*PROCESSES THE RECEIVED MESSAGE*/
            message = decrypted.split("Message:")[1].split(" Digital")[0];
            System.out.println("Message:" + message);

            digitalSignatureRecv = decrypted.split("Digital Signature:")[1];
            System.out.println("Digital Signature:" + digitalSignatureRecv);


            //VERIFIES THE SIGNATURE
            if(Utils.verifySignature(message,digitalSignatureRecv,publicKey)){
                System.out.println("Server Signature Verified!");
            }else{
                System.out.println("Server Signature Not Verified!");
            }

            System.out.println("----------------------------------------------------------------------------------------------------------------------------");
        }

        dout.close();
        s.close();
    }}
