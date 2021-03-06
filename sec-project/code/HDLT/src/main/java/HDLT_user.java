import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import userprotocol.*;
import userprotocol.UserProtocolGrpc.UserProtocolImplBase;
import userserver.*;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;


import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import Utils.Utils;
import userserver.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import static java.lang.Integer.parseInt;

public class HDLT_user extends UserProtocolImplBase{

    /*GLOBAL VARIABLES*/
    private static UserProtocolGrpc.UserProtocolBlockingStub blockingStub;
    private static UserServerGrpc.UserServerBlockingStub bStub;
    private static String USERS_CONNECTION_FILE = "files/users_connection.txt";
    private static String MAP_GRID_FILE = "files/map_grid.txt";
    private static int NUMBER_SERVERS = 4;
    private static int BYZANTINE_SERVERS = 1;
    private static HashMap<String,Integer> counters = new HashMap<>();

    private static HashMap<String,String> UsersMap = new HashMap<>();

    private static ConcurrentHashMap<String,String> proofers = new ConcurrentHashMap<>();

    private static HashMap<Integer, SecretKey> symmetricKeys = new HashMap<>();

    private static String user;
    private static int x;
    private static int y;
    private static String keystore_password;

    private static int currentEpoch;


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
        ManagedChannel channel;
        if(blockingStub != null){
            channel = (ManagedChannel)blockingStub.getChannel();
            channel.shutdownNow();
        }
        channel = ManagedChannelBuilder.forAddress(svcIP, svcPort)
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
            if(v <= 2.0){
                double [] a = {v,x1,y1};
                RadiusUsers.put(line[0],a);
            }
        }
        return RadiusUsers;
    }
    /*Requests location proofs to other users nearby*/
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
                        LocationRequest locationRequest = LocationRequest.newBuilder().setId(user).setXCoord(x).setYCoord(y).setEpoch(epoch).build();
                        Proof proof = blockingStub.requestLocationProof(locationRequest);
                        proofers.put(proof.getId(),proof.getDigSig());
                    }catch (Exception e){
                        System.err.println(e.getMessage());
                    }
                };
                executorService.execute(run);
                Thread.sleep(100);
                if(executorService.awaitTermination(100, TimeUnit.MILLISECONDS)){
                    executorService.shutdownNow();
                }
            }

        }
        else{
            System.out.println("Don't have proofers");
        }
    }

    /*Receives a request to proof a users' location and responds*/
    @Override
    public void requestLocationProof(LocationRequest request, StreamObserver<Proof> responseObserver)  {
        //Gera????o da Proof
        String id = request.getId();
        int xCoord = request.getXCoord();
        int yCoord = request.getYCoord();
        int epoch = request.getEpoch();

        try{
            HashMap<String,double []> users = readMap(epoch);
            // Verifica????o se o requisitante est?? perto deste user
            if(users.containsKey(id)){
                double [] coords = users.get(id);
                if (xCoord == coords[1] && yCoord == coords[2]){
                    String msg = id +","+epoch+","+xCoord+","+yCoord;

                    String digSig = signMessage(msg);
                    Proof pf = Proof.newBuilder().setId(user).setDigSig(digSig).build();

                    responseObserver.onNext(pf);
                    responseObserver.onCompleted();
                }else{
                    /*USER NOT IN THE PROVIDED POSITION*/
                    throw new Exception("ERROR: User not in the provided position");
                }
            }else{
                /*USER NOT IN MAP RANGE*/
                throw new Exception("ERROR: User not in map range" + user);
            }

        } catch (Exception e) {
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription(e.getMessage()))));
        }
    }

    public static void init(int server) {
        Random r = new Random(System.currentTimeMillis());
        int counter = 0;
        String digSig = null, message = null;

        /*INITIALIZES COUNTER*/
        counter = r.nextInt();
        if(counter < 0)
            counter = counter + 2*counter;

        counters.put("server" + server, counter);
        message = user + "," + counter;
        try {
            digSig = signMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        InitMessage initMessage = InitMessage.newBuilder().setUser(user).setCounter(counter).setDigSig(digSig).build();
        Key responseKey = null;

        responseKey = bStub.init(initMessage);

        String base64SymmetricKey = responseKey.getKey();
        int c = responseKey.getCounter();

        if(c <= counter){
            System.err.println("ERROR: Wrong message counter received!");
            return;
        }

        counters.put("server"+server, c);

        byte[] symmetricKeyBytes = new byte[0];
        try {
            symmetricKeyBytes = Utils.decryptMessageAssymetric("keystores/keystore_" + user + ".keystore",keystore_password,base64SymmetricKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!verifyMessage("server"+server, base64SymmetricKey+","+c, responseKey.getDigSig())){
            System.err.println("ERROR: Message not Verified");
            return;
        }
        symmetricKeys.put(server,Utils.generateSymmetricKey(symmetricKeyBytes));
    }

    public static void SubmitLocation(ArrayList<Integer> servers) throws InterruptedException {

        JsonArray proofersArray = new JsonArray();

        if(!proofers.isEmpty()){
            for (Map.Entry<String, String> entry : proofers.entrySet()) {
               JsonObject o = new JsonObject();
               o.addProperty("userID",entry.getKey());
               o.addProperty("digSIG",entry.getValue());

               proofersArray.add(o);
            }
        }


        JsonObject json = new JsonObject();
        json.addProperty("user", user);
        json.addProperty("epoch",currentEpoch);
        json.addProperty("xCoord",x);
        json.addProperty("yCoord",y);
        json.add("proofers",proofersArray);

        /*THIS DIGITAL SIGNATURE ONLY MAINTAINS THE INTEGRITY OF THE DATA ITSELF, WITHOUT THE COUNTER, BASICALLY ITS A WRITER'S DIG SIG*/
        String dataDigSig = null;
        try {
            dataDigSig =  signMessage(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        json.addProperty("writerDigSig",dataDigSig);



        AtomicInteger cAck = new AtomicInteger(0);
        ExecutorService executorService = Executors.newFixedThreadPool(servers.size());
        for (int i : servers) {
            Runnable run = () -> {
                changeServer(i);
                // Encripta????o
                String encryptedMessage = null;
                IvParameterSpec ivSpec = null;
                String digSig = null;
                int counter = counters.get("server" + i) + 1;
                counters.replace("server" + i, counter);

                JsonObject jsonWithCounter = json;
                jsonWithCounter.addProperty("counter", counter);


                try {
                    ivSpec = Utils.generateIv();
                    encryptedMessage = Utils.encryptMessageSymmetric(symmetricKeys.get(i), jsonWithCounter.toString(), ivSpec);
                    digSig = signMessage(jsonWithCounter.toString());
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                LocationReport lr = LocationReport.newBuilder().setMessage(encryptedMessage).setIv(Base64.getEncoder()
                        .encodeToString(ivSpec.getIV())).setDigSig(digSig).setUser(user).build();
                LocationResponse resp = null;

                try {
                    resp = bStub.submitLocationReport(lr);
                } catch (Exception e) {
                    if (e.getMessage().contains("RESOURCE_EXHAUSTED") || e.getMessage().contains("NOT_FOUND")) {
                        init(i);
                        try {
                            SubmitLocation(new ArrayList<>(i));
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                        //return;
                    }
                    System.err.println("ERROR: "+ e.getMessage());
                    return;
                }

                // Desencriptar
                encryptedMessage = resp.getMessage();
                byte[] iv = Base64.getDecoder().decode(resp.getIv());
                String decryptedMessage = null;
                try {
                    decryptedMessage = Utils.decryptMessageSymmetric(symmetricKeys.get(i), encryptedMessage, iv);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                JsonObject convertedResponse = new Gson().fromJson(decryptedMessage, JsonObject.class);

                Boolean bool = convertedResponse.get("Done").getAsBoolean();
                counter = convertedResponse.get("counter").getAsInt();
                if ( counter <= counters.get("server" + i)) {
                    System.err.println("ERROR: Wrong message received");
                    bool = false;
                }
                counters.replace("server"+i,counter);
                if (!verifyMessage("server" + i, convertedResponse.toString(), resp.getDigSig())) {
                    System.err.println("ERROR: Message not Verified");
                    bool = false;
                }
                if (bool) {
                    proofers.clear();
                    cAck.addAndGet(1);
                } else {
                    System.err.println("Submition failed!");
                }
            };
            executorService.execute(run);
            Thread.sleep(100);
            if(executorService.awaitTermination(100, TimeUnit.MILLISECONDS)){
                executorService.shutdownNow();
            }
        }
        //verification
        if ((double) cAck.get() > ((double) NUMBER_SERVERS + (double) BYZANTINE_SERVERS) / 2.0) {
            System.out.println("Submit Location operation was successfully executed");
        }
    }

    public static boolean verifyMessage(String node, String message, String digSig) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");

            byte[] publicKeyBytes = new byte[0];

            publicKeyBytes = Files.readAllBytes(Paths.get("keys/" + node));
            X509EncodedKeySpec specPublic = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = kf.generatePublic(specPublic);

            if (Utils.verifySignature(message, digSig, publicKey)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void ObtainLocation(ArrayList<Integer> servers, int epoch) throws NoSuchAlgorithmException, InterruptedException {

        JsonObject json = new JsonObject();
        json.addProperty("userID", user);
        json.addProperty("Epoch",epoch);

        // Proof-of-Work
        final String[] powData = computePoW(json.toString());

        ArrayList<JsonObject> serverResponses = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(servers.size());
        for (int i : servers) {
            Runnable run = () -> {
                changeServer(i);
                String encryptedMessage = null;
                IvParameterSpec ivSpec = null;
                LocationStatus resp = null;
                String digSig = null;
                int counter = counters.get("server" + i) + 1;
                counters.replace("server" + i, counter);

                JsonObject json2 = json;
                json2.addProperty("counter", counter);
                json2.addProperty("nounce", powData[0]);
                json2.addProperty("pow", powData[1]);

                try {
                    ivSpec = Utils.generateIv();
                    encryptedMessage = Utils.encryptMessageSymmetric(symmetricKeys.get(i), json2.toString(), ivSpec);
                    digSig = signMessage(json2.toString());
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                GetLocation gl = GetLocation.newBuilder().setMessage(encryptedMessage).setUser(user).setIv(Base64.getEncoder()
                        .encodeToString(ivSpec.getIV())).setDigSig(digSig).build();
                try {
                    resp = bStub.obtainLocationReport(gl);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    
                    if (e.getMessage().contains("RESOURCE_EXHAUSTED") || e.getMessage().contains("NOT_FOUND")) {
                        init(i);
                        try {
                            ObtainLocation(new ArrayList<>(i), epoch);
                        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                            noSuchAlgorithmException.printStackTrace();
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }

                    }
                    return;
                }

                encryptedMessage = resp.getMessage();
                byte[] iv = Base64.getDecoder().decode(resp.getIv());
                String decryptedMessage = null;
                try {
                    decryptedMessage = Utils.decryptMessageSymmetric(symmetricKeys.get(i), encryptedMessage, iv);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JsonObject convertedResponse = new Gson().fromJson(decryptedMessage, JsonObject.class);

                if (!verifyMessage("server" + i, convertedResponse.toString(), resp.getDigSig())) {
                    System.err.println("ERROR: Message not Verified");
                    return;
                }
                counter = convertedResponse.get("counter").getAsInt();
                if (counter <= counters.get("server" + i)) {
                    System.err.println("ERROR: Wrong message received");
                    return;
                }
                counters.replace("server"+i,counter);
                convertedResponse.remove("counter");
                String writerDigSig = convertedResponse.get("writerDigSig").getAsString();
                convertedResponse.remove("writerDigSig");
                if (verifyMessage(user, convertedResponse.toString(), writerDigSig)) {
                    convertedResponse.addProperty("writerDigSig", writerDigSig);
                    serverResponses.add(convertedResponse);
                }
            };
            executorService.execute(run);
            Thread.sleep(100);
            if(executorService.awaitTermination(100, TimeUnit.MILLISECONDS)){
                executorService.shutdownNow();
            }
        }

        /*VERIFIES RECEIVED MESSAGES FROM SERVERS*/
        HashMap<JsonObject,Integer> response_counter = new HashMap<>();
        int counter_responses = 0;
        JsonObject finalResponse = new JsonObject();
        /*VERIFIES RECEIVED MESSAGES FROM SERVERS*/
        if ((double) serverResponses.size() > ((double) NUMBER_SERVERS + (double) BYZANTINE_SERVERS) / 2.0) {
            for (JsonObject jsonObj : serverResponses) {
                if(response_counter.containsKey(jsonObj)){
                    counter_responses = response_counter.get(jsonObj);
                    response_counter.put(jsonObj,counter_responses+1);
                }else{
                    response_counter.put(jsonObj,1);
                }
            }
            int max = 0;
            for (Map.Entry<JsonObject,Integer> entry : response_counter.entrySet()) {
                if(entry.getValue() > max){
                    max = entry.getValue();
                    finalResponse = entry.getKey();
                }
            }

        }else{
            System.err.println("ERROR: Wrong number of servers responded");
            return;
        }
        System.out.println(finalResponse.toString());

        /*WRITE-BACK SERVER: CLIENT WRITES EXACTLY WHAT IT READ*/
        for (int i : servers) {
            JsonObject response = finalResponse;
            Runnable run = () -> {
                changeServer(i);
                int counter = counters.get("server" + i) + 1;
                counters.put("server" + i, counter);
                response.addProperty("counter",counter);

                /*SENDS THE PREVIOUSLY RECEIVED SERVER JSON*/
                String encryptedMessage = null;
                IvParameterSpec ivSpec = null;
                String digSig = null;
                try {
                    ivSpec = Utils.generateIv();
                    encryptedMessage = Utils.encryptMessageSymmetric(symmetricKeys.get(i), response.toString(),ivSpec);
                    digSig = signMessage(response.toString());
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                LocationReport lr = LocationReport.newBuilder().setMessage(encryptedMessage).setIv(Base64.getEncoder()
                        .encodeToString(ivSpec.getIV())).setDigSig(digSig).setUser(user).build();

                try {
                   bStub.submitLocationReport(lr);
                } catch (Exception e) {
                    if (e.getMessage().contains("RESOURCE_EXHAUSTED") || e.getMessage().contains("NOT_FOUND")) {
                        init(i);
                        try {
                            ObtainLocation(new ArrayList<>(i),epoch);
                        } catch (InterruptedException | NoSuchAlgorithmException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }
                    return;
                }

            };
            executorService.execute(run);
            Thread.sleep(100);
            if(executorService.awaitTermination(100, TimeUnit.MILLISECONDS)){
                executorService.shutdownNow();
            }
        }

    }

    /*Requests the users' proofs to the servers*/
    public static void requestProofs(ArrayList<Integer> servers, int[] epochs) throws InterruptedException, NoSuchAlgorithmException {
        JsonObject json = new JsonObject();
        json.addProperty("userID", user);
        String eps = "";
        for (int e : epochs) {
            eps = eps + e + ",";
        }
        json.addProperty("epochs", eps);

        ArrayList<JsonArray> serverResponses = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(servers.size());

        // Proof-of-Work
        final String[] powData = computePoW(json.toString());

        for (int i : servers) {
            Runnable run = () -> {
                changeServer(i);
                int counter = counters.get("server" + i) + 1;
                counters.put("server" + i, counter);
                JsonObject json2 = json;
                json2.addProperty("counter",counter);
                json2.addProperty("nounce", powData[0]);
                json2.addProperty("pow", powData[1]);

                // Encripta????o
                String encryptedMessage = null;
                IvParameterSpec ivSpec = null;
                String digSig = null;
                try {
                    ivSpec = Utils.generateIv();
                    encryptedMessage = Utils.encryptMessageSymmetric(symmetricKeys.get(i), json2.toString(), ivSpec);
                    digSig = signMessage(json2.toString());
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                GetProofs gp = GetProofs.newBuilder().setUser(user).setMessage(encryptedMessage).setIv(Base64.getEncoder()
                        .encodeToString(ivSpec.getIV())).setDigSig(digSig).build();
                ProofsResponse resp = null;
                try {
                    resp = bStub.requestMyProofs(gp);
                } catch (Exception e) {
                    if (e.getMessage().contains("RESOURCE_EXHAUSTED") || e.getMessage().contains("NOT_FOUND")) {
                        init(i);
                        try {
                            requestProofs(new ArrayList<>(i), epochs);
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                            noSuchAlgorithmException.printStackTrace();
                        }
                    }
                    System.err.println(e.getMessage());
                    return;
                }

                // Desencriptar -
                encryptedMessage = resp.getMessage();
                byte[] iv = Base64.getDecoder().decode(resp.getIv());
                String decryptedMessage = null;
                try {
                    decryptedMessage = Utils.decryptMessageSymmetric(symmetricKeys.get(i), encryptedMessage, iv);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                JsonArray convertedResponse = new Gson().fromJson(decryptedMessage, JsonArray.class);
                counter = convertedResponse.get(convertedResponse.size() - 1).getAsJsonObject().get("counter").getAsInt();
                if (!verifyMessage("server" + i, convertedResponse.toString(), resp.getDigSig())) {
                    System.err.println("ERROR: Message not Verified");
                    return;
                }

                if (counter <= counters.get("server" + i)) {
                    System.err.println("ERROR: Wrong message received");
                    return;
                }

                /*REMOVES COUNTER*/
                convertedResponse.remove(convertedResponse.size() -1);
                counters.replace("server" + i, counter);

                for(JsonElement jsonElement : convertedResponse){
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    String msg = jsonObject.get("user").getAsString() +","+jsonObject.get("epoch").getAsString()+","+jsonObject.get("xCoord").getAsInt()+","+jsonObject.get("yCoord").getAsInt();
                    if(verifyMessage(user,msg,jsonObject.get("prooferDigSig").getAsString())){
                        serverResponses.add(convertedResponse);
                    }
                }
            };
            executorService.execute(run);
            Thread.sleep(100);
            if(executorService.awaitTermination(100, TimeUnit.MILLISECONDS)){
                executorService.shutdownNow();
            }
        }

        HashMap<JsonArray,Integer> response_counter = new HashMap<>();
        int counter_responses = 0;
        JsonArray finalResponse = new JsonArray();
        /*VERIFIES RECEIVED MESSAGES FROM SERVERS*/
        if ((double) serverResponses.size() > ((double) NUMBER_SERVERS + (double) BYZANTINE_SERVERS) / 2.0) {
            for (JsonArray jsonArr : serverResponses) {
                if(response_counter.containsKey(jsonArr)){
                    counter_responses = response_counter.get(jsonArr);
                    response_counter.put(jsonArr,counter_responses+1);
                }else {
                    response_counter.put(jsonArr, 1);
                }
            }
            int max = 0;
            for (Map.Entry<JsonArray,Integer> entry : response_counter.entrySet()) {
                if(entry.getValue() > max){
                    max = entry.getValue();
                    finalResponse = entry.getKey();
                }
            }
        }else{
            System.err.println("ERROR: Wrong number of servers responded");
            return;
        }
        System.out.println(finalResponse.toString());

    }

    private static void changeServer(int n_server) {
        //Connection to server
        String phrase = UsersMap.get("server");
        String svcIP = phrase.split(":")[0];
        int sPort = Integer.parseInt(phrase.split(":")[1]) + n_server;
        ManagedChannel channel;
        if(bStub != null){
            channel = (ManagedChannel) bStub.getChannel();
            channel.shutdownNow();
        }
        channel = ManagedChannelBuilder.forAddress(svcIP, sPort)
                .usePlaintext()
                .build();
        bStub = UserServerGrpc.newBlockingStub(channel);
    }

    private static String signMessage(String msgToSign) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnrecoverableKeyException, KeyStoreException {
        byte[] messageSigned = Utils.signMessage("keystores/keystore_" + user + ".keystore",keystore_password,msgToSign);
        return new String(Base64.getEncoder().encode(messageSigned));
    }

    private static int[] splitStrToIntArr(String theStr) {
        String[] strArr = theStr.split(",");
        int[] intArr = new int[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            String num = strArr[i];
            intArr[i] = Integer.parseInt(num);
        }
        return intArr;
    }

    private static String[] computePoW (String msg) throws NoSuchAlgorithmException {
        Random r = new Random(System.currentTimeMillis());
        Boolean finish = false;
        String hash = null;
        int n = 0;
        int counter = 0;
        while (!finish) {
            n = r.nextInt();
            counter++;
            hash = Utils.computeSHA256(msg + n);
            if (hash.substring(0, 4).equals("0000"))
                finish = true;
        }
        System.out.println("INFO: PoW needed " +  counter  + " iterations!");
        return new String[]{String.valueOf(n), hash};
    }

    public static void main(String[] args) throws GeneralSecurityException, IOException {

        //Ler ficheiro com os endere??os dos utilizadores
        readUsers();

        //CHECK SIZE OF ARGUMENTS
        if(args.length < 1){
            System.err.println("ERROR:Wrong number of arguments");
            return;
        }

        /*GETS THE USER PASSWORD FROM INPUT*/
        keystore_password = Utils.getPasswordInput();

        //Obter o pr??prio Utilizador bem como o port
        user = args[0];
        int svcPort = Integer.parseInt(UsersMap.get(user).split(":")[1]);

        int i = 0;
        ArrayList<Integer> servers = new ArrayList<>();

        for (i = 1; i <= NUMBER_SERVERS ; i++) {
            try {
                changeServer(i);
                init(i);
                servers.add(i);
            }catch(Exception e){
                System.err.println("ERROR: Server connection failed!");
                counters.remove("server" + i);
            }

        }


        //Instancia de Servidor para os Clients
        Server svc = null;
        try {
            svc = ServerBuilder
                    .forPort(svcPort)
                    .addService(new HDLT_user())
                    .build();

            svc.start();

            System.out.println("User Server started, listening on " + svcPort);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("\nAvailable commands:");
        System.out.println("- RequestProof (r): requests nearby users to send proof of user location");
        System.out.println("- SubmitLocation (s): submits location report to the server");
        System.out.println("- ObtainLocation (o <epoch>): requests from the server the location of the user on the specified epoch");
        System.out.println("- RequestMyProofs (p <epoch>,<epoch>): requests from the server the location of the user on the specified epoch");
        System.out.println("- Sleep <n> : sleeps for n milliseconds");
        System.out.println("- Epoch (e <epoch>) : Sets the user's epoch to the indicated");


        //Ler um Script com os requests de cada utilizador
        BufferedReader reader;
        try {
            Scanner scanner = new Scanner(System.in);
            while(true){
                System.out.print("Epoch "+currentEpoch+"> ");
                String line = scanner.nextLine();
                String cmd = line.split(" ")[0];
                switch (cmd) {
                    case "RequestProof":
                    case "r":
                        System.out.println("Requesting Location Proof to nearby users");
                        requestProof(currentEpoch);
                        break;
                    case "SubmitLocation":
                    case "s":
                        System.out.println("Submitting Location");
                        SubmitLocation(servers);
                        break;
                    case "ObtainLocation":
                    case "o":
                        int epoch = Integer.parseInt(line.split(" ")[1]);
                        System.out.println("Obtaining Location of " + user + " at epoch " + epoch);
                        ObtainLocation(servers, epoch);
                        break;
                    case "Sleep":
                        Thread.sleep(Integer.parseInt(line.split(" ")[1]));
                        break;
                    case "Epoch":
                    case "e":
                        currentEpoch = Integer.parseInt(line.split(" ")[1]);
                        System.out.println("Setting time to Epoch " + currentEpoch);
                        break;
                    case "RequestMyProofs":
                    case "p":
                        int[] epochs = splitStrToIntArr(line.split(" ")[1]);
                        System.out.println("Requesting My proofs " + currentEpoch);
                        requestProofs(servers, epochs);
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