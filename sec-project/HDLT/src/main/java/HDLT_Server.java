import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import Utils.*;
import userserver.Key;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static java.lang.Integer.parseInt;

public class HDLT_Server extends UserServerGrpc.UserServerImplBase {
    // Variaveis Globais

    private static String REPORTS_FILE = "location_reports";

    private static String SYMMETRICS_FILE = "symmetric_keys";

    private static Double BYZANTINE_RATIO = 0.5;

    private static HashMap<String,String> UsersMap = new HashMap<>();

    private static HashMap<Integer,HashMap<String,int[]>> reports = new HashMap<>();

    private static HashMap<String, SecretKey> userSymmetricKeys = new HashMap<String, SecretKey>();

    public static void readUsers() {
        try (CSVReader reader = new CSVReader(new FileReader("Users.txt"))) {
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
       SecretKey secretKey = null;
        try {
            secretKey = AESKeyGenerator.write();
            userSymmetricKeys.put(user,secretKey);
            saveKeysToFile();
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
            Key symmetricKeyResponse = Key.newBuilder().setKey(encryptedKey).build();
            responseObserver.onNext(symmetricKeyResponse);
            responseObserver.onCompleted();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void submitLocationReport(LocationReport request, StreamObserver<LocationResponse> responseObserver) {

        //Decoding
        String message = null;
        String user = null;
        try {
            byte[] iv = Base64.getDecoder().decode(request.getIv());
            user = request.getUser();
            String encryptedMessage = request.getMessage();
            message = Utils.decryptMessageSymmetric(userSymmetricKeys.get(user),encryptedMessage,iv);
        } catch (Exception e) {
            System.err.print("ERROR: Invalid key");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid key"))));
            return;
        }

        JsonObject convertedRequest= new Gson().fromJson(message, JsonObject.class);
        String requester = convertedRequest.get("userID").getAsString();

        if(!requester.equals(user)){
            System.err.print("ERROR: Invalid user");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid user"))));
            return;
        }

        int epoch = convertedRequest.get("currentEpoch").getAsInt();

        if(reports.containsKey(epoch) && reports.get(epoch).containsKey(user)){
            System.err.print("ERROR: Invalid request");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid request"))));
            return;
        }

        int xCoords = convertedRequest.get("xCoord").getAsInt();
        int yCoords = convertedRequest.get("yCoord").getAsInt();
        JsonArray ab = convertedRequest.get("proofers").getAsJsonArray();

        Map<String, String> proofers = new HashMap<>();

        for (JsonElement o : ab){
            JsonObject obj = o.getAsJsonObject();
            System.out.println(o.toString());
            proofers.put(obj.get("userID").getAsString(),obj.get("digSIG").getAsString());
        }



        for (Map.Entry<String, String> entry : proofers.entrySet()) {
            System.out.println(entry.getKey()+":"+entry.getValue());
        }
        String verify = requester+","+epoch+","+xCoords+","+yCoords;

        int counter = 0;
        Boolean done = false;
        Double flag = 0.0;

        if(!proofers.isEmpty()) {
            for (Map.Entry<String, String> entry : proofers.entrySet()) {
                System.out.println("For " + entry.getKey() + " verifing " + entry.getValue());

                KeyFactory kf = null;
                try {
                    kf = KeyFactory.getInstance("RSA");

                    byte[] publicKeyBytes = Files.readAllBytes(Paths.get("keys/" + entry.getKey()));
                    X509EncodedKeySpec specPublic = new X509EncodedKeySpec(publicKeyBytes);
                    PublicKey publicKey = kf.generatePublic(specPublic);

                    if (Utils.verifySignature(verify, entry.getValue(), publicKey)) {
                        System.out.println("Server Signature Verified!");
                    } else {
                        System.out.println("Server Signature Not Verified!");
                        counter++;
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                flag = Double.valueOf(counter)/Double.valueOf(proofers.size());
                if (flag < BYZANTINE_RATIO) {
                    if (reports.containsKey(epoch)) {
                        HashMap<String, int[]> UsersAt = reports.get(epoch);
                        if (!UsersAt.containsKey(requester)) {
                            int[] b = {xCoords, yCoords};
                            UsersAt.put(requester, b);
                        }
                        reports.replace(epoch, UsersAt);

                    } else {
                        HashMap<String, int[]> a = new HashMap<>();
                        int[] b = {xCoords, yCoords};
                        a.put(requester, b);
                        reports.put(epoch, a);
                    }

                    try {
                        saveReportsToFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    done = true;
                } else {
                    done = false;
                }
            }
        }
        else{
            done = false;
        }

        JsonObject json = new JsonObject();
        json.addProperty("Done",done);

        //Encriptação


        String resp = null;
        IvParameterSpec iv = null;
        try {
            iv = Utils.generateIv();
            resp = Utils.encryptMessageSymmetric(userSymmetricKeys.get(requester),json.toString(),iv);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LocationResponse lr = LocationResponse.newBuilder().setMessage(resp).setIv(Base64.getEncoder()
                .encodeToString(iv.getIV())).build();
        responseObserver.onNext(lr);
        responseObserver.onCompleted();
    }

    @Override
    public void obtainLocationReport(GetLocation request, StreamObserver<LocationStatus> responseObserver) {
        //Decoding
        String message = null;
        try {
            byte[] iv = Base64.getDecoder().decode(request.getIv());
            String user = request.getUser();
            String encryptedMessage = request.getMessage();
            message = Utils.decryptMessageSymmetric(userSymmetricKeys.get(user),encryptedMessage,iv);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonObject convertedRequest= new Gson().fromJson(message, JsonObject.class);


        String requester = convertedRequest.get("userID").getAsString();
        int epoch = convertedRequest.get("Epoch").getAsInt();
        int[] coords = {0,0};
        if(reports.containsKey(epoch)) {
            HashMap<String, int[]> UsersAt = reports.get(epoch);
            if (UsersAt.containsKey(requester)) {
                coords = UsersAt.get(requester);
            }
        }
        JsonObject json = new JsonObject();
        json.addProperty("XCoord",coords[0]);
        json.addProperty("YCoord",coords[1]);

        //Encriptação


        String resp = null;
        IvParameterSpec iv = null;
        try {
            iv = Utils.generateIv();
            resp = Utils.encryptMessageSymmetric(userSymmetricKeys.get(requester),json.toString(),iv);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LocationStatus ls = LocationStatus.newBuilder().setMessage(resp).setIv(Base64.getEncoder()
                .encodeToString(iv.getIV())).build();
        responseObserver.onNext(ls);
        responseObserver.onCompleted();
    }


    public static class HA_Server extends HAProtocolGrpc.HAProtocolImplBase {
        @Override
        public void obtainLocationReport(hacontract.GetLocation request, StreamObserver<hacontract.LocationStatus> responseObserver) {
            String requester = request.getId();
            int epoch = request.getEp();
            int[] coords = {0,0};
            if(reports.containsKey(epoch)) {
                HashMap<String, int[]> UsersAt = reports.get(epoch);
                if (UsersAt.containsKey(requester)) {
                    coords = UsersAt.get(requester);
                }
            }
            hacontract.LocationStatus ls = hacontract.LocationStatus.newBuilder().setXCoord(coords[0]).setYCoord(coords[1]).build();
            responseObserver.onNext(ls);
            responseObserver.onCompleted();
        }

        @Override
        public void obtainUsersAtLocation(UserAtLocation request, StreamObserver<Users> responseObserver) {
            int epoch = request.getEpoch();
            int xCoords = request.getXCoord();
            int yCoords = request.getYCoord();
            List<String> users = new ArrayList<>();
            if(reports.containsKey(epoch)) {
                HashMap<String, int[]> UsersAt = reports.get(epoch);

                for(Map.Entry<String, int[]> entry : UsersAt.entrySet()){
                    String a = entry.getKey();
                    int[] coords = entry.getValue();
                    if(coords[0] == xCoords && coords[1] == yCoords){
                        users.add(a);
                    }
                }
            }
            Users u = Users.newBuilder().addAllIds(users).build();
            responseObserver.onNext(u);
            responseObserver.onCompleted();
        }
    }

    private static void saveReportsToFile() throws IOException {
        try(BufferedWriter csvWriter = new BufferedWriter(new FileWriter(REPORTS_FILE))){
            for (Map.Entry<Integer,HashMap<String,int[]>> entry : reports.entrySet()) {
                for(Map.Entry<String,int[]> entry1 : entry.getValue().entrySet()){
                    csvWriter.write(entry.getKey() + "," + entry1.getKey() + "," + entry1.getValue()[0] + "," + entry1.getValue()[1]);
                    csvWriter.newLine();

                }
            }
            csvWriter.flush();
            csvWriter.close();
        }
    }

    private static void readReportsFromFile() throws IOException {
        File f = new File(REPORTS_FILE);
        if(!f.exists() || f.isDirectory()) {
            return;
        }

        try(BufferedReader csvReader = new BufferedReader(new FileReader(REPORTS_FILE))){
            String line;
            int epoch = 0;
            int[] coords = null;
            String user = null;
            String[] splits = null;
            HashMap<String,int[]> map = new HashMap<>();

            while((line = csvReader.readLine()) != null){

                splits = line.split(",");
                epoch = Integer.parseInt(splits[0]);
                user = splits[1];
                coords[0] = Integer.parseInt(splits[2]);
                coords[1] = Integer.parseInt(splits[3]);

                if(reports.containsKey(epoch)){
                    map = reports.get(epoch);
                    map.put(user,coords);
                    reports.replace(epoch,map);
                }
                else{
                    map.put(user,coords);
                    reports.put(epoch,map);
                }
            }
        }

    }

    private static void saveKeysToFile() throws IOException {

        try(BufferedWriter csvWriter = new BufferedWriter(new FileWriter(SYMMETRICS_FILE))){
            for (Map.Entry<String,SecretKey> entry : userSymmetricKeys.entrySet()) {
                csvWriter.write(entry.getKey() + ',' + Base64.getEncoder().encodeToString(entry.getValue().getEncoded()));
                csvWriter.newLine();
            }
            csvWriter.close();
        }

    }

    private static void readKeysFromFile() throws IOException {
        File f = new File(SYMMETRICS_FILE);
        if(!f.exists() || f.isDirectory()) {
            return;
        }
        try(BufferedReader csvReader = new BufferedReader(new FileReader(SYMMETRICS_FILE))){
            String line;
            while((line = csvReader.readLine()) != null){
                userSymmetricKeys.put(line.split(",")[0],new SecretKeySpec(Base64.getDecoder().decode(line.split(",")[1]),0, 16, "AES"));
            }
        }

    }



    public static void main(String[] args) throws IOException {
        readUsers();
        int svcPort = Integer.parseInt(UsersMap.get("server").split(":")[1]);
        int svcPort_HA = svcPort + 50;
        Server svc = null;
        Server svc_HA = null;

        readKeysFromFile();
        readReportsFromFile();

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
            System.out.println("Server started, listening on " + svcPort_HA);


            svc.awaitTermination();
            svc.shutdown();

            svc_HA.awaitTermination();
            svc_HA.shutdown();


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
