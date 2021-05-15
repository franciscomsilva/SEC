// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: HAContract.proto

package hacontract;

public final class HAContract {
  private HAContract() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_hacontract_InitMessage_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_hacontract_InitMessage_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_hacontract_Key_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_hacontract_Key_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_hacontract_GetLocation_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_hacontract_GetLocation_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_hacontract_LocationStatus_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_hacontract_LocationStatus_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_hacontract_UserAtLocation_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_hacontract_UserAtLocation_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_hacontract_Users_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_hacontract_Users_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\020HAContract.proto\022\nhacontract\032\033google/p" +
      "rotobuf/empty.proto\032\037google/protobuf/tim" +
      "estamp.proto\"<\n\013InitMessage\022\014\n\004user\030\001 \001(" +
      "\t\022\017\n\007counter\030\002 \001(\005\022\016\n\006digSig\030\003 \001(\t\"\"\n\003Ke" +
      "y\022\013\n\003key\030\001 \001(\t\022\016\n\006digSig\030\003 \001(\t\":\n\013GetLoc" +
      "ation\022\017\n\007message\030\001 \001(\t\022\n\n\002iv\030\002 \001(\t\022\016\n\006di" +
      "gSig\030\003 \001(\t\"=\n\016LocationStatus\022\017\n\007message\030" +
      "\001 \001(\t\022\n\n\002iv\030\002 \001(\t\022\016\n\006digSig\030\003 \001(\t\"=\n\016Use" +
      "rAtLocation\022\017\n\007message\030\001 \001(\t\022\n\n\002iv\030\002 \001(\t" +
      "\022\016\n\006digSig\030\003 \001(\t\"4\n\005Users\022\017\n\007message\030\001 \001" +
      "(\t\022\n\n\002iv\030\002 \001(\t\022\016\n\006digSig\030\003 \001(\t2\323\001\n\nHAPro" +
      "tocol\022K\n\024obtainLocationReport\022\027.hacontra" +
      "ct.GetLocation\032\032.hacontract.LocationStat" +
      "us\022F\n\025obtainUsersAtLocation\022\032.hacontract" +
      ".UserAtLocation\032\021.hacontract.Users\0220\n\004in" +
      "it\022\027.hacontract.InitMessage\032\017.hacontract" +
      ".KeyB\016\n\nhacontractP\001b\006proto3"
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
    internal_static_hacontract_InitMessage_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_hacontract_InitMessage_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_hacontract_InitMessage_descriptor,
        new java.lang.String[] { "User", "Counter", "DigSig", });
    internal_static_hacontract_Key_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_hacontract_Key_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_hacontract_Key_descriptor,
        new java.lang.String[] { "Key", "DigSig", });
    internal_static_hacontract_GetLocation_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_hacontract_GetLocation_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_hacontract_GetLocation_descriptor,
        new java.lang.String[] { "Message", "Iv", "DigSig", });
    internal_static_hacontract_LocationStatus_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_hacontract_LocationStatus_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_hacontract_LocationStatus_descriptor,
        new java.lang.String[] { "Message", "Iv", "DigSig", });
    internal_static_hacontract_UserAtLocation_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_hacontract_UserAtLocation_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_hacontract_UserAtLocation_descriptor,
        new java.lang.String[] { "Message", "Iv", "DigSig", });
    internal_static_hacontract_Users_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_hacontract_Users_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_hacontract_Users_descriptor,
        new java.lang.String[] { "Message", "Iv", "DigSig", });
    com.google.protobuf.EmptyProto.getDescriptor();
    com.google.protobuf.TimestampProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
