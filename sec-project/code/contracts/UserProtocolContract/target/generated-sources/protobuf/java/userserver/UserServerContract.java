// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: UserServerContract.proto

package userserver;

public final class UserServerContract {
  private UserServerContract() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_userserver_InitMessage_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_userserver_InitMessage_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_userserver_Key_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_userserver_Key_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_userserver_LocationReport_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_userserver_LocationReport_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_userserver_LocationResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_userserver_LocationResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_userserver_GetLocation_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_userserver_GetLocation_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_userserver_LocationStatus_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_userserver_LocationStatus_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\030UserServerContract.proto\022\nuserserver\032\033" +
      "google/protobuf/empty.proto\032\037google/prot" +
      "obuf/timestamp.proto\"\033\n\013InitMessage\022\014\n\004u" +
      "ser\030\001 \001(\t\"\022\n\003Key\022\013\n\003key\030\001 \001(\t\";\n\016Locatio" +
      "nReport\022\014\n\004user\030\001 \001(\t\022\017\n\007message\030\002 \001(\t\022\n" +
      "\n\002iv\030\003 \001(\t\"/\n\020LocationResponse\022\017\n\007messag" +
      "e\030\001 \001(\t\022\n\n\002iv\030\002 \001(\t\"8\n\013GetLocation\022\014\n\004us" +
      "er\030\001 \001(\t\022\017\n\007message\030\002 \001(\t\022\n\n\002iv\030\003 \001(\t\"-\n" +
      "\016LocationStatus\022\017\n\007message\030\001 \001(\t\022\n\n\002iv\030\002" +
      " \001(\t2\335\001\n\nUserServer\0220\n\004init\022\027.userserver" +
      ".InitMessage\032\017.userserver.Key\022P\n\024submitL" +
      "ocationReport\022\032.userserver.LocationRepor" +
      "t\032\034.userserver.LocationResponse\022K\n\024obtai" +
      "nLocationReport\022\027.userserver.GetLocation" +
      "\032\032.userserver.LocationStatusB\016\n\nuserserv" +
      "erP\001b\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.EmptyProto.getDescriptor(),
          com.google.protobuf.TimestampProto.getDescriptor(),
        }, assigner);
    internal_static_userserver_InitMessage_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_userserver_InitMessage_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_userserver_InitMessage_descriptor,
        new java.lang.String[] { "User", });
    internal_static_userserver_Key_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_userserver_Key_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_userserver_Key_descriptor,
        new java.lang.String[] { "Key", });
    internal_static_userserver_LocationReport_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_userserver_LocationReport_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_userserver_LocationReport_descriptor,
        new java.lang.String[] { "User", "Message", "Iv", });
    internal_static_userserver_LocationResponse_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_userserver_LocationResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_userserver_LocationResponse_descriptor,
        new java.lang.String[] { "Message", "Iv", });
    internal_static_userserver_GetLocation_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_userserver_GetLocation_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_userserver_GetLocation_descriptor,
        new java.lang.String[] { "User", "Message", "Iv", });
    internal_static_userserver_LocationStatus_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_userserver_LocationStatus_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_userserver_LocationStatus_descriptor,
        new java.lang.String[] { "Message", "Iv", });
    com.google.protobuf.EmptyProto.getDescriptor();
    com.google.protobuf.TimestampProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
