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


import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Utils.Utils;
import userserver.Key;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import static java.lang.Integer.parseInt;

public class HDLT_user extends UserProtocolImplBase{

    /*GLOBAL VARIABLES*/
    private static UserProtocolGrpc.UserProtocolBlockingStub blockingStub;
    private static UserServerGrpc.UserServerBlockingStub bStub;

    private static HashMap<String,String> UsersMap = new HashMap<>();

    private static ConcurrentHashMap<String,String> proofers = new ConcurrentHashMap<>();

    private static SecretKey symmetricKey;

    private static String user;
    private static int x;
    private static int y;

    private static int currentEpoch;


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

        try (CSVReader reader = new CSVReader(new FileReader("MAP.txt"))) {
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
        System.out.println(RadiusUsers.size());
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
            }
            Thread.sleep(3000);
            executorService.shutdownNow();
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
                throw new Exception("ERROR: User not in map range");
            }

        } catch (Exception e) {
            responseObserver.onError(new StatusException((Status.ABORTED.withDescription(e.getMessage()))));
        }
    }

    public static void SubmitLocation(){

        JsonArray proofersArray = new JsonArray();

        //System.out.println(proofersArray);

        for (Map.Entry<String, String> entry : proofers.entrySet()) {
            System.out.println(entry.getKey()+":"+entry.getValue());
        }
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
        //Obter o próprio Utilizador bem como o port
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
        Key responseKey = bStub.init(initMessage);
        String base64SymmetricKey = responseKey.getKey();

        byte[] symmetricKeyBytes = Utils.decryptMessageAssymetric("keys/" + user + ".key",base64SymmetricKey);
        symmetricKey = Utils.generateSymmetricKey(symmetricKeyBytes);

        //Instancia de Servidor para os Clients
        Server svc = null;
        try {
            svc = ServerBuilder
                    .forPort(svcPort)
                    .addService(new HDLT_user())
                    .build();

            svc.start();

            System.out.println("Server started, listening on " + svcPort);

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
                    default:
                        System.out.println("Some errors in reading of the script");
                }
                //line = reader.readLine();
            }
            //reader.close();
            //while (true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        svc.shutdown();
    }
}