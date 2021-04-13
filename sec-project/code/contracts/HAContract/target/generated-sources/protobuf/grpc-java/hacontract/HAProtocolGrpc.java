package hacontract;

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
    comments = "Source: HAContract.proto")
public final class HAProtocolGrpc {

  private HAProtocolGrpc() {}

  public static final String SERVICE_NAME = "hacontract.HAProtocol";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<hacontract.GetLocation,
      hacontract.LocationStatus> getObtainLocationReportMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "obtainLocationReport",
      requestType = hacontract.GetLocation.class,
      responseType = hacontract.LocationStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<hacontract.GetLocation,
      hacontract.LocationStatus> getObtainLocationReportMethod() {
    io.grpc.MethodDescriptor<hacontract.GetLocation, hacontract.LocationStatus> getObtainLocationReportMethod;
    if ((getObtainLocationReportMethod = HAProtocolGrpc.getObtainLocationReportMethod) == null) {
      synchronized (HAProtocolGrpc.class) {
        if ((getObtainLocationReportMethod = HAProtocolGrpc.getObtainLocationReportMethod) == null) {
          HAProtocolGrpc.getObtainLocationReportMethod = getObtainLocationReportMethod = 
              io.grpc.MethodDescriptor.<hacontract.GetLocation, hacontract.LocationStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "hacontract.HAProtocol", "obtainLocationReport"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  hacontract.GetLocation.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  hacontract.LocationStatus.getDefaultInstance()))
                  .setSchemaDescriptor(new HAProtocolMethodDescriptorSupplier("obtainLocationReport"))
                  .build();
          }
        }
     }
     return getObtainLocationReportMethod;
  }

  private static volatile io.grpc.MethodDescriptor<hacontract.UserAtLocation,
      hacontract.Users> getObtainUsersAtLocationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "obtainUsersAtLocation",
      requestType = hacontract.UserAtLocation.class,
      responseType = hacontract.Users.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<hacontract.UserAtLocation,
      hacontract.Users> getObtainUsersAtLocationMethod() {
    io.grpc.MethodDescriptor<hacontract.UserAtLocation, hacontract.Users> getObtainUsersAtLocationMethod;
    if ((getObtainUsersAtLocationMethod = HAProtocolGrpc.getObtainUsersAtLocationMethod) == null) {
      synchronized (HAProtocolGrpc.class) {
        if ((getObtainUsersAtLocationMethod = HAProtocolGrpc.getObtainUsersAtLocationMethod) == null) {
          HAProtocolGrpc.getObtainUsersAtLocationMethod = getObtainUsersAtLocationMethod = 
              io.grpc.MethodDescriptor.<hacontract.UserAtLocation, hacontract.Users>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "hacontract.HAProtocol", "obtainUsersAtLocation"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  hacontract.UserAtLocation.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  hacontract.Users.getDefaultInstance()))
                  .setSchemaDescriptor(new HAProtocolMethodDescriptorSupplier("obtainUsersAtLocation"))
                  .build();
          }
        }
     }
     return getObtainUsersAtLocationMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static HAProtocolStub newStub(io.grpc.Channel channel) {
    return new HAProtocolStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static HAProtocolBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new HAProtocolBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static HAProtocolFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new HAProtocolFutureStub(channel);
  }

  /**
   */
  public static abstract class HAProtocolImplBase implements io.grpc.BindableService {

    /**
     */
    public void obtainLocationReport(hacontract.GetLocation request,
        io.grpc.stub.StreamObserver<hacontract.LocationStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getObtainLocationReportMethod(), responseObserver);
    }

    /**
     */
    public void obtainUsersAtLocation(hacontract.UserAtLocation request,
        io.grpc.stub.StreamObserver<hacontract.Users> responseObserver) {
      asyncUnimplementedUnaryCall(getObtainUsersAtLocationMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getObtainLocationReportMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                hacontract.GetLocation,
                hacontract.LocationStatus>(
                  this, METHODID_OBTAIN_LOCATION_REPORT)))
          .addMethod(
            getObtainUsersAtLocationMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                hacontract.UserAtLocation,
                hacontract.Users>(
                  this, METHODID_OBTAIN_USERS_AT_LOCATION)))
          .build();
    }
  }

  /**
   */
  public static final class HAProtocolStub extends io.grpc.stub.AbstractStub<HAProtocolStub> {
    private HAProtocolStub(io.grpc.Channel channel) {
      super(channel);
    }

    private HAProtocolStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected HAProtocolStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new HAProtocolStub(channel, callOptions);
    }

    /**
     */
    public void obtainLocationReport(hacontract.GetLocation request,
        io.grpc.stub.StreamObserver<hacontract.LocationStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getObtainLocationReportMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void obtainUsersAtLocation(hacontract.UserAtLocation request,
        io.grpc.stub.StreamObserver<hacontract.Users> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getObtainUsersAtLocationMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class HAProtocolBlockingStub extends io.grpc.stub.AbstractStub<HAProtocolBlockingStub> {
    private HAProtocolBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private HAProtocolBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected HAProtocolBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new HAProtocolBlockingStub(channel, callOptions);
    }

    /**
     */
    public hacontract.LocationStatus obtainLocationReport(hacontract.GetLocation request) {
      return blockingUnaryCall(
          getChannel(), getObtainLocationReportMethod(), getCallOptions(), request);
    }

    /**
     */
    public hacontract.Users obtainUsersAtLocation(hacontract.UserAtLocation request) {
      return blockingUnaryCall(
          getChannel(), getObtainUsersAtLocationMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class HAProtocolFutureStub extends io.grpc.stub.AbstractStub<HAProtocolFutureStub> {
    private HAProtocolFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private HAProtocolFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected HAProtocolFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new HAProtocolFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<hacontract.LocationStatus> obtainLocationReport(
        hacontract.GetLocation request) {
      return futureUnaryCall(
          getChannel().newCall(getObtainLocationReportMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<hacontract.Users> obtainUsersAtLocation(
        hacontract.UserAtLocation request) {
      return futureUnaryCall(
          getChannel().newCall(getObtainUsersAtLocationMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_OBTAIN_LOCATION_REPORT = 0;
  private static final int METHODID_OBTAIN_USERS_AT_LOCATION = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final HAProtocolImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(HAProtocolImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_OBTAIN_LOCATION_REPORT:
          serviceImpl.obtainLocationReport((hacontract.GetLocation) request,
              (io.grpc.stub.StreamObserver<hacontract.LocationStatus>) responseObserver);
          break;
        case METHODID_OBTAIN_USERS_AT_LOCATION:
          serviceImpl.obtainUsersAtLocation((hacontract.UserAtLocation) request,
              (io.grpc.stub.StreamObserver<hacontract.Users>) responseObserver);
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

  private static abstract class HAProtocolBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    HAProtocolBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return hacontract.HAContract.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("HAProtocol");
    }
  }

  private static final class HAProtocolFileDescriptorSupplier
      extends HAProtocolBaseDescriptorSupplier {
    HAProtocolFileDescriptorSupplier() {}
  }

  private static final class HAProtocolMethodDescriptorSupplier
      extends HAProtocolBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    HAProtocolMethodDescriptorSupplier(String methodName) {
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
      synchronized (HAProtocolGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new HAProtocolFileDescriptorSupplier())
              .addMethod(getObtainLocationReportMethod())
              .addMethod(getObtainUsersAtLocationMethod())
              .build();
        }
      }
    }
    return result;
  }
}
