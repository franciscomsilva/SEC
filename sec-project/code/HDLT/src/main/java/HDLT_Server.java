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

    private static String REPORTS_FILE = "files/location_reports.json";
    private static String SYMMETRICS_FILE = "files/symmetric_keys";
    private static String USERS_CONNECTION_FILE =  "files/users_connection.txt";

    private static Double BYZANTINE_RATIO = 0.5;
    private static int MIN_PROOFERS = 0;
    private static HashMap<String,String> UsersMap = new HashMap<>();
    private static JsonArray reports = new JsonArray();
    private static HashMap<String, SecretKey> userSymmetricKeys = new HashMap<String, SecretKey>();

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
        JsonObject convertedRequest = null;
        try {
            byte[] iv = Base64.getDecoder().decode(request.getIv());
            user = request.getUser();
            String encryptedMessage = request.getMessage();
            message = Utils.decryptMessageSymmetric(userSymmetricKeys.get(user),encryptedMessage,iv);
            convertedRequest= new Gson().fromJson(message, JsonObject.class);
        } catch (Exception e) {
            System.err.println("ERROR: Invalid key");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid key"))));
            return;
        }

        String requester = convertedRequest.get("userID").getAsString();

        if(!requester.equals(user)){
            System.err.println("ERROR: Invalid user");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid user"))));
            return;
        }

        int epoch = convertedRequest.get("currentEpoch").getAsInt();

        boolean flagRequest = false;
        /*RUNS THROUGH THE EPOCH OBJECTS WITH ALL THE REPORTS FOR THAT EPOCH*/
        for( JsonElement report_epoch : reports){
            if(report_epoch.getAsJsonObject().get("epoch").equals(epoch))
            {
                JsonArray reports = report_epoch.getAsJsonObject().get("reports").getAsJsonArray();
                for(JsonElement jsonElement : reports){
                    if(jsonElement.getAsJsonObject().get("user").equals(user)){
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
        JsonArray ab = convertedRequest.get("proofers").getAsJsonArray();

        Map<String, String> proofers = new HashMap<>();

        for (JsonElement o : ab){
            JsonObject obj = o.getAsJsonObject();
            System.out.println(o.toString());
            proofers.put(obj.get("userID").getAsString(),obj.get("digSIG").getAsString());
        }

        String verify = requester+","+epoch+","+xCoords+","+yCoords;

        int counter = 0;
        Boolean done = false;
        Double flag = 0.0;

        if(!proofers.isEmpty() && proofers.size() >= 3) {
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
                flag = Double.valueOf(counter)/Double.valueOf(proofers.size());
                if (flag < BYZANTINE_RATIO) {

                    /*CHECK IF JSON OBJECT FOR THAT EPOCH EXISTS*/
                    JsonObject epoch_object = null;
                    for( JsonElement report : reports){
                        if(report.getAsJsonObject().get("epoch").equals(epoch))
                        {
                            epoch_object = report.getAsJsonObject().get("epoch").getAsJsonObject();
                        }
                    }
                    JsonArray reports_array = null;

                    if(epoch_object == null){
                        epoch_object = new JsonObject();
                        epoch_object.addProperty("epoch",epoch);
                        reports_array = new JsonArray();
                        epoch_object.add("reports",reports_array);
                    }else{
                        reports_array = epoch_object.getAsJsonArray("reports");
                    }
                    JsonObject reportObject = new JsonObject();
                    reportObject.addProperty("user",user);
                    reportObject.addProperty("coordX",xCoords);
                    reportObject.addProperty("coordY",yCoords);
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
        JsonObject convertedRequest = null;
        try {
            byte[] iv = Base64.getDecoder().decode(request.getIv());
            String user = request.getUser();
            String encryptedMessage = request.getMessage();
            message = Utils.decryptMessageSymmetric(userSymmetricKeys.get(user),encryptedMessage,iv);
            convertedRequest= new Gson().fromJson(message, JsonObject.class);
        } catch (Exception e) {
            System.err.println("ERROR: Invalid key");
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid key"))));
            return;
        }

        String requester = convertedRequest.get("userID").getAsString();
        int epoch = convertedRequest.get("Epoch").getAsInt();
        int[] coords = {0,0};

        boolean flagRequest = true;
        for( JsonElement report_epoch : reports){
            if(report_epoch.getAsJsonObject().get("epoch").getAsInt() == (epoch))
            {
                JsonArray reports = report_epoch.getAsJsonObject().get("reports").getAsJsonArray();
                for(JsonElement jsonElement : reports){
                    if(jsonElement.getAsJsonObject().get("user").getAsString().equals(requester)){
                        coords[0] = jsonElement.getAsJsonObject().get("coordX").getAsInt();
                        coords[1] = jsonElement.getAsJsonObject().get("coordY").getAsInt();
                        flagRequest = false;
                        break;
                    }
                }
            }
        }

        if(flagRequest) {
            responseObserver.onError(new StatusException(Status.NOT_FOUND.withDescription("ERROR: No location report for that user in that epoch!")));
            return;
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
            //Decoding
            String message = null;
            JsonObject convertedRequest = null;
            try {
                byte[] iv = Base64.getDecoder().decode(request.getIv());
                String user = "user_ha";
                String encryptedMessage = request.getMessage();
                message = Utils.decryptMessageSymmetric(userSymmetricKeys.get(user),encryptedMessage,iv);
                convertedRequest= new Gson().fromJson(message, JsonObject.class);
            } catch (Exception e) {
                System.err.println("ERROR: Invalid key");
                responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid key"))));
                return;
            }
            String requester = convertedRequest.get("user").getAsString();
            int epoch = convertedRequest.get("epoch").getAsInt();
            int[] coords = {0, 0};

            boolean flagRequest = true;
            for (JsonElement report_epoch : reports) {
                if (report_epoch.getAsJsonObject().get("epoch").getAsInt() == (epoch)) {
                    JsonArray reports = report_epoch.getAsJsonObject().get("reports").getAsJsonArray();
                    for (JsonElement jsonElement : reports) {
                        if (jsonElement.getAsJsonObject().get("user").getAsString().equals(requester)) {
                            coords[0] = jsonElement.getAsJsonObject().get("coordX").getAsInt();
                            coords[1] = jsonElement.getAsJsonObject().get("coordY").getAsInt();
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
            String resp = null;
            IvParameterSpec iv = null;
            JsonObject coords_object = new JsonObject();
            coords_object.addProperty("xCoords",coords[0]);
            coords_object.addProperty("yCoords",coords[1]);
            try {
                iv = Utils.generateIv();
                resp = Utils.encryptMessageSymmetric(userSymmetricKeys.get("user_ha"),coords_object.toString(),iv);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            hacontract.LocationStatus ls = hacontract.LocationStatus.newBuilder().setMessage(resp).setIv(Base64.getEncoder()
                    .encodeToString(iv.getIV())).build();
            responseObserver.onNext(ls);
            responseObserver.onCompleted();
        }

        @Override
        public void obtainUsersAtLocation(UserAtLocation request, StreamObserver<Users> responseObserver) {
            //Decoding
            String message = null;
            JsonObject convertedRequest = null;
            try {
                byte[] iv = Base64.getDecoder().decode(request.getIv());
                String user = "user_ha";
                String encryptedMessage = request.getMessage();
                message = Utils.decryptMessageSymmetric(userSymmetricKeys.get(user),encryptedMessage,iv);
                convertedRequest= new Gson().fromJson(message, JsonObject.class);
            } catch (Exception e) {
                System.err.println("ERROR: Invalid key");
                responseObserver.onError(new StatusException((Status.ABORTED.withDescription("ERROR: Invalid key"))));
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
                            users.add(jsonElement.getAsJsonObject().get("user").getAsString());
                            flagRequest = false;
                        }
                    }
                }
            }

            if (flagRequest) {
                responseObserver.onError(new StatusException(Status.NOT_FOUND.withDescription("ERROR: No location report for those coordinates in that epoch!")));
                return;
            }
             StringBuilder sb = new StringBuilder();
            for(String user : users){
                sb.append(user + ";");
            }

            //Encriptação
            String resp = null;
            IvParameterSpec iv = null;
            try {
                iv = Utils.generateIv();
                resp = Utils.encryptMessageSymmetric(userSymmetricKeys.get("user_ha"),sb.toString(),iv);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Users u = Users.newBuilder().setIv(Base64.getEncoder()
                    .encodeToString(iv.getIV())).setMessage(resp).build();
            responseObserver.onNext(u);
            responseObserver.onCompleted();
        }

        @Override
        public void init(hacontract.InitMessage request, StreamObserver<hacontract.Key> responseObserver) {
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
                hacontract.Key symmetricKeyResponse = hacontract.Key.newBuilder().setKey(encryptedKey).build();
                responseObserver.onNext(symmetricKeyResponse);
                responseObserver.onCompleted();
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

    private static void saveKeysToFile() throws IOException {
        FileWriter fileWriter = new FileWriter(SYMMETRICS_FILE);
        try(BufferedWriter csvWriter = new BufferedWriter(fileWriter)){
            for (Map.Entry<String,SecretKey> entry : userSymmetricKeys.entrySet()) {
                csvWriter.write(entry.getKey() + ',' + Base64.getEncoder().encodeToString(entry.getValue().getEncoded()));
                csvWriter.newLine();
            }
            fileWriter.flush();
            csvWriter.flush();
            csvWriter.close();
            fileWriter.close();
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
