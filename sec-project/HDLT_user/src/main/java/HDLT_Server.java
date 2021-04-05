import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import hacontract.HAProtocolGrpc;
import hacontract.UserAtLocation;
import hacontract.Users;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import userserver.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Utils.*;

import static java.lang.Integer.parseInt;

public class HDLT_Server extends UserServerGrpc.UserServerImplBase {
    // Variaveis Globais

    static HashMap<String,String> UsersMap = new HashMap<>();

    static HashMap<Integer,HashMap<String,int[]>> reports = new HashMap<>();

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
    public void submitLocationReport(LocationReport request, StreamObserver<LocationResponse> responseObserver) {

        //Decoding
        String message = null;
        try {
            message = Utils.decryptMessage("keys/server.key",request.getMessage());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



        JsonObject convertedRequest= new Gson().fromJson(message, JsonObject.class);


        String requester = convertedRequest.get("userID").getAsString();
        int epoch = convertedRequest.get("currentEpoch").getAsInt();
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

        Boolean flag = true;
        Boolean done = false;

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
                        //flag = true;
                    } else {
                        System.out.println("Server Signature Not Verified!");
                        flag = false;
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (flag) {
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
        try {
            resp = Utils.encryptMessage("keys/"+requester,json.toString());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LocationResponse lr = LocationResponse.newBuilder().setMessage(resp).build();
        responseObserver.onNext(lr);
        responseObserver.onCompleted();
    }

    @Override
    public void obtainLocationReport(GetLocation request, StreamObserver<LocationStatus> responseObserver) {
        String message = request.getMessage();

        //Decoding

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

        //Encrupt
        String resp = json.toString();
        LocationStatus ls = LocationStatus.newBuilder().setMessage(resp).build();
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

    public static void main(String[] args){
        readUsers();
        int svcPort = Integer.parseInt(UsersMap.get("server").split(":")[1]);
        int svcPort_HA = svcPort + 50;
        Server svc = null;
        Server svc_HA = null;


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
