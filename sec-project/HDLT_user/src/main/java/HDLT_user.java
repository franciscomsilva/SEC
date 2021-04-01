import com.google.protobuf.Descriptors;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import userprotocol.*;
import userprotocol.UserProtocolGrpc.UserProtocolImplBase;
import userserver.*;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


import Utils.Utils;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static java.lang.Integer.parseInt;

public class HDLT_user extends UserProtocolImplBase{

    // Variaveis Globais
    static UserProtocolGrpc.UserProtocolBlockingStub blockingStub;
    static UserServerGrpc.UserServerBlockingStub bStub;
    static HashMap<String,String> UsersMap = new HashMap<>();

    static HashMap<String,String> proofers = new HashMap<>();


    static String user;
    static int x;
    static int y;

    static int currentEpoch;


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

    public static String[] requestProof(){

        HashMap<String,double []> RadiusUsers = new HashMap<>();
        try {
            RadiusUsers = readMap(currentEpoch);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String,double []> entry : RadiusUsers.entrySet()){
            connectToUser(UsersMap.get(entry.getKey()));
            try {
                LocationRequest locationRequest = LocationRequest.newBuilder().setId(user).setXCoord(x).setYCoord(y).build();
                Proof proof = blockingStub.requestLocationProof(locationRequest);
                proofers.put(proof.getId(),proof.getDigSig());

            }catch (Exception e){

            }
        }
        return null;
    }

    @Override
    public void requestLocationProof(LocationRequest request, StreamObserver<Proof> responseObserver) {
        //Geração da Proof
        String id = request.getId();
        int xCoord = request.getXCoord();
        int yCoord = request.getYCoord();

        try{
            HashMap<String,double []> users = readMap(currentEpoch);
            // Verificação se o requisitante está perto deste user
            if(users.containsKey(id)){
                double [] coords = users.get(id);
                if (xCoord == coords[1] && yCoord == coords[2]){
                    String msg = id +","+currentEpoch+","+xCoord+","+yCoord;

                    /*READS PRIVATE  KEY TO SIGN */
                    byte[] privKeyBytes = Files.readAllBytes(Paths.get("keys/"+user));
                    PKCS8EncodedKeySpec specPriv = new PKCS8EncodedKeySpec(privKeyBytes);
                    KeyFactory kf = KeyFactory.getInstance("RSA");
                    PrivateKey privateKey = kf.generatePrivate(specPriv);

                    byte[] digitalSignatureToSent = Utils.signMessage(privateKey,msg);

                    Proof pf = Proof.newBuilder().setId(user).setDigSig(digitalSignatureToSent.toString()).build();

                    responseObserver.onNext(pf);
                    responseObserver.onCompleted();
                }else{
                    //responseObserver.onError(new StatusException((Status.ABORTED.withDescription(e.getMessage()).withCause(e))));
                }
            }else{
                //responseObserver.onError();
            }

        } catch (CsvValidationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


    }

    public static void SubmitLocation(){
        LocationReport lr = LocationReport.newBuilder().setId(user).setEp(currentEpoch).setXCoord(x).setYCoord(y).putAllReport(proofers).build();
        LocationResponse bool = bStub.submitLocationReport(lr);

        if(bool.getDone()){
            proofers.clear();
        }// e se não for true ???
    }

    public static void ObtainLocation(int epoch){

        GetLocation gl = GetLocation.newBuilder().setId(user).setEp(epoch).build();
        LocationStatus coords = bStub.obtainLocationReport(gl);

        System.out.println("("+ coords.getXCoord()+","+coords.getYCoord()+") at epoch"+epoch);

    }

    public static void main(String[] args){

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
                String line = scanner.nextLine();
                String cmd = line.split(" ")[0];
                switch (cmd) {
                    case "RequestProof":
                        requestProof();
                        break;
                    case "SubmitLocation":
                        SubmitLocation();
                        break;
                    case "ObtainLocation":
                        ObtainLocation(Integer.parseInt(line.split(" ")[1]));
                        break;
                    case "Sleep":
                        Thread.sleep(Integer.parseInt(line.split(" ")[1]));
                        break;
                    case "Epoch":
                        currentEpoch = Integer.parseInt(line.split(" ")[1]);
                        //System.out.println("test");
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