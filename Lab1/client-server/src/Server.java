import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import Utils.Utils;


class Server{


    public static void main(String args[])throws Exception{

        /*STARTS INPUT AND OUPUT SOCKET*/
        ServerSocket ss=new ServerSocket(3333);
        Socket s=ss.accept();
        System.out.println("Client connection established!");
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


        String recv="",message="", digitalSignatureRecv="";

        /*READS RECEIVED UNTIL STRING='stop'*/
        while(!recv.equals("stop")){
            /*READS THE RECEIVED INPUT*/
            recv=din.readUTF();

            System.out.println("Received from client: ");
            /*PROCESSES THE RECEIVED MESSAGE*/
            message = recv.split("Message:")[1].split(" Digital")[0];
            System.out.println("Message:" + message);

            digitalSignatureRecv = recv.split("Digital Signature:")[1];
            System.out.println("Digital Signature:" + digitalSignatureRecv);

            //VERIFIES THE SIGNATURE
            if(Utils.verifySignature(message,digitalSignatureRecv,publicKey)){
                System.out.println("Client Signature Verified!");
            }else{
                System.out.println("Client Signature Not Verified!");
            }

            //SIGN THE MESSAGE
            byte[] digitalSignatureSent = Utils.signMessage(privateKey,message);

            //SEND THE MESSAGE
            StringBuilder sb = new StringBuilder();
            sb.append("Message:" + message);
            sb.append(" Digital Signature:");
            sb.append(new String(Base64.getEncoder().encode(digitalSignatureSent)));
            dout.writeUTF(sb.toString());
            dout.flush();

            System.out.println("----------------------------------------------------------------------------------------------------------------------------");
        }
        din.close();
        s.close();
        ss.close();
    }}