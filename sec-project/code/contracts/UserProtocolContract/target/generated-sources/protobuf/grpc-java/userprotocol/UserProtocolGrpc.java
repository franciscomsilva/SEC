package userprotocol;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.18.0)",
    comments = "Source: UserProtocolContract.proto")
public final class UserProtocolGrpc {

  private UserProtocolGrpc() {}

  public static final String SERVICE_NAME = "userprotocol.UserProtocol";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<userprotocol.LocationRequest,
      userprotocol.Proof> getRequestLocationProofMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "requestLocationProof",
      requestType = userprotocol.LocationRequest.class,
      responseType = userprotocol.Proof.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<userprotocol.LocationRequest,
      userprotocol.Proof> getRequestLocationProofMethod() {
    io.grpc.MethodDescriptor<userprotocol.LocationRequest, userprotocol.Proof> getRequestLocationProofMethod;
    if ((getRequestLocationProofMethod = UserProtocolGrpc.getRequestLocationProofMethod) == null) {
      synchronized (UserProtocolGrpc.class) {
        if ((getRequestLocationProofMethod = UserProtocolGrpc.getRequestLocationProofMethod) == null) {
          UserProtocolGrpc.getRequestLocationProofMethod = getRequestLocationProofMethod = 
              io.grpc.MethodDescriptor.<userprotocol.LocationRequest, userprotocol.Proof>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "userprotocol.UserProtocol", "requestLocationProof"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  userprotocol.LocationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  userprotocol.Proof.getDefaultInstance()))
                  .setSchemaDescriptor(new UserProtocolMethodDescriptorSupplier("requestLocationProof"))
                  .build();
          }
        }
     }
     return getRequestLocationProofMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static UserProtocolStub newStub(io.grpc.Channel channel) {
    return new UserProtocolStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static UserProtocolBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new UserProtocolBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static UserProtocolFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new UserProtocolFutureStub(channel);
  }

  /**
   */
  public static abstract class UserProtocolImplBase implements io.grpc.BindableService {

    /**
     */
    public void requestLocationProof(userprotocol.LocationRequest request,
        io.grpc.stub.StreamObserver<userprotocol.Proof> responseObserver) {
      asyncUnimplementedUnaryCall(getRequestLocationProofMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRequestLocationProofMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                userprotocol.LocationRequest,
                userprotocol.Proof>(
                  this, METHODID_REQUEST_LOCATION_PROOF)))
          .build();
    }
  }

  /**
   */
  public static final class UserProtocolStub extends io.grpc.stub.AbstractStub<UserProtocolStub> {
    private UserProtocolStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UserProtocolStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserProtocolStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UserProtocolStub(channel, callOptions);
    }

    /**
     */
    public void requestLocationProof(userprotocol.LocationRequest request,
        io.grpc.stub.StreamObserver<userprotocol.Proof> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRequestLocationProofMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class UserProtocolBlockingStub extends io.grpc.stub.AbstractStub<UserProtocolBlockingStub> {
    private UserProtocolBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UserProtocolBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserProtocolBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UserProtocolBlockingStub(channel, callOptions);
    }

    /**
     */
    public userprotocol.Proof requestLocationProof(userprotocol.LocationRequest request) {
      return blockingUnaryCall(
          getChannel(), getRequestLocationProofMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class UserProtocolFutureStub extends io.grpc.stub.AbstractStub<UserProtocolFutureStub> {
    private UserProtocolFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UserProtocolFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserProtocolFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UserProtocolFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<userprotocol.Proof> requestLocationProof(
        userprotocol.LocationRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getRequestLocationProofMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_REQUEST_LOCATION_PROOF = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final UserProtocolImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(UserProtocolImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REQUEST_LOCATION_PROOF:
          serviceImpl.requestLocationProof((userprotocol.LocationRequest) request,
              (io.grpc.stub.StreamObserver<userprotocol.Proof>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class UserProtocolBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    UserProtocolBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return userprotocol.UserProtocolContract.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("UserProtocol");
    }
  }

  private static final class UserProtocolFileDescriptorSupplier
      extends UserProtocolBaseDescriptorSupplier {
    UserProtocolFileDescriptorSupplier() {}
  }

  private static final class UserProtocolMethodDescriptorSupplier
      extends UserProtocolBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    UserProtocolMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (UserProtocolGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new UserProtocolFileDescriptorSupplier())
              .addMethod(getRequestLocationProofMethod())
              .build();
        }
      }
    }
    return result;
  }
}
