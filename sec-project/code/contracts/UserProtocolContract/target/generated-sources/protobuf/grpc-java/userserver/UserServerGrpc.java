package userserver;

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
    comments = "Source: UserServerContract.proto")
public final class UserServerGrpc {

  private UserServerGrpc() {}

  public static final String SERVICE_NAME = "userserver.UserServer";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<userserver.InitMessage,
      userserver.Key> getInitMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "init",
      requestType = userserver.InitMessage.class,
      responseType = userserver.Key.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<userserver.InitMessage,
      userserver.Key> getInitMethod() {
    io.grpc.MethodDescriptor<userserver.InitMessage, userserver.Key> getInitMethod;
    if ((getInitMethod = UserServerGrpc.getInitMethod) == null) {
      synchronized (UserServerGrpc.class) {
        if ((getInitMethod = UserServerGrpc.getInitMethod) == null) {
          UserServerGrpc.getInitMethod = getInitMethod = 
              io.grpc.MethodDescriptor.<userserver.InitMessage, userserver.Key>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "userserver.UserServer", "init"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  userserver.InitMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  userserver.Key.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServerMethodDescriptorSupplier("init"))
                  .build();
          }
        }
     }
     return getInitMethod;
  }

  private static volatile io.grpc.MethodDescriptor<userserver.LocationReport,
      userserver.LocationResponse> getSubmitLocationReportMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "submitLocationReport",
      requestType = userserver.LocationReport.class,
      responseType = userserver.LocationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<userserver.LocationReport,
      userserver.LocationResponse> getSubmitLocationReportMethod() {
    io.grpc.MethodDescriptor<userserver.LocationReport, userserver.LocationResponse> getSubmitLocationReportMethod;
    if ((getSubmitLocationReportMethod = UserServerGrpc.getSubmitLocationReportMethod) == null) {
      synchronized (UserServerGrpc.class) {
        if ((getSubmitLocationReportMethod = UserServerGrpc.getSubmitLocationReportMethod) == null) {
          UserServerGrpc.getSubmitLocationReportMethod = getSubmitLocationReportMethod = 
              io.grpc.MethodDescriptor.<userserver.LocationReport, userserver.LocationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "userserver.UserServer", "submitLocationReport"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  userserver.LocationReport.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  userserver.LocationResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServerMethodDescriptorSupplier("submitLocationReport"))
                  .build();
          }
        }
     }
     return getSubmitLocationReportMethod;
  }

  private static volatile io.grpc.MethodDescriptor<userserver.GetLocation,
      userserver.LocationStatus> getObtainLocationReportMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "obtainLocationReport",
      requestType = userserver.GetLocation.class,
      responseType = userserver.LocationStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<userserver.GetLocation,
      userserver.LocationStatus> getObtainLocationReportMethod() {
    io.grpc.MethodDescriptor<userserver.GetLocation, userserver.LocationStatus> getObtainLocationReportMethod;
    if ((getObtainLocationReportMethod = UserServerGrpc.getObtainLocationReportMethod) == null) {
      synchronized (UserServerGrpc.class) {
        if ((getObtainLocationReportMethod = UserServerGrpc.getObtainLocationReportMethod) == null) {
          UserServerGrpc.getObtainLocationReportMethod = getObtainLocationReportMethod = 
              io.grpc.MethodDescriptor.<userserver.GetLocation, userserver.LocationStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "userserver.UserServer", "obtainLocationReport"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  userserver.GetLocation.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  userserver.LocationStatus.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServerMethodDescriptorSupplier("obtainLocationReport"))
                  .build();
          }
        }
     }
     return getObtainLocationReportMethod;
  }

  private static volatile io.grpc.MethodDescriptor<userserver.GetProofs,
      userserver.ProofsResponse> getRequestMyProofsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "requestMyProofs",
      requestType = userserver.GetProofs.class,
      responseType = userserver.ProofsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<userserver.GetProofs,
      userserver.ProofsResponse> getRequestMyProofsMethod() {
    io.grpc.MethodDescriptor<userserver.GetProofs, userserver.ProofsResponse> getRequestMyProofsMethod;
    if ((getRequestMyProofsMethod = UserServerGrpc.getRequestMyProofsMethod) == null) {
      synchronized (UserServerGrpc.class) {
        if ((getRequestMyProofsMethod = UserServerGrpc.getRequestMyProofsMethod) == null) {
          UserServerGrpc.getRequestMyProofsMethod = getRequestMyProofsMethod = 
              io.grpc.MethodDescriptor.<userserver.GetProofs, userserver.ProofsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "userserver.UserServer", "requestMyProofs"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  userserver.GetProofs.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  userserver.ProofsResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServerMethodDescriptorSupplier("requestMyProofs"))
                  .build();
          }
        }
     }
     return getRequestMyProofsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static UserServerStub newStub(io.grpc.Channel channel) {
    return new UserServerStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static UserServerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new UserServerBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static UserServerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new UserServerFutureStub(channel);
  }

  /**
   */
  public static abstract class UserServerImplBase implements io.grpc.BindableService {

    /**
     */
    public void init(userserver.InitMessage request,
        io.grpc.stub.StreamObserver<userserver.Key> responseObserver) {
      asyncUnimplementedUnaryCall(getInitMethod(), responseObserver);
    }

    /**
     */
    public void submitLocationReport(userserver.LocationReport request,
        io.grpc.stub.StreamObserver<userserver.LocationResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getSubmitLocationReportMethod(), responseObserver);
    }

    /**
     */
    public void obtainLocationReport(userserver.GetLocation request,
        io.grpc.stub.StreamObserver<userserver.LocationStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getObtainLocationReportMethod(), responseObserver);
    }

    /**
     */
    public void requestMyProofs(userserver.GetProofs request,
        io.grpc.stub.StreamObserver<userserver.ProofsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getRequestMyProofsMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getInitMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                userserver.InitMessage,
                userserver.Key>(
                  this, METHODID_INIT)))
          .addMethod(
            getSubmitLocationReportMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                userserver.LocationReport,
                userserver.LocationResponse>(
                  this, METHODID_SUBMIT_LOCATION_REPORT)))
          .addMethod(
            getObtainLocationReportMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                userserver.GetLocation,
                userserver.LocationStatus>(
                  this, METHODID_OBTAIN_LOCATION_REPORT)))
          .addMethod(
            getRequestMyProofsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                userserver.GetProofs,
                userserver.ProofsResponse>(
                  this, METHODID_REQUEST_MY_PROOFS)))
          .build();
    }
  }

  /**
   */
  public static final class UserServerStub extends io.grpc.stub.AbstractStub<UserServerStub> {
    private UserServerStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UserServerStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserServerStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UserServerStub(channel, callOptions);
    }

    /**
     */
    public void init(userserver.InitMessage request,
        io.grpc.stub.StreamObserver<userserver.Key> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getInitMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void submitLocationReport(userserver.LocationReport request,
        io.grpc.stub.StreamObserver<userserver.LocationResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSubmitLocationReportMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void obtainLocationReport(userserver.GetLocation request,
        io.grpc.stub.StreamObserver<userserver.LocationStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getObtainLocationReportMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void requestMyProofs(userserver.GetProofs request,
        io.grpc.stub.StreamObserver<userserver.ProofsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRequestMyProofsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class UserServerBlockingStub extends io.grpc.stub.AbstractStub<UserServerBlockingStub> {
    private UserServerBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UserServerBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserServerBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UserServerBlockingStub(channel, callOptions);
    }

    /**
     */
    public userserver.Key init(userserver.InitMessage request) {
      return blockingUnaryCall(
          getChannel(), getInitMethod(), getCallOptions(), request);
    }

    /**
     */
    public userserver.LocationResponse submitLocationReport(userserver.LocationReport request) {
      return blockingUnaryCall(
          getChannel(), getSubmitLocationReportMethod(), getCallOptions(), request);
    }

    /**
     */
    public userserver.LocationStatus obtainLocationReport(userserver.GetLocation request) {
      return blockingUnaryCall(
          getChannel(), getObtainLocationReportMethod(), getCallOptions(), request);
    }

    /**
     */
    public userserver.ProofsResponse requestMyProofs(userserver.GetProofs request) {
      return blockingUnaryCall(
          getChannel(), getRequestMyProofsMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class UserServerFutureStub extends io.grpc.stub.AbstractStub<UserServerFutureStub> {
    private UserServerFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UserServerFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserServerFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UserServerFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<userserver.Key> init(
        userserver.InitMessage request) {
      return futureUnaryCall(
          getChannel().newCall(getInitMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<userserver.LocationResponse> submitLocationReport(
        userserver.LocationReport request) {
      return futureUnaryCall(
          getChannel().newCall(getSubmitLocationReportMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<userserver.LocationStatus> obtainLocationReport(
        userserver.GetLocation request) {
      return futureUnaryCall(
          getChannel().newCall(getObtainLocationReportMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<userserver.ProofsResponse> requestMyProofs(
        userserver.GetProofs request) {
      return futureUnaryCall(
          getChannel().newCall(getRequestMyProofsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_INIT = 0;
  private static final int METHODID_SUBMIT_LOCATION_REPORT = 1;
  private static final int METHODID_OBTAIN_LOCATION_REPORT = 2;
  private static final int METHODID_REQUEST_MY_PROOFS = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final UserServerImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(UserServerImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_INIT:
          serviceImpl.init((userserver.InitMessage) request,
              (io.grpc.stub.StreamObserver<userserver.Key>) responseObserver);
          break;
        case METHODID_SUBMIT_LOCATION_REPORT:
          serviceImpl.submitLocationReport((userserver.LocationReport) request,
              (io.grpc.stub.StreamObserver<userserver.LocationResponse>) responseObserver);
          break;
        case METHODID_OBTAIN_LOCATION_REPORT:
          serviceImpl.obtainLocationReport((userserver.GetLocation) request,
              (io.grpc.stub.StreamObserver<userserver.LocationStatus>) responseObserver);
          break;
        case METHODID_REQUEST_MY_PROOFS:
          serviceImpl.requestMyProofs((userserver.GetProofs) request,
              (io.grpc.stub.StreamObserver<userserver.ProofsResponse>) responseObserver);
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

  private static abstract class UserServerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    UserServerBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return userserver.UserServerContract.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("UserServer");
    }
  }

  private static final class UserServerFileDescriptorSupplier
      extends UserServerBaseDescriptorSupplier {
    UserServerFileDescriptorSupplier() {}
  }

  private static final class UserServerMethodDescriptorSupplier
      extends UserServerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    UserServerMethodDescriptorSupplier(String methodName) {
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
      synchronized (UserServerGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new UserServerFileDescriptorSupplier())
              .addMethod(getInitMethod())
              .addMethod(getSubmitLocationReportMethod())
              .addMethod(getObtainLocationReportMethod())
              .addMethod(getRequestMyProofsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
