// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: UserProtocolContract.proto

package userprotocol;

/**
 * Protobuf type {@code userprotocol.LocationRequest}
 */
public  final class LocationRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:userprotocol.LocationRequest)
    LocationRequestOrBuilder {
private static final long serialVersionUID = 0L;
  // Use LocationRequest.newBuilder() to construct.
  private LocationRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private LocationRequest() {
    id_ = "";
    xCoord_ = 0;
    yCoord_ = 0;
    epoch_ = 0;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private LocationRequest(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    int mutable_bitField0_ = 0;
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          default: {
            if (!parseUnknownFieldProto3(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
          case 10: {
            java.lang.String s = input.readStringRequireUtf8();

            id_ = s;
            break;
          }
          case 16: {

            xCoord_ = input.readInt32();
            break;
          }
          case 24: {

            yCoord_ = input.readInt32();
            break;
          }
          case 32: {

            epoch_ = input.readInt32();
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return userprotocol.UserProtocolContract.internal_static_userprotocol_LocationRequest_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return userprotocol.UserProtocolContract.internal_static_userprotocol_LocationRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            userprotocol.LocationRequest.class, userprotocol.LocationRequest.Builder.class);
  }

  public static final int ID_FIELD_NUMBER = 1;
  private volatile java.lang.Object id_;
  /**
   * <code>string id = 1;</code>
   */
  public java.lang.String getId() {
    java.lang.Object ref = id_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      id_ = s;
      return s;
    }
  }
  /**
   * <code>string id = 1;</code>
   */
  public com.google.protobuf.ByteString
      getIdBytes() {
    java.lang.Object ref = id_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      id_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int XCOORD_FIELD_NUMBER = 2;
  private int xCoord_;
  /**
   * <code>int32 xCoord = 2;</code>
   */
  public int getXCoord() {
    return xCoord_;
  }

  public static final int YCOORD_FIELD_NUMBER = 3;
  private int yCoord_;
  /**
   * <code>int32 yCoord = 3;</code>
   */
  public int getYCoord() {
    return yCoord_;
  }

  public static final int EPOCH_FIELD_NUMBER = 4;
  private int epoch_;
  /**
   * <code>int32 epoch = 4;</code>
   */
  public int getEpoch() {
    return epoch_;
  }

  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!getIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, id_);
    }
    if (xCoord_ != 0) {
      output.writeInt32(2, xCoord_);
    }
    if (yCoord_ != 0) {
      output.writeInt32(3, yCoord_);
    }
    if (epoch_ != 0) {
      output.writeInt32(4, epoch_);
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, id_);
    }
    if (xCoord_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(2, xCoord_);
    }
    if (yCoord_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(3, yCoord_);
    }
    if (epoch_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(4, epoch_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof userprotocol.LocationRequest)) {
      return super.equals(obj);
    }
    userprotocol.LocationRequest other = (userprotocol.LocationRequest) obj;

    boolean result = true;
    result = result && getId()
        .equals(other.getId());
    result = result && (getXCoord()
        == other.getXCoord());
    result = result && (getYCoord()
        == other.getYCoord());
    result = result && (getEpoch()
        == other.getEpoch());
    result = result && unknownFields.equals(other.unknownFields);
    return result;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + ID_FIELD_NUMBER;
    hash = (53 * hash) + getId().hashCode();
    hash = (37 * hash) + XCOORD_FIELD_NUMBER;
    hash = (53 * hash) + getXCoord();
    hash = (37 * hash) + YCOORD_FIELD_NUMBER;
    hash = (53 * hash) + getYCoord();
    hash = (37 * hash) + EPOCH_FIELD_NUMBER;
    hash = (53 * hash) + getEpoch();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static userprotocol.LocationRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static userprotocol.LocationRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static userprotocol.LocationRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static userprotocol.LocationRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static userprotocol.LocationRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static userprotocol.LocationRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static userprotocol.LocationRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static userprotocol.LocationRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static userprotocol.LocationRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static userprotocol.LocationRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static userprotocol.LocationRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static userprotocol.LocationRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(userprotocol.LocationRequest prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code userprotocol.LocationRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:userprotocol.LocationRequest)
      userprotocol.LocationRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return userprotocol.UserProtocolContract.internal_static_userprotocol_LocationRequest_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return userprotocol.UserProtocolContract.internal_static_userprotocol_LocationRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              userprotocol.LocationRequest.class, userprotocol.LocationRequest.Builder.class);
    }

    // Construct using userprotocol.LocationRequest.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    public Builder clear() {
      super.clear();
      id_ = "";

      xCoord_ = 0;

      yCoord_ = 0;

      epoch_ = 0;

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return userprotocol.UserProtocolContract.internal_static_userprotocol_LocationRequest_descriptor;
    }

    public userprotocol.LocationRequest getDefaultInstanceForType() {
      return userprotocol.LocationRequest.getDefaultInstance();
    }

    public userprotocol.LocationRequest build() {
      userprotocol.LocationRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public userprotocol.LocationRequest buildPartial() {
      userprotocol.LocationRequest result = new userprotocol.LocationRequest(this);
      result.id_ = id_;
      result.xCoord_ = xCoord_;
      result.yCoord_ = yCoord_;
      result.epoch_ = epoch_;
      onBuilt();
      return result;
    }

    public Builder clone() {
      return (Builder) super.clone();
    }
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return (Builder) super.setField(field, value);
    }
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return (Builder) super.clearField(field);
    }
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return (Builder) super.clearOneof(oneof);
    }
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return (Builder) super.setRepeatedField(field, index, value);
    }
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return (Builder) super.addRepeatedField(field, value);
    }
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof userprotocol.LocationRequest) {
        return mergeFrom((userprotocol.LocationRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(userprotocol.LocationRequest other) {
      if (other == userprotocol.LocationRequest.getDefaultInstance()) return this;
      if (!other.getId().isEmpty()) {
        id_ = other.id_;
        onChanged();
      }
      if (other.getXCoord() != 0) {
        setXCoord(other.getXCoord());
      }
      if (other.getYCoord() != 0) {
        setYCoord(other.getYCoord());
      }
      if (other.getEpoch() != 0) {
        setEpoch(other.getEpoch());
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    public final boolean isInitialized() {
      return true;
    }

    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      userprotocol.LocationRequest parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (userprotocol.LocationRequest) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object id_ = "";
    /**
     * <code>string id = 1;</code>
     */
    public java.lang.String getId() {
      java.lang.Object ref = id_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        id_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string id = 1;</code>
     */
    public com.google.protobuf.ByteString
        getIdBytes() {
      java.lang.Object ref = id_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        id_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string id = 1;</code>
     */
    public Builder setId(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      id_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string id = 1;</code>
     */
    public Builder clearId() {
      
      id_ = getDefaultInstance().getId();
      onChanged();
      return this;
    }
    /**
     * <code>string id = 1;</code>
     */
    public Builder setIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      id_ = value;
      onChanged();
      return this;
    }

    private int xCoord_ ;
    /**
     * <code>int32 xCoord = 2;</code>
     */
    public int getXCoord() {
      return xCoord_;
    }
    /**
     * <code>int32 xCoord = 2;</code>
     */
    public Builder setXCoord(int value) {
      
      xCoord_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 xCoord = 2;</code>
     */
    public Builder clearXCoord() {
      
      xCoord_ = 0;
      onChanged();
      return this;
    }

    private int yCoord_ ;
    /**
     * <code>int32 yCoord = 3;</code>
     */
    public int getYCoord() {
      return yCoord_;
    }
    /**
     * <code>int32 yCoord = 3;</code>
     */
    public Builder setYCoord(int value) {
      
      yCoord_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 yCoord = 3;</code>
     */
    public Builder clearYCoord() {
      
      yCoord_ = 0;
      onChanged();
      return this;
    }

    private int epoch_ ;
    /**
     * <code>int32 epoch = 4;</code>
     */
    public int getEpoch() {
      return epoch_;
    }
    /**
     * <code>int32 epoch = 4;</code>
     */
    public Builder setEpoch(int value) {
      
      epoch_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 epoch = 4;</code>
     */
    public Builder clearEpoch() {
      
      epoch_ = 0;
      onChanged();
      return this;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:userprotocol.LocationRequest)
  }

  // @@protoc_insertion_point(class_scope:userprotocol.LocationRequest)
  private static final userprotocol.LocationRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new userprotocol.LocationRequest();
  }

  public static userprotocol.LocationRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<LocationRequest>
      PARSER = new com.google.protobuf.AbstractParser<LocationRequest>() {
    public LocationRequest parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new LocationRequest(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<LocationRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<LocationRequest> getParserForType() {
    return PARSER;
  }

  public userprotocol.LocationRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

