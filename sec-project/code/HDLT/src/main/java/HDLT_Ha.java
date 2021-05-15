import Utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import hacontract.*;
import hacontract.Key;
import io.grpc.*;
import userserver.UserServerGrpc;
//import userserver.UserServerGrpc;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HDLT_Ha {

    static HashMap<String,String> UsersMap = new HashMap<>();

    static HAProtocolGrpc.HAProtocolBlockingStub bStub;//TODO Mudar o tipo de stub consoante o contracto

    private static String user;

    private static String keystore_password;
    private static HashMap<String,Integer> counters = new HashMap<>();
    private static ArrayList<SecretKey> symmetricKeys = new ArrayList<>();

    private static int NUMBER_SERVERS = 4;
    private static int BYZANTINE_SERVERS = 1;



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

    public static void obtainLocationReport(int[] servers,String user, int epoch) {

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


    public static void ObtainUsersAtLocation(int[] servers,int epoch, int xCoords, int yCoords) throws NoSuchPaddingException, UnrecoverableKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, KeyStoreException, InvalidKeyException, IOException, InvalidKeySpecException, InterruptedException {

        JsonObject json =  new JsonObject();
        json.addProperty("epoch", epoch);
        json.addProperty("xCoords", xCoords);
        json.addProperty("yCoords", yCoords);
        String messageSigned = signMessage(json.toString());

        ExecutorService executorService = Executors.newFixedThreadPool(servers.length);
        for (int i : servers) {
            Runnable run = () -> {
                changeServer(i);
                int counter = counters.get("server" + i) + 1;
                counters.put("server" + i, counter);
                JsonObject json2 = json;
                json2.addProperty("counter",counter);

                // Encriptação
                String encryptedMessage = null;
                IvParameterSpec ivSpec = null;
                hacontract.LocationStatus ls = null;
                String digSig = null;
                try {
                    ivSpec = Utils.generateIv();
                    encryptedMessage = Utils.encryptMessageSymmetric(symmetricKeys.get(i),json2.toString(),ivSpec);
                    digSig = signMessage(json2.toString());
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                UserAtLocation ua = UserAtLocation.newBuilder().setIv(Base64.getEncoder()
                        .encodeToString(ivSpec.getIV())).setMessage(encryptedMessage).setDigSig(digSig).build();

                Users users;
                try{
                    users =  bStub.obtainUsersAtLocation(ua);
                }catch(Exception e){
                    Throwable cause = e.getCause();
                    Status status = ((StatusException) cause).getStatus();
                    if (status.getCode().equals(Status.Code.RESOURCE_EXHAUSTED) || status.getCode().equals(Status.Code.NOT_FOUND)) {
                        init(i);
                        try {
                            ObtainUsersAtLocation(new int[]{i}, epoch,xCoords,yCoords);
                        } catch (InterruptedException | NoSuchPaddingException | UnrecoverableKeyException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | KeyStoreException | InvalidKeyException | IOException | InvalidKeySpecException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }
                    System.err.println(e.getMessage());
                    return;
                }

                // Desencriptar
                encryptedMessage = users.getMessage();
                byte[] iv =Base64.getDecoder().decode(users.getIv());
                String decryptedMessage = null;
                try {
                    decryptedMessage = Utils.decryptMessageSymmetric(symmetricKeys.get(i),encryptedMessage,iv);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }



                /*VERIFY DIGITAL SIGNATURE SERVER'S DIGITAL SIGNATURE*/


            };
            executorService.execute(run);
            Thread.sleep(100);
            if(executorService.awaitTermination(100, TimeUnit.MILLISECONDS)){
                executorService.shutdownNow();
            }
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

    private static String signMessage(String msgToSign) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnrecoverableKeyException, KeyStoreException {
        byte[] messageSigned = Utils.signMessage("keystores/keystore_" + user + ".keystore",keystore_password,msgToSign);
        return new String(Base64.getEncoder().encode(messageSigned));
    }

    public static void init(int server) {
        Random r = new Random(System.currentTimeMillis());
        int counter = 0;
        String digSig = null, message = null;

        /*INITIALIZES COUNTER*/
        counter = r.nextInt();
        counters.put("server" + server, counter);
        message = user + "," + counter;
        try {
            digSig = signMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        hacontract.InitMessage initMessage = hacontract.InitMessage.newBuilder().setUser(user).setCounter(counter).setDigSig(digSig).build();

        hacontract.Key responseKey = bStub.init(initMessage);

        String base64SymmetricKey = responseKey.getKey();
        int c = responseKey.getCounter();
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
        symmetricKeys.add(Utils.generateSymmetricKey(symmetricKeyBytes));
    }


    private static void changeServer(int n_server){
        //Conexão com o servidor
        String phrase = UsersMap.get("server");
        String svcIP = phrase.split(":")[0];
        int sPort = Integer.parseInt(phrase.split(":")[1]) + n_server;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(svcIP, sPort)
                .usePlaintext()
                .build();
        bStub = HAProtocolGrpc.newBlockingStub(channel);
    }


    public static void main(String[] args) throws GeneralSecurityException, IOException {
        readUsers();

        //Conexão com o servidor
        user = "clientHA";

        /*GETS THE USER PASSWORD FROM INPUT*/
        keystore_password = Utils.getPasswordInput();

        int svcPort = Integer.parseInt(UsersMap.get(user).split(":")[1]);

        int i = 0;
        int[] servers = new int[NUMBER_SERVERS];
        try{
            for (i = 1; i <= NUMBER_SERVERS ; i++) {
                changeServer(i);
                init(i);
                servers[i-1] = i;
            }
        }catch(Exception e){
            System.err.println("ERROR: Server connection failed!");
            counters.remove("server" + i);
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

            System.out.println("User HA Server started, listening on " + svcPort);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
                        obtainLocationReport(servers,user,epoch);
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
