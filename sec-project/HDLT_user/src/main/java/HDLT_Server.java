import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import userprotocol.*;
import userserver.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

public class HDLT_Server extends UserServerGrpc.UserServerImplBase {
    // Variaveis Globais

    static HashMap<String,String> UsersMap = new HashMap<>();
    static int currentEpoch;

    static HashMap<String,List<HashMap<String,String>>> reports = new HashMap<>();

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
        super.submitLocationReport(request, responseObserver);
    }

    @Override
    public void obtainLocationReport(GetLocation request, StreamObserver<LocationStatus> responseObserver) {
        super.obtainLocationReport(request, responseObserver);
    }

    public static void main(String[] args){

        int svcPort = Integer.parseInt(UsersMap.get("server").split(":")[1]);

        Server svc = null;
        try {
            svc = ServerBuilder
                    .forPort(svcPort)
                    .addService(new HDLT_Server())
                    .build();

            svc.start();

            System.out.println("Server started, listening on " + svcPort);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
