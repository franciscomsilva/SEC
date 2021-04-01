import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import userprotocol.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Integer.parseInt;

public class HDLT_user extends UserProtocolGrpc.UserProtocolImplBase{
    // Variaveis Globais
    static UserProtocolGrpc.UserProtocolBlockingStub blockingStub;
    static HashMap<String,String> UsersMap = new HashMap<>();
    static String user;
    static int x;
    static int y;

    static int currentEpoch;


    public static void readUsers(){
        String path = "";


    }

    public static void connectToUser(String phrase){
        //Split da phrase

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
        //Ler o mapa -> readMap(int epoch)



        // Conectar com um utilizador

        LocationRequest locationRequest = LocationRequest.newBuilder().setId(5).setXCoord(x).setYCoord(y).build();
        Proof proof = blockingStub.requestLocationProof(locationRequest);
        int id = proof.getId();
        String digSig = proof.getDigSig();

       return null;
    }

    public void requestLocationProof(LocationRequest request, StreamObserver<Proof> responseObserver) {
        //Geração da Proof
        int id = request.getId();
        int xCoord = request.getXCoord();
        int yCoord = request.getYCoord();

        try{
            HashMap<String,double []> users = readMap(currentEpoch);

            users.get
        } catch (CsvValidationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        responseObserver.onNext();
        responseObserver.onCompleted();
    }

    public static String[] SubmitLocation(){

        return null;
    }

    public static String[] ObtainLocation(){

        return [];
    }

    public static void main(String[] args){
        user = args[1];
        int svcPort = 0;
        io.grpc.Server svc;
        try {
            if (args.length > 0)
                svcPort = parseInt(args[0]);
            svc = ServerBuilder
                    .forPort(svcPort)
                    .addService(new Server())
                    .build();
            svc.start();
            System.out.println("Server started, listening on " + svcPort);





        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Ler ficheiro com o HashMAP dos users
        //LER um Script para gerar movimentos maybe
        String cmd = "Request";
        switch (cmd) {
            case "RequestProof":
                break;
            case "SubmitLocation":
                break;
            case "ObtainLocation":
                break;
            case "Sleep":
                break;
            default:
                System.out.println("Some errors in reading of the script");
        }

        svc.shutdown();
    }

    @Override

}
