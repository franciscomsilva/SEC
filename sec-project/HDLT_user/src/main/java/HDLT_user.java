import userprotocol.*;

public class HDLT_user {

    private static UserProtocolGrpc.UserProtocolBlockingStub blockingStub;


    LocationRequest locationRequest = LocationRequest.newBuilder().setId(5).setXCoord(1).setYCoord(2).build();
    Proof proof = blockingStub.requestLocationProof(locationRequest);
    int id = proof.getId();
    String digSig = proof.getDigSig();
}
