import com.google.gson.*;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import hacontract.HAProtocolGrpc;
import hacontract.UserAtLocation;
import hacontract.Users;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import userserver.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

import Utils.*;
import userserver.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static java.lang.Integer.parseInt;

public class HDLT_Server extends UserServerGrpc.UserServerImplBase {
    // Variaveis Globais
    private static int n_server = 0;
    private static String REPORTS_FILE = "files/location_reports";
    private static String USERS_CONNECTION_FILE =  "files/users_connection.txt";

    private static int BYZANTINE_USERS = 3;
    private static HashMap<String,String> UsersMap = new HashMap<>();
    private static JsonArray reports = new JsonArray();
    private static HashMap<String, SecretKey> userSymmetricKeys = new HashMap<String, SecretKey>();
    private static HashMap<SecretKey, Integer> userSymmetricKeysCounter = new HashMap<SecretKey, Integer>();
    private static HashMap<String, Integer> userCounters = new HashMap<String, Integer>();

    private static String keystore_password;


    public static void readUsers() {
        try (CSVReader reader = new CSVReader(new FileReader(USERS_CONNECTION_FILE))) {
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

    @Override
    public void init(InitMessage request, StreamObserver<Key> responseObserver) {
       String user = request.getUser();
       int c = request.getCounter();
       SecretKey secretKey = null;
        try {
            String verify = user + "," + c;
            if(!verifyMessage(user, verify, request.getDigSig())){
                System.err.println("ERROR: Message not Verified");
                responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Integrity of the Message"))));
                return;
            }

            secretKey = AESKeyGenerator.write();
            userSymmetricKeys.put(user,secretKey);
            userSymmetricKeysCounter.put(secretKey, 10);

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PublicKey pubKey = null;
        try {
             pubKey = Utils.readPub("keys/"+user);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String encryptedKey = Utils.encryptSymmetricKey(pubKey,secretKey);
            String msg = encryptedKey + "," + (c+1);
            String digSig = signMessage(msg);
            Key symmetricKeyResponse = Key.newBuilder().setKey(encryptedKey).setCounter(c+1).setDigSig(digSig).build();
            responseObserver.onNext(symmetricKeyResponse);
            responseObserver.onCompleted();
            if(userCounters.containsKey(user))
                userCounters.replace(user, c+1);
            else
                userCounters.put(user,c+1);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public  void submitLocationReport(LocationReport request, StreamObserver<LocationResponse> responseObserver) {

        //Decoding
        String message = null;
        String user = null;
        JsonObject convertedRequest = null;
        try {
            byte[] iv = Base64.getDecoder().decode(request.getIv());
            user = request.getUser();
            if (!userSymmetricKeys.containsKey(user)) { //throw error: no key for user
                System.err.println("ERROR: No Key for User " + user);
                responseObserver.onError(new StatusException((Status.NOT_FOUND.withDescription("ERROR: Key for user " + user + "not found"))));
                return;
            }
            String encryptedMessage = request.getMessage();
            SecretKey sk = userSymmetricKeys.get(user);
            int counter = userSymmetricKeysCounter.get(sk);
            if (counter == 0) { //throw error: key timeout
                System.err.println("ERROR: Key Timeout");
                userSymmetricKeysCounter.remove(sk);
                userSymmetricKeys.remove(user);
                userCounters.remove(user);
                responseObserver.onError(new StatusException((Status.RESOURCE_EXHAUSTED.withDescription("ERROR: Key Timeout"))));
                return;
            }
            message = Utils.decryptMessageSymmetric(sk, encryptedMessage, iv);
            userSymmetricKeysCounter.replace(sk, counter-1);
            convertedRequest = new Gson().fromJson(message, JsonObject.class);
        } catch (Exception e) {
            System.err.println("ERROR: Invalid key");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid key"))));
            return;
        }
        if(!verifyMessage(user,message,request.getDigSig())){
            System.err.println("ERROR: Message not Verified");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Integrity of the Message"))));
            return;
        }
        String requester = convertedRequest.get("user").getAsString();
        /*if(!requester.equals(user)){
            System.err.println("ERROR: Invalid user");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid user"))));
            return;
        }*/
        int c = convertedRequest.get("counter").getAsInt();
        if(c <= userCounters.get(user)){
            System.err.println("ERROR: Invalid counter");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid counter"))));
            return;
        }
        convertedRequest.remove("counter");
        int epoch = convertedRequest.get("epoch").getAsInt();

        boolean flagRequest = false;
        /*RUNS THROUGH THE EPOCH OBJECTS WITH ALL THE REPORTS FOR THAT EPOCH*/
        for( JsonElement report_epoch : reports){
            if(report_epoch.getAsJsonObject().get("epoch").getAsInt() == epoch)
            {
                JsonArray reports = report_epoch.getAsJsonObject().get("reports").getAsJsonArray();
                for(JsonElement jsonElement : reports){
                    if(jsonElement.getAsJsonObject().get("user").getAsString().equals(user)){
                        flagRequest = true;
                        break;
                    }
                }
                if(!flagRequest)
                    break;
            }
        }

        if(flagRequest){
            System.err.println("ERROR: Invalid request");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid request"))));
            return;
        }

        int xCoords = convertedRequest.get("xCoord").getAsInt();
        int yCoords = convertedRequest.get("yCoord").getAsInt();
        String writerDigSig = convertedRequest.get("writerDigSig").getAsString();
        JsonArray ab = convertedRequest.get("proofers").getAsJsonArray();

        Map<String, String> proofers = new HashMap<>();
        ArrayList<String> proofersIDs = new ArrayList<>();
        for (JsonElement o : ab){
            JsonObject obj = o.getAsJsonObject();
            String userID = obj.get("userID").getAsString();
            if(userID.equals(user)){
                System.err.println("ERROR: Invalid proofers");
                responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid proofers"))));
                return;
            }
            if(proofersIDs.contains(userID)){
                System.err.println("ERROR: Invalid proofers");
                responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid proofers"))));
                return;
            }

            proofersIDs.add(userID);

            proofers.put(userID,obj.get("digSIG").getAsString());
        }

        String verify = requester+","+epoch+","+xCoords+","+yCoords;

        int counter = 0;
        Boolean done = false;

        if(!proofers.isEmpty() && proofers.size() >= BYZANTINE_USERS) {
            for (Map.Entry<String, String> entry : proofers.entrySet()) {

                KeyFactory kf = null;
                try {
                    kf = KeyFactory.getInstance("RSA");

                    byte[] publicKeyBytes = Files.readAllBytes(Paths.get("keys/" + entry.getKey()));
                    X509EncodedKeySpec specPublic = new X509EncodedKeySpec(publicKeyBytes);
                    PublicKey publicKey = kf.generatePublic(specPublic);

                    if (Utils.verifySignature(verify, entry.getValue(), publicKey)) {
                        System.out.println("Proofer " + entry.getKey() + " Signature Verified!");
                    } else {
                        System.out.println("Proofer " + entry.getKey() + " Signature Not Verified!");
                        counter++;
                    }
                } catch (Exception e) {
                    System.out.println("Proofer " + entry.getKey() + " Signature Not Verified!");
                    counter++;
                }
            }
                if (proofers.size() - counter >= BYZANTINE_USERS) {
                    boolean flagEpoch = false;
                    /*CHECK IF JSON OBJECT FOR THAT EPOCH EXISTS*/
                    JsonObject epoch_object = null;
                    for( JsonElement report : reports){
                        if(report.getAsJsonObject().get("epoch").getAsInt() == epoch)
                        {
                            epoch_object = report.getAsJsonObject();
                        }
                    }
                    JsonArray reports_array = null;

                    if(epoch_object == null){
                        epoch_object = new JsonObject();
                        epoch_object.addProperty("epoch",epoch);
                        reports_array = new JsonArray();
                        flagEpoch = true;
                    } else{
                        reports_array = epoch_object.getAsJsonArray("reports");
                    }
                    JsonObject reportObject = new JsonObject();
                    reportObject.addProperty("user",user);
                    reportObject.addProperty("epoch",epoch);
                    reportObject.addProperty("writerDigSig",writerDigSig);
                    reportObject.addProperty("xCoord",xCoords);
                    reportObject.addProperty("yCoord",yCoords);
                    JsonArray proofers_array = new JsonArray();

                    for(Map.Entry<String,String> entry : proofers.entrySet()){
                        JsonObject entryObject = new JsonObject();
                        entryObject.addProperty("userID",entry.getKey());
                        entryObject.addProperty("digSIG",entry.getValue());
                        proofers_array.add(entryObject);
                    }
                    reportObject.add("proofers",proofers_array);
                    reports_array.add(reportObject);
                    epoch_object.add("reports",reports_array);
                    if(flagEpoch)
                        reports.add(epoch_object);

                    try {
                        saveReportsToFile();
                        System.out.println("INFO: Location report submitted!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    done = true;
                } else {
                    System.err.println("ERROR: Location report invalid!");
                    done = false;
                }
        }
        else{
            done = false;
            System.err.println("ERROR: Invalid request");
        }

        JsonObject json = new JsonObject();
        json.addProperty("Done",done);
        json.addProperty("counter",c+1);
        userCounters.replace(user, c+1);

        //Encriptação
        String resp = null, respDigSig = null;
        IvParameterSpec iv = null;
        try {
            iv = Utils.generateIv();
            resp = Utils.encryptMessageSymmetric(userSymmetricKeys.get(requester),json.toString(),iv);
            respDigSig = signMessage(json.toString());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LocationResponse lr = LocationResponse.newBuilder().setMessage(resp).setIv(Base64.getEncoder()
                .encodeToString(iv.getIV())).setDigSig(respDigSig).build();
        responseObserver.onNext(lr);
        responseObserver.onCompleted();
    }

    @Override
    public void obtainLocationReport(GetLocation request, StreamObserver<LocationStatus> responseObserver) {
        //Decoding
        String message = null;
        JsonObject convertedRequest = null;
        String user = null;
        try {
            byte[] iv = Base64.getDecoder().decode(request.getIv());
            user = request.getUser();
            String encryptedMessage = request.getMessage();
            if (!userSymmetricKeys.containsKey(user)) { //throw error: no key for user
                System.err.println("ERROR: Key for user " + user + "not found");
                responseObserver.onError(new StatusException((Status.NOT_FOUND.withDescription("ERROR: Key for user " + user + "not found"))));
                return;
            }
            SecretKey sk = userSymmetricKeys.get(user);
            int counter = userSymmetricKeysCounter.get(sk);
            if (counter == 0) { //throw error: key timeout
                System.err.println("ERROR: Key Timeout");
                userSymmetricKeysCounter.remove(sk);
                userSymmetricKeys.remove(user);
                userCounters.remove(user);
                responseObserver.onError(new StatusException((Status.RESOURCE_EXHAUSTED.withDescription("ERROR: Key Timeout"))));
                return;
            }
            message = Utils.decryptMessageSymmetric(sk,encryptedMessage,iv);
            userSymmetricKeysCounter.replace(sk, counter-1);
            convertedRequest = new Gson().fromJson(message, JsonObject.class);
        } catch (Exception e) {
            System.err.println("ERROR: Invalid key");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid key"))));
            return;
        }

        if(!verifyMessage(request.getUser(),message,request.getDigSig())){
            System.err.println("ERROR: Message not Verified");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Integrity of the Message"))));
            return;
        }
        int c = convertedRequest.get("counter").getAsInt();
        if(c <= userCounters.get(user)){
            System.err.println("ERROR: Invalid counter");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid counter"))));
            return;
        }

        //Proof-of-Work
        int nounce = convertedRequest.get("nounce").getAsInt();
        String pow = convertedRequest.get("pow").getAsString();
        convertedRequest.remove("nounce");
        convertedRequest.remove("pow");
        convertedRequest.remove("counter");
        Boolean powVerify = false;
        try {
            powVerify = verifyPoW(convertedRequest.toString(), nounce, pow);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (!powVerify) {
            System.err.println("ERROR: Invalid Proof-of-Work");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid Proof-of-Work"))));
            return;
        }

        String requester = convertedRequest.get("userID").getAsString();
        int epoch = convertedRequest.get("Epoch").getAsInt();


        boolean flagRequest = true;
        JsonObject response_json = new JsonObject();
        for( JsonElement report_epoch : reports){
            if(report_epoch.getAsJsonObject().get("epoch").getAsInt() == (epoch))
            {
                JsonArray reports = report_epoch.getAsJsonObject().get("reports").getAsJsonArray();
                for(JsonElement jsonElement : reports){
                    if(jsonElement.getAsJsonObject().get("user").getAsString().equals(requester)){
                        response_json = jsonElement.getAsJsonObject();
                        flagRequest = false;
                        break;
                    }
                }
            }
        }

        if(flagRequest) {
            responseObserver.onError(new StatusException(Status.NOT_FOUND.withDescription("ERROR: No location report for that user in that epoch!")));
            System.err.println("ERROR: No location report for that user in that epoch!");
            return;
        }

        response_json.addProperty("counter",c+1);
        userCounters.replace(user, c+1);

        //Encriptação
        String resp = null, respDigSig = null;
        IvParameterSpec iv = null;
        try {
            iv = Utils.generateIv();
            resp = Utils.encryptMessageSymmetric(userSymmetricKeys.get(requester),response_json.toString(),iv);
            respDigSig = signMessage(response_json.toString());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LocationStatus ls = LocationStatus.newBuilder().setMessage(resp).setIv(Base64.getEncoder()
                .encodeToString(iv.getIV())).setDigSig(respDigSig).build();

        System.out.println("INFO: Sent location report for " + requester +  " at epoch " + epoch);
        responseObserver.onNext(ls);
        responseObserver.onCompleted();
    }

    @Override
    public void requestMyProofs(GetProofs request, StreamObserver<ProofsResponse> responseObserver) {
        //Decoding
        String message = null;
        String user = null;
        JsonObject convertedRequest = null;
        try {
            byte[] iv = Base64.getDecoder().decode(request.getIv());
            user = request.getUser();
            String encryptedMessage = request.getMessage();
            if (!userSymmetricKeys.containsKey(user)) { //throw error: no key for user
                System.err.println("ERROR: No Key for User " + user);
                responseObserver.onError(new StatusException((Status.NOT_FOUND.withDescription("ERROR: Key for user " + user + "not found"))));
                return;
            }
            SecretKey sk = userSymmetricKeys.get(user);
            int counter = userSymmetricKeysCounter.get(sk);
            if (counter == 0) { //throw error: key timeout
                System.err.println("ERROR: Key Timeout");
                userSymmetricKeysCounter.remove(sk);
                userSymmetricKeys.remove(user);
                userCounters.remove(user);
                responseObserver.onError(new StatusException((Status.RESOURCE_EXHAUSTED.withDescription("ERROR: Key Timeout"))));
                return;
            }
            message = Utils.decryptMessageSymmetric(sk,encryptedMessage,iv);
            userSymmetricKeysCounter.replace(sk, counter-1);
            convertedRequest = new Gson().fromJson(message, JsonObject.class);
        } catch (Exception e) {
            System.err.println("ERROR: Invalid key");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid key"))));
            return;
        }

        if(!verifyMessage(user,message,request.getDigSig())){
            System.err.println("ERROR: Message not Verified");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Integrity of the Message"))));
            return;
        }
        int c = convertedRequest.get("counter").getAsInt();
        if(c <= userCounters.get(user)){
            System.err.println("ERROR: Invalid counter");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid counter"))));
            return;
        }

        //Proof-of-Work
        int nounce = convertedRequest.get("nounce").getAsInt();
        String pow = convertedRequest.get("pow").getAsString();
        convertedRequest.remove("nounce");
        convertedRequest.remove("pow");
        convertedRequest.remove("counter");
        Boolean powVerify = false;
        try {
            powVerify = verifyPoW(convertedRequest.toString(), nounce, pow);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (!powVerify) {
            System.err.println("ERROR: Invalid Proof-of-Work");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid Proof-of-Work"))));
            return;
        }

        String s = convertedRequest.get("epochs").getAsString();
        int[] eps = Arrays.stream(s.split(",")).mapToInt(Integer::parseInt).toArray();
        ArrayList<Integer> epochs = (ArrayList<Integer>) Arrays.stream(eps).boxed().collect(Collectors.toList());

        JsonArray data = new JsonArray();
        for (JsonElement je : reports) {
            JsonObject jo = je.getAsJsonObject();
            int epoch = jo.get("epoch").getAsInt();
            if (epochs.contains(epoch)) {
                JsonArray reps = jo.get("reports").getAsJsonArray();
                for (JsonElement r : reps) {
                    JsonArray proofers = r.getAsJsonObject().get("proofers").getAsJsonArray();
                    for (JsonElement p : proofers) {
                        String u = p.getAsJsonObject().get("userID").getAsString();
                        if (u.equals(user)) {
                            JsonObject json = new JsonObject();
                            json.addProperty("epoch", epoch);
                            json.addProperty("user", r.getAsJsonObject().get("user").getAsString());
                            json.addProperty("xCoord", r.getAsJsonObject().get("xCoord").getAsString());
                            json.addProperty("yCoord", r.getAsJsonObject().get("yCoord").getAsString());
                            json.addProperty("prooferDigSig", p.getAsJsonObject().get("digSIG").getAsString());
                            data.add(json);
                        }
                    }
                }
            }
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("counter",c+1);
        data.add(jsonObject);
        userCounters.replace(user, c+1);

        //Encriptação
        String resp = null, respDigSig = null;
        IvParameterSpec iv = null;
        try {
            iv = Utils.generateIv();
            resp = Utils.encryptMessageSymmetric(userSymmetricKeys.get(user),data.toString(),iv);
            respDigSig = signMessage(data.toString());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ProofsResponse pr = ProofsResponse.newBuilder().setMessage(resp).setIv(Base64.getEncoder()
                .encodeToString(iv.getIV())).setDigSig(respDigSig).build();

        System.out.println("INFO: Sent proofs of " + user +  " at requested epochs");
        responseObserver.onNext(pr);
        responseObserver.onCompleted();
    }

    public static boolean verifyMessage(String user, String message, String digSig) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");

            byte[] publicKeyBytes = new byte[0];

            publicKeyBytes = Files.readAllBytes(Paths.get("keys/" + user));
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

    public static class HA_Server extends HAProtocolGrpc.HAProtocolImplBase {

        @Override
        public void obtainLocationReport(hacontract.GetLocation request, StreamObserver<hacontract.LocationStatus> responseObserver) {
            String user = "clientHA";
            //Decoding
            String message = null;
            JsonObject convertedRequest = null;

            try {
                byte[] iv = Base64.getDecoder().decode(request.getIv());
                String encryptedMessage = request.getMessage();
                if (!userSymmetricKeys.containsKey(user)) { //throw error: no key for user
                    System.err.println("ERROR: No Key for User " + user);
                    responseObserver.onError(new StatusException((Status.NOT_FOUND.withDescription("ERROR: Key for user " + user + "not found"))));
                    return;
                }
                SecretKey sk = userSymmetricKeys.get(user);
                int counter = userSymmetricKeysCounter.get(sk);
                if (counter == 0) { //throw error: key timeout
                    System.err.println("ERROR: Key Timeout");
                    userSymmetricKeysCounter.remove(sk);
                    userSymmetricKeys.remove(user);
                    userCounters.remove(user);
                    responseObserver.onError(new StatusException((Status.RESOURCE_EXHAUSTED.withDescription("ERROR: Key Timeout"))));
                    return;
                }
                message = Utils.decryptMessageSymmetric(sk,encryptedMessage,iv);
                userSymmetricKeysCounter.replace(sk, counter-1);
                convertedRequest = new Gson().fromJson(message, JsonObject.class);
            } catch (Exception e) {
                System.err.println("ERROR: Invalid key");
                responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid key"))));
                return;
            }
            if(!verifyMessage(user,message,request.getDigSig())){
                System.err.println("ERROR: Message not Verified");
                responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Integrity of the Message"))));
                return;
            }
            int c = convertedRequest.get("counter").getAsInt();
            if(c <= userCounters.get(user)){
                System.err.println("ERROR: Invalid counter");
                responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid counter"))));
                return;
            }

            String requester = convertedRequest.get("user").getAsString();
            convertedRequest.remove("counter");
            int epoch = convertedRequest.get("epoch").getAsInt();

            boolean flagRequest = true;
            JsonObject response_json = new JsonObject();
            for (JsonElement report_epoch : reports) {
                if (report_epoch.getAsJsonObject().get("epoch").getAsInt() == (epoch)) {
                    JsonArray reports = report_epoch.getAsJsonObject().get("reports").getAsJsonArray();
                    for (JsonElement jsonElement : reports) {
                        if (jsonElement.getAsJsonObject().get("user").getAsString().equals(requester)) {
                            response_json = jsonElement.getAsJsonObject();
                            flagRequest = false;
                            break;
                        }
                    }
                }
            }

            if (flagRequest) {
                responseObserver.onError(new StatusException(Status.NOT_FOUND.withDescription("ERROR: No location report for that user in that epoch!")));
                return;
            }
            //Encriptação
            String resp = null, respDigSig = null;
            IvParameterSpec iv = null;
            response_json.addProperty("counter",c+1);
            userCounters.replace(user, c+1);

            try {
                iv = Utils.generateIv();
                resp = Utils.encryptMessageSymmetric(userSymmetricKeys.get("user_ha"),response_json.toString(),iv);
                respDigSig = signMessage(response_json.toString());
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            hacontract.LocationStatus ls = hacontract.LocationStatus.newBuilder().setMessage(resp).
                    setDigSig(respDigSig).setIv(Base64.getEncoder()
                    .encodeToString(iv.getIV())).build();
            responseObserver.onNext(ls);
            responseObserver.onCompleted();
        }

        @Override
        public void obtainUsersAtLocation(UserAtLocation request, StreamObserver<Users> responseObserver) {
            String user = "clientHA";
            //Decoding
            String message = null;
            JsonObject convertedRequest = null;

            try {
                byte[] iv = Base64.getDecoder().decode(request.getIv());
                String encryptedMessage = request.getMessage();
                if (!userSymmetricKeys.containsKey(user)) { //throw error: no key for user
                    System.err.println("ERROR: No Key for User " + user);
                    responseObserver.onError(new StatusException((Status.NOT_FOUND.withDescription("ERROR: Key for user " + user + "not found"))));
                    return;
                }
                SecretKey sk = userSymmetricKeys.get(user);
                int counter = userSymmetricKeysCounter.get(sk);
                if (counter == 0) { //throw error: key timeout
                    System.err.println("ERROR: Key Timeout");
                    userSymmetricKeysCounter.remove(sk);
                    userSymmetricKeys.remove(user);
                    userCounters.remove(user);
                    responseObserver.onError(new StatusException((Status.RESOURCE_EXHAUSTED.withDescription("ERROR: Key Timeout"))));
                    return;
                }
                message = Utils.decryptMessageSymmetric(sk,encryptedMessage,iv);
                userSymmetricKeysCounter.replace(sk, counter-1);
                convertedRequest = new Gson().fromJson(message, JsonObject.class);
            } catch (Exception e) {
                System.err.println("ERROR: Invalid key");
                responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid key"))));
                return;
            }
            if(!verifyMessage(user,message,request.getDigSig())){
                System.err.println("ERROR: Message not Verified");
                responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Integrity of the Message"))));
                return;
            }
            int c = convertedRequest.get("counter").getAsInt();
            if(c <= userCounters.get(user)){
                System.err.println("ERROR: Invalid counter");
                responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid counter"))));
                return;
            }

            int epoch = convertedRequest.get("epoch").getAsInt();
            int xCoords = convertedRequest.get("xCoords").getAsInt();
            int yCoords = convertedRequest.get("yCoords").getAsInt();

            List<String> users = new ArrayList<>();

            boolean flagRequest = true;
            for (JsonElement report_epoch : reports) {
                if (report_epoch.getAsJsonObject().get("epoch").getAsInt() == (epoch)) {
                    JsonArray reports = report_epoch.getAsJsonObject().get("reports").getAsJsonArray();
                    for (JsonElement jsonElement : reports) {
                        if (jsonElement.getAsJsonObject().get("coordX").getAsInt() == (xCoords) && jsonElement.getAsJsonObject().get("coordY").getAsInt() == (yCoords)) {
                            users.add(jsonElement.getAsJsonObject().get("user").getAsString() + "," + jsonElement.getAsJsonObject().get("writerDigSIG").getAsString());
                            flagRequest = false;
                        }
                    }
                }
            }

            if (flagRequest) {
                responseObserver.onError(new StatusException(Status.NOT_FOUND.withDescription("ERROR: No location report for those coordinates in that epoch!")));
                return;
            }

            JsonObject response = new JsonObject();
            StringBuilder sb = new StringBuilder();
            for(String user2 : users){
                sb.append(user2 + ";");
            }
            userCounters.replace(user, c+1);
            response.addProperty("counter", c+1);
            response.addProperty("users",sb.toString());

            //Encriptação
            String resp = null;
            IvParameterSpec iv = null;
            String digSig = null;
            try {
                iv = Utils.generateIv();
                resp = Utils.encryptMessageSymmetric(userSymmetricKeys.get("clientHA"),response.toString(),iv);
                digSig = signMessage(response.toString());
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Users u = Users.newBuilder().setIv(Base64.getEncoder()
                    .encodeToString(iv.getIV())).setMessage(resp).setDigSig(digSig).build();
            responseObserver.onNext(u);
            responseObserver.onCompleted();
        }

        @Override
        public void init(hacontract.InitMessage request, StreamObserver<hacontract.Key> responseObserver) {
            String user = request.getUser();
            int c = request.getCounter();
            SecretKey secretKey = null;
            try {
                String verify = user + "," + c;
                if(!verifyMessage(user, verify, request.getDigSig())){
                    System.err.println("ERROR: Message not Verified");
                    responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Integrity of the Message"))));
                    return;
                }

                secretKey = AESKeyGenerator.write();
                userSymmetricKeys.put(user,secretKey);
                userSymmetricKeysCounter.put(secretKey, 10);

            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            PublicKey pubKey = null;
            try {
                pubKey = Utils.readPub("keys/"+user);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                String encryptedKey = Utils.encryptSymmetricKey(pubKey,secretKey);
                String msg = encryptedKey + "," + (c+1);
                String digSig = signMessage(msg);
                hacontract.Key symmetricKeyResponse = hacontract.Key.newBuilder().setKey(encryptedKey).
                        setDigSig(digSig).setCounter(c+1).build();
                responseObserver.onNext(symmetricKeyResponse);
                responseObserver.onCompleted();

                if(userCounters.containsKey(user))
                    userCounters.replace(user, c+1);
                else
                    userCounters.put(user,c+1);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveReportsToFile() throws IOException {
        FileWriter fileWriter = new FileWriter(REPORTS_FILE);
        try{
            fileWriter.write(reports.toString());
        }catch (Exception e){
            System.err.println(e.getStackTrace());
        }finally {
            fileWriter.flush();
            fileWriter.close();
        }
    }

    private static void readReportsFromFile() throws IOException {
        File f = new File(REPORTS_FILE);
        if(!f.exists() || f.isDirectory()) {
            return;
        }

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(REPORTS_FILE))){
           String line = bufferedReader.readLine();
           reports = new Gson().fromJson(line,JsonArray.class);
        }catch(Exception e){
            System.err.println(e.getMessage());
        }

    }

    private static String signMessage(String msgToSign) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnrecoverableKeyException, KeyStoreException {
        byte[] messageSigned = Utils.signMessage("keystores/keystore_server" + n_server + ".keystore", keystore_password, msgToSign);
        return new String(Base64.getEncoder().encode(messageSigned));
    }

    private static Boolean verifyPoW(String msg, int nounce, String hash) throws NoSuchAlgorithmException {
        String calcHash = Utils.computeSHA256(msg + nounce);
        if (calcHash.equals(hash)) {
            //if (calcHash.charAt(0) == '0' && calcHash.charAt(1) == '0')
            if (calcHash.substring(0, 4).equals("0000"))
                return true;
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        readUsers();
        n_server = Integer.parseInt(args[0]);
        int svcPort = Integer.parseInt(UsersMap.get("server").split(":")[1]) + n_server;
        int svcPort_HA = svcPort + 50 ;
        Server svc = null;
        Server svc_HA = null;

        REPORTS_FILE += + n_server + ".json";

        readReportsFromFile();


        /*GETS THE USER PASSWORD FROM INPUT*/
        keystore_password = Utils.getPasswordInput();

        try {
            svc = ServerBuilder
                    .forPort(svcPort)
                    .addService(new HDLT_Server())
                    .build();
            svc_HA = ServerBuilder
                    .forPort( svcPort_HA)
                    .addService(new HA_Server())
                    .build();
            svc.start();
            svc_HA.start();

            System.out.println("Server started, listening on " + svcPort);
            System.out.println("HA Server started, listening on " + svcPort_HA);


            svc.awaitTermination();
            svc.shutdown();

            svc_HA.awaitTermination();
            svc_HA.shutdown();


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
