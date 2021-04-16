import Utils.Utils;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import hacontract.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
//import userserver.UserServerGrpc;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

public class HDLT_Ha {

    static HashMap<String,String> UsersMap = new HashMap<>();

    static HAProtocolGrpc.HAProtocolBlockingStub bStub;//TODO Mudar o tipo de stub consoante o contracto

    private static String SYMMETRICS_FILE_HA = "files/symmetric_keys_ha";
    private static HashMap<String, SecretKey> userSymmetricKeys = new HashMap<String, SecretKey>();


    public static void readUsers() {
        try (CSVReader reader = new CSVReader(new FileReader("users_connection.txt"))) {
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

    public static void obtainLocationReport(String user, int epoch) {

        // Encriptação

        String encryptedMessage = null;
        IvParameterSpec ivSpec = null;
        userserver.LocationStatus resp = null;
        try {
            ivSpec = Utils.generateIv();
            encryptedMessage = Utils.encryptMessageSymmetric(symmetricKey,json.toString(),ivSpec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        userserver.GetLocation gl = userserver.GetLocation.newBuilder().setMessage(encryptedMessage).setUser(user).setIv(Base64.getEncoder()
                .encodeToString(ivSpec.getIV())).build();
        try{
            resp = bStub.obtainLocationReport(gl);
        }catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }


        GetLocation gl = GetLocation.newBuilder().setEp(epoch).setId(user).build();
        LocationStatus ls = null;
        try{
            ls = bStub.obtainLocationReport(gl);
        }catch (Exception e){
            System.err.println(e.getMessage());
            return;
        }

        System.out.println("User "+user+" at "+epoch+" are in coordinates ("+ls.getXCoord()+","+ls.getYCoord()+")");
    }

    public static void ObtainUsersAtLocation(int epoch, int xCoords, int yCoords) {

        UserAtLocation ua = UserAtLocation.newBuilder().setEpoch(epoch).setXCoord(xCoords).setYCoord(yCoords).build();
        List<String> users = new ArrayList<>();
        try{
            users =  bStub.obtainUsersAtLocation(ua).getIdsList();
        }catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }

        for(String u : users){
            System.out.print(u);
        }
        System.out.println("");
    }



    public static void main(String[] args){
        readUsers();

        //Conexão com o servidor
        String phrase = UsersMap.get("server");
        String svcIP = phrase.split(":")[0];
        int sPort = Integer.parseInt(phrase.split(":")[1] )+ 50;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(svcIP, sPort)
                .usePlaintext()
                .build();
        bStub = HAProtocolGrpc.newBlockingStub(channel);

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
                        obtainLocationReport(user,epoch);
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
