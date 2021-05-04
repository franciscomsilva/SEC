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


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    private static int NUMBER_SERVERS = 2;


    private static HashMap<String,String> UsersMap = new HashMap<>();

    private static ConcurrentHashMap<String,String> proofers = new ConcurrentHashMap<>();

    private static ArrayList<SecretKey> symmetricKeys = new ArrayList<>();

    private static String user;
    private static int x;
    private static int y;

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
            if(v <= 2.0){
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

    @Override
    public void requestLocationProof(LocationRequest request, StreamObserver<Proof> responseObserver)  {
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
                    //byte[] privKeyBytes = Files.readAllBytes(Paths.get("keys/"+user+".key"));
                    //PKCS8EncodedKeySpec specPriv = new PKCS8EncodedKeySpec(privKeyBytes);
                    //KeyFactory kf = KeyFactory.getInstance("RSA");
                    //PrivateKey privateKey = kf.generatePrivate(specPriv);
                    //byte[] digitalSignatureToSent = Utils.signMessage(privateKey,msg);
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

    public static void SubmitLocation(){

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
        json.addProperty("userID", user);
        json.addProperty("currentEpoch",currentEpoch);
        json.addProperty("xCoord",x);
        json.addProperty("yCoord",y);
        json.add("proofers",proofersArray);
        System.out.println(json.toString());

        for (int i = 1; i <= NUMBER_SERVERS ; i++) {
            changeServer(i);
            // Encriptação
            String encryptedMessage = null;
            IvParameterSpec ivSpec = null;
            String digSig = null;
            try {
                ivSpec = Utils.generateIv();
                encryptedMessage = Utils.encryptMessageSymmetric(symmetricKeys.get(i), json.toString(), ivSpec);
                digSig = signMessage(json.toString());
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
                Throwable cause = e.getCause();
                Status status = ((StatusException) cause).getStatus();
                if (status.getCode().equals(Status.Code.RESOURCE_EXHAUSTED) || status.getCode().equals(Status.Code.NOT_FOUND)) {
                    InitMessage initMessage = InitMessage.newBuilder().setUser(user).build();
                    Key responseKey = null;
                    try{
                        responseKey = bStub.init(initMessage);
                        String base64SymmetricKey = responseKey.getKey();
                        /*GETS THE USER PASSWORD FROM INPUT*/
                        String password = Utils.getPasswordInput();

                        byte[] symmetricKeyBytes = Utils.decryptMessageAssymetric("keystores/keystore_" + user + ".keystore",password,base64SymmetricKey);
                        symmetricKeys.set(i, Utils.generateSymmetricKey(symmetricKeyBytes));
                    } catch(Exception ex){
                        System.err.println("ERROR: Server connection failed!");
                        return;
                    }
                    SubmitLocation();
                    return;
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

            JsonObject convertedResponse = new Gson().fromJson(decryptedMessage, JsonObject.class);

            Boolean bool = convertedResponse.get("Done").getAsBoolean();

            if (bool) {
                proofers.clear();
            } else {
                System.err.println("Submittion failed!");
            }
        }
    }

    public static void ObtainLocation(int epoch){

        JsonObject json = new JsonObject();
        json.addProperty("userID", user);
        json.addProperty("Epoch",epoch);

        // Encriptação
        for (int i = 1; i <= NUMBER_SERVERS ; i++) {
            changeServer(i);
            String encryptedMessage = null;
            IvParameterSpec ivSpec = null;
            LocationStatus resp = null;
            String digSig = null;
            try {
                ivSpec = Utils.generateIv();
                encryptedMessage = Utils.encryptMessageSymmetric(symmetricKeys.get(i), json.toString(), ivSpec);
                digSig = signMessage(json.toString());
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            GetLocation gl = GetLocation.newBuilder().setMessage(encryptedMessage).setUser(user).setIv(Base64.getEncoder()
                    .encodeToString(ivSpec.getIV())).setDigSig(digSig).build();
            try{
                resp = bStub.obtainLocationReport(gl);
            } catch (Exception e) {
                Throwable cause = e.getCause();
                Status status = ((StatusException) cause).getStatus();
                if (status.getCode().equals(Status.Code.RESOURCE_EXHAUSTED) || status.getCode().equals(Status.Code.NOT_FOUND)) {
                    InitMessage initMessage = InitMessage.newBuilder().setUser(user).build();
                    Key responseKey = null;
                    try{
                        responseKey = bStub.init(initMessage);
                        String base64SymmetricKey = responseKey.getKey();

                        /*GETS THE USER PASSWORD FROM INPUT*/
                        String password = Utils.getPasswordInput();

                        byte[] symmetricKeyBytes = Utils.decryptMessageAssymetric("keystores/keystore_" + user + ".keystore",password,base64SymmetricKey);
                        symmetricKeys.set(i, Utils.generateSymmetricKey(symmetricKeyBytes));
                    } catch(Exception ex){
                        System.err.println("ERROR: Server connection failed!");
                        return;
                    }
                    ObtainLocation(epoch);
                    return;
                }
                System.err.println(e.getMessage());
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

            int XCoord = convertedResponse.get("XCoord").getAsInt();
            int YCoord = convertedResponse.get("YCoord").getAsInt();

            System.out.println("User " + user + " are in (" + XCoord + "," + YCoord + ") at epoch " + epoch);
        }
    }

    public static void requestProofs(int[] epochs){
        JsonObject json = new JsonObject();
        json.addProperty("userID", user);
        JsonArray jsonEpochs = new JsonArray();
        for (int e : epochs) {
            JsonObject o = new JsonObject();
            o.addProperty("epoch", e);
            jsonEpochs.add(o);
        }
        json.add("epochs", jsonEpochs);

        for (int i = 1; i <= NUMBER_SERVERS ; i++) {
            changeServer(i);
            // Encriptação
            String encryptedMessage = null;
            IvParameterSpec ivSpec = null;
            String digSig = null;
            try {
                ivSpec = Utils.generateIv();
                encryptedMessage = Utils.encryptMessageSymmetric(symmetricKeys.get(i), json.toString(), ivSpec);
                digSig = signMessage(json.toString());
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
                Throwable cause = e.getCause();
                Status status = ((StatusException) cause).getStatus();
                if (status.getCode().equals(Status.Code.RESOURCE_EXHAUSTED) || status.getCode().equals(Status.Code.NOT_FOUND)) {
                    InitMessage initMessage = InitMessage.newBuilder().setUser(user).build();
                    Key responseKey = null;
                    try{
                        responseKey = bStub.init(initMessage);
                        String base64SymmetricKey = responseKey.getKey();
                        /*GETS THE USER PASSWORD FROM INPUT*/
                        String password = Utils.getPasswordInput();

                        byte[] symmetricKeyBytes = Utils.decryptMessageAssymetric("keystores/keystore_" + user + ".keystore",password,base64SymmetricKey);
                        symmetricKeys.set(i, Utils.generateSymmetricKey(symmetricKeyBytes));
                    } catch(Exception ex){
                        System.err.println("ERROR: Server connection failed!");
                        return;
                    }
                    requestProofs(epochs);
                    return;
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

            JsonObject convertedResponse = new Gson().fromJson(decryptedMessage, JsonObject.class);

            //TODO - processar resposta do servidor
        }
    }

    private static void changeServer(int n_server){
        //Conexão com o servidor
        String phrase = UsersMap.get("server");
        String svcIP = phrase.split(":")[0];
        int sPort = Integer.parseInt(phrase.split(":")[1]) + n_server;

        ManagedChannel channel = ManagedChannelBuilder.forAddress(svcIP, sPort)
                .usePlaintext()
                .build();
        bStub = UserServerGrpc.newBlockingStub(channel);
    }

    private static String signMessage(String msgToSign) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnrecoverableKeyException, KeyStoreException {
        String password = Utils.getPasswordInput();
        byte[] messageSigned = Utils.signMessage("keystores/keystore_" + user + ".keystore",password,msgToSign);
        return new String(Base64.getEncoder().encode(messageSigned));
    }

    public static int[] splitStrToIntArr(String theStr) {
        String[] strArr = theStr.split(",");
        int[] intArr = new int[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            String num = strArr[i];
            intArr[i] = Integer.parseInt(num);
        }
        return intArr;
    }

    public static void main(String[] args) throws GeneralSecurityException, IOException {

        //Ler ficheiro com os endereços dos utilizadores
        readUsers();

        //CHECK SIZE OF ARGUMENTS
        if(args.length < 1){
            System.err.println("ERROR:Wrong number of arguments");
            return;
        }

        //Obter o próprio Utilizador bem como o port
        user = args[0];
        int svcPort = Integer.parseInt(UsersMap.get(user).split(":")[1]);


        InitMessage initMessage = InitMessage.newBuilder().setUser(user).build();
        Key responseKey = null;
        try{
            for (int i = 1; i <= NUMBER_SERVERS ; i++) {
                changeServer(i);
                responseKey = bStub.init(initMessage);
                String base64SymmetricKey = responseKey.getKey();
                /*GETS THE USER PASSWORD FROM INPUT*/
                String password = Utils.getPasswordInput();

                byte[] symmetricKeyBytes = Utils.decryptMessageAssymetric("keystores/keystore_" + user + ".keystore",password,base64SymmetricKey);
                symmetricKeys.add(Utils.generateSymmetricKey(symmetricKeyBytes));
            }
        }catch(Exception e){
            System.err.println("ERROR: Server connection failed!");
            return;
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
                        SubmitLocation();
                        break;
                    case "ObtainLocation":
                    case "o":
                        int epoch = Integer.parseInt(line.split(" ")[1]);
                        System.out.println("Obtaining Location of " + user + " at epoch " + epoch);
                        ObtainLocation(epoch);
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