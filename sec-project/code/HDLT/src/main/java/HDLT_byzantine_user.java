import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import userprotocol.*;
import userprotocol.UserProtocolGrpc.UserProtocolImplBase;
import userserver.*;


import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Utils.Utils;
import userserver.Key;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import static java.lang.Integer.parseInt;

public class HDLT_byzantine_user extends UserProtocolImplBase{

    /*GLOBAL VARIABLES*/
    private static UserProtocolGrpc.UserProtocolBlockingStub blockingStub;
    private static UserServerGrpc.UserServerBlockingStub bStub;
    private static String USERS_CONNECTION_FILE = "files/users_connection.txt";
    private static String MAP_GRID_FILE = "files/map_grid.txt";

    private static HashMap<String,String> UsersMap = new HashMap<>();

    private static ConcurrentHashMap<String,String> proofers = new ConcurrentHashMap<>();

    private static SecretKey symmetricKey;

    private static String user;
    private static int x;
    private static int y;

    private static int currentEpoch;

    private static int operation_mode = 0;


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

    public static void connectToUser(String phrase){
        //Split da phrase
        String svcIP = phrase.split(":")[0];
        int svcPort = Integer.parseInt(phrase.split(":")[1]);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(svcIP, svcPort)
                .usePlaintext()
                .build();
        blockingStub = UserProtocolGrpc.newBlockingStub(channel);
        //noBlockStub = UserProtocolGrpc.newStub(channel);
    }

    public static  HashMap<String,double []> readMap(int epoch) throws IOException, CsvValidationException {
        List<String[]> UsersInMap = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(MAP_GRID_FILE))) {
            String[] lineInArray;
            while ((lineInArray = reader.readNext()) != null) {
                if(parseInt(lineInArray[1]) == epoch ) {
                    if(lineInArray[0].equals(user)){
                        x = parseInt(lineInArray[2]);
                        y = parseInt(lineInArray[3]);

                    }else{
                        UsersInMap.add(lineInArray);
                    }
                }
            }
        }

        HashMap<String,double []> RadiusUsers = new HashMap<>();
        for (String[] line: UsersInMap) {
            int x1 = Integer.parseInt(line[2]);
            int y1 = Integer.parseInt(line[3]);
            double v = Math.sqrt(Math.pow((double)Math.abs(x1-x),2) + Math.pow((double)Math.abs(y1-y),2));
            if(v < 2.0){
                double [] a = {v,x1,y1};
                RadiusUsers.put(line[0],a);
            }
        }
        return RadiusUsers;
    }

    public static void requestProof(int epoch) throws InterruptedException {

        HashMap<String,double []> RadiusUsers = new HashMap<>();

        try {
            RadiusUsers = readMap(epoch);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
        if(!RadiusUsers.isEmpty()){
            ExecutorService executorService = Executors.newFixedThreadPool(RadiusUsers.size());

            for (Map.Entry<String,double []> entry : RadiusUsers.entrySet()){
                Runnable run = () -> {
                    connectToUser(UsersMap.get(entry.getKey()));
                    try {
                        LocationRequest locationRequest = LocationRequest.newBuilder().setId(user).setXCoord(x).setYCoord(y).build();
                        Proof proof = blockingStub.requestLocationProof(locationRequest);
                        proofers.put(proof.getId(),proof.getDigSig());
                    }catch (Exception e){
                        System.err.println(e.getMessage());
                    }
                };
                executorService.execute(run);
                Thread.sleep(1000);
            }

        }
        else{
            System.out.println("Don't have proofers");
        }
    }

    @Override
    public void requestLocationProof(LocationRequest request, StreamObserver<Proof> responseObserver)  {

        if(operation_mode == 0){
            //Geração da Proof
            String id = request.getId();
            int xCoord = request.getXCoord();
            int yCoord = request.getYCoord();
            int epoch = request.getEpoch();

            try{
                HashMap<String,double []> users = readMap(epoch);
                // Verificação se o requisitante está perto deste user
                if(users.containsKey(id)){
                    double [] coords = users.get(id);
                    if (xCoord == coords[1] && yCoord == coords[2]){
                        String msg = id +","+epoch+","+xCoord+","+yCoord;

                        /*READS PRIVATE  KEY TO SIGN */
                        byte[] privKeyBytes = Files.readAllBytes(Paths.get("keys/"+user+".key"));
                        PKCS8EncodedKeySpec specPriv = new PKCS8EncodedKeySpec(privKeyBytes);
                        KeyFactory kf = KeyFactory.getInstance("RSA");
                        PrivateKey privateKey = kf.generatePrivate(specPriv);

                        byte[] digitalSignatureToSent = Utils.signMessage(privateKey,msg);

                        Proof pf = Proof.newBuilder().setId(user).setDigSig(new String(Base64.getEncoder().encode(digitalSignatureToSent))).build();

                        responseObserver.onNext(pf);
                        responseObserver.onCompleted();
                    }else{
                        /*USER NOT IN THE PROVIDED POSITION*/
                        throw new Exception("ERROR: User not in the provided position");
                    }
                }else{
                    /*USER NOT IN MAP RANGE*/
                    throw new Exception("ERROR: User not in map range " + user);
                }

            } catch (Exception e) {
                responseObserver.onError(new StatusException((Status.ABORTED.withDescription(e.getMessage()))));
            }
        }else if(operation_mode == 1){
            /*SLEEPS THE THREAD TO TIMEOUT THE REQUEST*/
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else if(operation_mode == 2){
            responseObserver.onError(new StatusException(Status.CANCELLED.withDescription("Rejected")));
        }else if(operation_mode == 3){
            //Geração da Proof
            String id = request.getId();
            int xCoord = request.getXCoord();
            int yCoord = request.getYCoord();
            int epoch = request.getEpoch();

            try{
                HashMap<String,double []> users = readMap(epoch);
                // Verificação se o requisitante está perto deste user
                if(users.containsKey(id)){
                    double [] coords = users.get(id);
                    if (xCoord == coords[1] && yCoord == coords[2]){
                        String msg = id +","+epoch+","+5+","+5;

                        /*READS PRIVATE  KEY TO SIGN */
                        byte[] privKeyBytes = Files.readAllBytes(Paths.get("keys/"+user+".key"));
                        PKCS8EncodedKeySpec specPriv = new PKCS8EncodedKeySpec(privKeyBytes);
                        KeyFactory kf = KeyFactory.getInstance("RSA");
                        PrivateKey privateKey = kf.generatePrivate(specPriv);

                        byte[] digitalSignatureToSent = Utils.signMessage(privateKey,msg);

                        Proof pf = Proof.newBuilder().setId(user).setDigSig(new String(Base64.getEncoder().encode(digitalSignatureToSent))).build();

                        responseObserver.onNext(pf);
                        responseObserver.onCompleted();
                    }else{
                        /*USER NOT IN THE PROVIDED POSITION*/
                        throw new Exception("ERROR: User not in the provided position");
                    }
                }else{
                    /*USER NOT IN MAP RANGE*/
                    throw new Exception("ERROR: User not in map range " + user);
                }

            } catch (Exception e) {
                responseObserver.onError(new StatusException((Status.ABORTED.withDescription(e.getMessage()))));
            }
        }

    }

    public static void SubmitLocation() {

        JsonArray proofersArray = new JsonArray();


        if (!proofers.isEmpty()) {
            for (Map.Entry<String, String> entry : proofers.entrySet()) {
                JsonObject o = new JsonObject();
                o.addProperty("userID", entry.getKey());
                o.addProperty("digSIG", entry.getValue());

                proofersArray.add(o);
            }
        }

        JsonObject json = new JsonObject();
        json.addProperty("userID", user);
        json.addProperty("currentEpoch", currentEpoch);
        json.addProperty("xCoord", x);
        json.addProperty("yCoord", y);
        json.add("proofers", proofersArray);


        System.out.println(json.toString());

        // Encriptação
        String encryptedMessage = null;
        IvParameterSpec ivSpec = null;
        try {
            ivSpec = Utils.generateIv();
            encryptedMessage = Utils.encryptMessageSymmetric(symmetricKey, json.toString(), ivSpec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LocationReport lr = LocationReport.newBuilder().setMessage(encryptedMessage).setIv(Base64.getEncoder()
                .encodeToString(ivSpec.getIV())).setUser(user).build();
        LocationResponse resp = null;

        try {
            resp = bStub.submitLocationReport(lr);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }
    }

    public static void SubmitLocationAttack1(){

        JsonArray proofersArray = new JsonArray();

        HashMap<String,String> emptyProofers = new HashMap();

        for (Map.Entry<String, String> entry : emptyProofers.entrySet()) {
            JsonObject o = new JsonObject();
            o.addProperty("userID",entry.getKey());
            o.addProperty("digSIG",entry.getValue());

            proofersArray.add(o);
        }


        JsonObject json = new JsonObject();
        json.addProperty("userID", user);
        json.addProperty("currentEpoch",currentEpoch);
        json.addProperty("xCoord",x);
        json.addProperty("yCoord",y);
        json.add("proofers",proofersArray);


        System.out.println(json.toString());

        // Encriptação
        String encryptedMessage = null;
        IvParameterSpec ivSpec = null;
        try {
            ivSpec = Utils.generateIv();
            encryptedMessage = Utils.encryptMessageSymmetric(symmetricKey,json.toString(),ivSpec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LocationReport lr = LocationReport.newBuilder().setMessage(encryptedMessage).setIv(Base64.getEncoder()
                .encodeToString(ivSpec.getIV())).setUser(user).build();
        LocationResponse resp = null;

        try{
            resp = bStub.submitLocationReport(lr);
        }catch(Exception e){
            System.out.println(e.getMessage());
            return;
        }

        // Desencriptar
        encryptedMessage = resp.getMessage();
        byte[] iv =Base64.getDecoder().decode(resp.getIv());
        String decryptedMessage = null;
        try {
            decryptedMessage = Utils.decryptMessageSymmetric(symmetricKey,encryptedMessage,iv);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonObject convertedResponse = new Gson().fromJson(decryptedMessage, JsonObject.class);

        Boolean bool = convertedResponse.get("Done").getAsBoolean();

        if(bool){
            proofers.clear();
        }else{
            System.out.println("Submittion failed!");
        }
    }

    public static void SubmitLocationDifferentUserID(){

        JsonArray proofersArray = new JsonArray();

        System.out.println(proofersArray);
        if(!proofers.isEmpty()){
            for (Map.Entry<String, String> entry : proofers.entrySet()) {
                JsonObject o = new JsonObject();
                o.addProperty("userID",entry.getKey());
                o.addProperty("digSIG",entry.getValue());

                proofersArray.add(o);
            }
        }

        JsonObject json = new JsonObject();
        json.addProperty("userID", "u1");
        json.addProperty("currentEpoch",currentEpoch);
        json.addProperty("xCoord",x);
        json.addProperty("yCoord",y);
        json.add("proofers",proofersArray);


        System.out.println(json.toString());

        // Encriptação
        String encryptedMessage = null;
        IvParameterSpec ivSpec = null;
        try {
            ivSpec = Utils.generateIv();
            encryptedMessage = Utils.encryptMessageSymmetric(symmetricKey,json.toString(),ivSpec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LocationReport lr = LocationReport.newBuilder().setMessage(encryptedMessage).setIv(Base64.getEncoder()
                .encodeToString(ivSpec.getIV())).setUser(user).build();
        LocationResponse resp = null;

        try{
            resp = bStub.submitLocationReport(lr);
        }catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }

        // Desencriptar
        encryptedMessage = resp.getMessage();
        byte[] iv =Base64.getDecoder().decode(resp.getIv());
        String decryptedMessage = null;
        try {
            decryptedMessage = Utils.decryptMessageSymmetric(symmetricKey,encryptedMessage,iv);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonObject convertedResponse = new Gson().fromJson(decryptedMessage, JsonObject.class);

        Boolean bool = convertedResponse.get("Done").getAsBoolean();

        if(bool){
            proofers.clear();
        }else{
            System.out.println("Submittion failed!");
        }
    }

    public static void SubmitLocationDifferentUserIDRequester(){

        JsonArray proofersArray = new JsonArray();

        System.out.println(proofersArray);
        if(!proofers.isEmpty()){
            for (Map.Entry<String, String> entry : proofers.entrySet()) {
                JsonObject o = new JsonObject();
                o.addProperty("userID",entry.getKey());
                o.addProperty("digSIG",entry.getValue());

                proofersArray.add(o);
            }
        }

        JsonObject json = new JsonObject();
        json.addProperty("userID", user);
        json.addProperty("currentEpoch",currentEpoch);
        json.addProperty("xCoord",x);
        json.addProperty("yCoord",y);
        json.add("proofers",proofersArray);


        System.out.println(json.toString());

        // Encriptação
        String encryptedMessage = null;
        IvParameterSpec ivSpec = null;
        try {
            ivSpec = Utils.generateIv();
            encryptedMessage = Utils.encryptMessageSymmetric(symmetricKey,json.toString(),ivSpec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LocationReport lr = LocationReport.newBuilder().setMessage(encryptedMessage).setIv(Base64.getEncoder()
                .encodeToString(ivSpec.getIV())).setUser("u1").build();
        LocationResponse resp = null;

        try{
            resp = bStub.submitLocationReport(lr);
        }catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }

        // Desencriptar
        encryptedMessage = resp.getMessage();
        byte[] iv =Base64.getDecoder().decode(resp.getIv());
        String decryptedMessage = null;
        try {
            decryptedMessage = Utils.decryptMessageSymmetric(symmetricKey,encryptedMessage,iv);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonObject convertedResponse = new Gson().fromJson(decryptedMessage, JsonObject.class);

        Boolean bool = convertedResponse.get("Done").getAsBoolean();

        if(bool){
            proofers.clear();
        }else{
            System.out.println("Submittion failed!");
        }
    }

    public static void SubmitLocationWithAlteredDigSig(){

        JsonArray proofersArray = new JsonArray();

        /*CHANGES FIRST CHAR OF DIGITAL SIGNATURE*/

        Map.Entry<String,String> firstEntry = proofers.entrySet().iterator().next();
        byte[] byteArray = firstEntry.getValue().getBytes(StandardCharsets.UTF_8);
        byteArray[0] = 'f';
        firstEntry.setValue(new String(byteArray));
        proofers.replace(firstEntry.getKey(),firstEntry.getValue());

        if(!proofers.isEmpty()){
            for (Map.Entry<String, String> entry : proofers.entrySet()) {
                JsonObject o = new JsonObject();
                o.addProperty("userID",entry.getKey());
                o.addProperty("digSIG",entry.getValue());

                proofersArray.add(o);
            }
        }

        JsonObject json = new JsonObject();
        json.addProperty("userID", user);
        json.addProperty("currentEpoch",currentEpoch);
        json.addProperty("xCoord",x);
        json.addProperty("yCoord",y);
        json.add("proofers",proofersArray);


        System.out.println(json.toString());

        // Encriptação
        String encryptedMessage = null;
        IvParameterSpec ivSpec = null;
        try {
            ivSpec = Utils.generateIv();
            encryptedMessage = Utils.encryptMessageSymmetric(symmetricKey,json.toString(),ivSpec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LocationReport lr = LocationReport.newBuilder().setMessage(encryptedMessage).setIv(Base64.getEncoder()
                .encodeToString(ivSpec.getIV())).setUser(user).build();
        LocationResponse resp = null;

        try{
            resp = bStub.submitLocationReport(lr);
        }catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }

        // Desencriptar
        encryptedMessage = resp.getMessage();
        byte[] iv =Base64.getDecoder().decode(resp.getIv());
        String decryptedMessage = null;
        try {
            decryptedMessage = Utils.decryptMessageSymmetric(symmetricKey,encryptedMessage,iv);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonObject convertedResponse = new Gson().fromJson(decryptedMessage, JsonObject.class);

        Boolean bool = convertedResponse.get("Done").getAsBoolean();

        if(bool){
            proofers.clear();
        }else{
            System.out.println("Submittion failed!");
        }
    }

    public static void ObtainLocation(int epoch){

        JsonObject json = new JsonObject();
        json.addProperty("userID", user);
        json.addProperty("Epoch",epoch);

        // Encriptação

        String encryptedMessage = null;
        IvParameterSpec ivSpec = null;
        try {
            ivSpec = Utils.generateIv();
            encryptedMessage = Utils.encryptMessageSymmetric(symmetricKey,json.toString(),ivSpec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        GetLocation gl = GetLocation.newBuilder().setMessage(encryptedMessage).setUser(user).setIv(Base64.getEncoder()
                .encodeToString(ivSpec.getIV())).build();
        LocationStatus resp = bStub.obtainLocationReport(gl);

        encryptedMessage = resp.getMessage();
        byte[] iv =Base64.getDecoder().decode(resp.getIv());
        String decryptedMessage = null;
        try {
            decryptedMessage = Utils.decryptMessageSymmetric(symmetricKey,encryptedMessage,iv);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonObject convertedResponse = new Gson().fromJson(decryptedMessage, JsonObject.class);

        int XCoord = convertedResponse.get("XCoord").getAsInt();
        int YCoord = convertedResponse.get("YCoord").getAsInt();

        System.out.println("User "+user+" are in ("+ XCoord+","+YCoord+") at epoch "+epoch);

    }

    public static void main(String[] args) throws GeneralSecurityException, IOException {

        //Ler ficheiro com os endereços dos utilizadores
        readUsers();

        //CHECK SIZE OF ARGUMENTS
        if(args.length < 1){
            System.err.println("ERROR:Wrong number of arguments");
            return;
        }

        //READ COMMAND LINE ARGUMENTS
        user = args[0];


        int svcPort = Integer.parseInt(UsersMap.get(user).split(":")[1]);

        //Conexão com o servidor
        String phrase = UsersMap.get("server");
        String svcIP = phrase.split(":")[0];
        int sPort = Integer.parseInt(phrase.split(":")[1]);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(svcIP, sPort)
                .usePlaintext()
                .build();
        bStub = UserServerGrpc.newBlockingStub(channel);
        InitMessage initMessage = InitMessage.newBuilder().setUser(user).build();
        Key responseKey = null;
        try{
            responseKey = bStub.init(initMessage);
        }catch(Exception e){
            System.err.println("ERROR: Server connection failed!");
            return;
        }
        String base64SymmetricKey = responseKey.getKey();
        byte[] symmetricKeyBytes = Utils.decryptMessageAssymetric("keys/" + user + ".key",base64SymmetricKey);
        symmetricKey = Utils.generateSymmetricKey(symmetricKeyBytes);
        //Instancia de Servidor para os Clients
        Server svc = null;
        try {
            svc = ServerBuilder
                    .forPort(svcPort)
                    .addService(new HDLT_byzantine_user())
                    .build();

            svc.start();

            System.out.println("Byzantine User Server started, listening on " + svcPort);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //Ler um Script com os requests de cada utilizador
        BufferedReader reader;
        try {
            //reader = new BufferedReader(new FileReader(user + ".txt"));
            //String line = reader.readLine();
            //while (line != null) {
            Scanner scanner = new Scanner(System.in);
            while(true){
                System.out.print("Epoch "+currentEpoch+"> ");
                String line = scanner.nextLine();
                String cmd = line.split(" ")[0];
                switch (cmd) {
                    case "Attack1":
                    case "a1":
                        System.out.println("ATTACK: Sending a empty submition to a server");
                        SubmitLocationAttack1();   
                        System.out.println("INFO: Attack 1 finished!");
                        break;
                    case "Attack2":
                    case "a2":
                        System.out.println("ATTACK: Dropping a proofer request from another user");
                        System.out.println("INFO: Attack 2 finished!");
                        operation_mode = 1;
                        break;
                    case "Attack3":
                    case "a3":
                        System.out.println("ATTACK: Requesting proofers and sending location report with fake userID");
                        System.out.println("INFO: Attack 3 finished!");
                        requestProof(currentEpoch);
                        SubmitLocationDifferentUserID();
                        break;
                    case "Attack4":
                    case "a4":
                        System.out.println("ATTACK: Requesting proofers and sending location report with fake requester userID");
                        System.out.println("INFO: Attack 4 finished!");
                        requestProof(currentEpoch);
                        SubmitLocationDifferentUserIDRequester();
                        break;
                    case "Attack 5":
                    case "a5":
                        System.out.println("ATTACK: Requestion proofers and sending location report with altered digital signature");
                        System.out.println("INFO: Attack 5 finished!");
                        requestProof(currentEpoch);
                        SubmitLocationWithAlteredDigSig();
                        break;
                    case "Attack 6":
                    case "a6":
                        System.out.println("ATTACK: Requesting proofers and sending replaying submission");
                        System.out.println("INFO: Attack 6 finished!");
                        requestProof(currentEpoch);
                        SubmitLocation();
                        SubmitLocation();
                        break;
                    case "Attack 7":
                    case "a7":
                        System.out.println("ATTACK: Rejecting a proofer request from another user");
                        System.out.println("INFO: Attack 7 finished!");
                        operation_mode = 2;
                        break;
                    case "Attack 8":
                    case "a8":
                        System.out.println("ATTACK: Sending a proofer request to another user with spoofed location");
                        System.out.println("INFO: Attack 8 finished!");
                        operation_mode = 3;
                        break;
                    case "Reset Operation Mode":
                    case "reset":
                        System.out.println("INFO: Resetting operation mode");
                        operation_mode = 0;
                        break;
                    case "Epoch":
                    case "e":
                        currentEpoch = Integer.parseInt(line.split(" ")[1]);
                        System.out.println("Setting time to Epoch " + currentEpoch);
                        break;
                    default:
                        System.out.println("ERROR: Incorrect command");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        svc.shutdown();
    }
}



