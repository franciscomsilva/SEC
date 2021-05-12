import Utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import hacontract.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import userserver.UserServerGrpc;
//import userserver.UserServerGrpc;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class HDLT_Ha {

    static HashMap<String,String> UsersMap = new HashMap<>();

    static HAProtocolGrpc.HAProtocolBlockingStub bStub;//TODO Mudar o tipo de stub consoante o contracto

    private static String user;

    private static SecretKey symmetricKey;



    public static void readUsers() {
        try (CSVReader reader = new CSVReader(new FileReader("files/users_connection.txt"))) {
            String[] lineInArray;
            while ((lineInArray = reader.readNext()) != null) {
                UsersMap.put(lineInArray[0],lineInArray[1]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public static void obtainLocationReport(String user, int epoch) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("epoch", epoch);
        jsonObject.addProperty("user", user);


        // Encriptação
        String encryptedMessage = null;
        IvParameterSpec ivSpec = null;
        hacontract.LocationStatus ls = null;
        try {
            ivSpec = Utils.generateIv();
            encryptedMessage = Utils.encryptMessageSymmetric(symmetricKey,jsonObject.toString(),ivSpec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        hacontract.GetLocation gl = hacontract.GetLocation.newBuilder().setMessage(encryptedMessage).setIv(Base64.getEncoder()
                .encodeToString(ivSpec.getIV())).build();
        try{
            ls = bStub.obtainLocationReport(gl);
        }catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }
        // Desencriptar
        encryptedMessage = ls.getMessage();
        byte[] iv =Base64.getDecoder().decode(ls.getIv());
        String decryptedMessage = null;
        try {
            decryptedMessage = Utils.decryptMessageSymmetric(symmetricKey,encryptedMessage,iv);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonObject convertedResponse = new Gson().fromJson(decryptedMessage, JsonObject.class);


        System.out.println("User "+user+" at "+epoch+" are in coordinates ("+convertedResponse.get("xCoords").getAsInt()+","+convertedResponse.get("yCoords").getAsInt()+")");

    }

    public static void ObtainUsersAtLocation(int epoch, int xCoords, int yCoords) {

        JsonObject json =  new JsonObject();
        json.addProperty("epoch", epoch);
        json.addProperty("xCoords", xCoords);
        json.addProperty("yCoords", yCoords);

        // Encriptação
        String encryptedMessage = null;
        IvParameterSpec ivSpec = null;
        hacontract.LocationStatus ls = null;
        try {
            ivSpec = Utils.generateIv();
            encryptedMessage = Utils.encryptMessageSymmetric(symmetricKey,json.toString(),ivSpec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UserAtLocation ua = UserAtLocation.newBuilder().setIv(Base64.getEncoder()
                .encodeToString(ivSpec.getIV())).setMessage(encryptedMessage).build();

        Users users;
        try{
            users =  bStub.obtainUsersAtLocation(ua);
        }catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }

        // Desencriptar
        encryptedMessage = users.getMessage();
        byte[] iv =Base64.getDecoder().decode(users.getIv());
        String decryptedMessage = null;
        try {
            decryptedMessage = Utils.decryptMessageSymmetric(symmetricKey,encryptedMessage,iv);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] splits = decryptedMessage.split(";");

        for(String u : splits){
            String[] spl = u.split(",");


            System.out.println("USER: " + u);
        }
        System.out.println("");
    }

    public static boolean verifyMessage(String node, String message, String digSig) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            byte[] publicKeyBytes = new byte[0];

            publicKeyBytes = Files.readAllBytes(Paths.get("keys/" + node));
            X509EncodedKeySpec specPublic = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = kf.generatePublic(specPublic);

            if (Utils.verifySignature(message, digSig, publicKey)) {
                System.out.println("Message Signature Verified!");
                return true;
            } else {
                System.out.println("Message Signature Not Verified!");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        readUsers();

        //Conexão com o servidor
        String phrase = UsersMap.get("server");
        user = "user_ha";
        String svcIP = phrase.split(":")[0];
        int sPort = Integer.parseInt(phrase.split(":")[1] )+ 50;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(svcIP, sPort)
                .usePlaintext()
                .build();
        bStub = HAProtocolGrpc.newBlockingStub(channel);

        hacontract.InitMessage initMessage = hacontract.InitMessage.newBuilder().setUser(user).build();
        hacontract.Key responseKey = null;
        try{
            responseKey = bStub.init(initMessage);
        }catch(Exception e){
            System.err.println("ERROR: Server connection failed!");
            return;
        }
        String base64SymmetricKey = responseKey.getKey();

        /*GETS THE USER PASSWORD FROM INPUT*/
        String password = Utils.getPasswordInput();

        byte[] symmetricKeyBytes = Utils.decryptMessageAssymetric("keystores/keystore_" + user + ".keystore",password,base64SymmetricKey);
        symmetricKey = Utils.generateSymmetricKey(symmetricKeyBytes);

        System.out.println("\nAvailable commands:");
        System.out.println("- ObtainLocationReport (o <user> <epoch>): obtains location of indicated user at indicated epoch");
        System.out.println("- ObtainUsersAtLocation (r <epoch> <xCoord> <yCoord>): obtains users at specified epoch and X and Y coordinates");

        try {
            Scanner scanner = new Scanner(System.in);
            int epoch = 0;
            while(true){
                System.out.print("> ");
                String line = scanner.nextLine();
                String cmd = line.split(" ")[0];
                switch (cmd) {
                    case "ObtainLocationReport":
                    case "o":
                        String user = line.split(" ")[1];
                        epoch = Integer.parseInt(line.split(" ")[2]);
                        System.out.println("Requesting the position of "+user+" at epoch "+ epoch);
                        obtainLocationReport(user,epoch);
                        break;


                    case "ObtainUsersAtLocation":
                    case "r":
                        epoch = Integer.parseInt(line.split(" ")[1]);
                        int xCoords = Integer.parseInt(line.split(" ")[2]);
                        int yCoords = Integer.parseInt(line.split(" ")[3]);
                        System.out.println("Requesting Location Proof to nearby users");
                        ObtainUsersAtLocation(epoch,xCoords,yCoords);
                        break;


                    default:
                        System.out.println("Some errors in reading of the script");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
