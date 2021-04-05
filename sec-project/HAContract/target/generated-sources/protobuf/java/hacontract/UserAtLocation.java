// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: HAContract.proto

package hacontract;

/**
 * Protobuf type {@code hacontract.UserAtLocation}
 */
public  final class UserAtLocation extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:hacontract.UserAtLocation)
    UserAtLocationOrBuilder {
private static final long serialVersionUID = 0L;
  // Use UserAtLocation.newBuilder() to construct.
  private UserAtLocation(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private UserAtLocation() {
    xCoord_ = 0;
    yCoord_ = 0;
    epoch_ = 0;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private UserAtLocation(
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
          case 8: {

            xCoord_ = input.readInt32();
            break;
          }
          case 16: {

            yCoord_ = input.readInt32();
            break;
          }
          case 24: {

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
    return hacontract.HAContract.internal_static_hacontract_UserAtLocation_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return hacontract.HAContract.internal_static_hacontract_UserAtLocation_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            hacontract.UserAtLocation.class, hacontract.UserAtLocation.Builder.class);
  }

  public static final int XCOORD_FIELD_NUMBER = 1;
  private int xCoord_;
  /**
   * <code>int32 xCoord = 1;</code>
   */
  public int getXCoord() {
    return xCoord_;
  }

  public static final int YCOORD_FIELD_NUMBER = 2;
  private int yCoord_;
  /**
   * <code>int32 yCoord = 2;</code>
   */
  public int getYCoord() {
    return yCoord_;
  }

  public static final int EPOCH_FIELD_NUMBER = 3;
  private int epoch_;
  /**
   * <code>int32 epoch = 3;</code>
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
    if (xCoord_ != 0) {
      output.writeInt32(1, xCoord_);
    }
    if (yCoord_ != 0) {
      output.writeInt32(2, yCoord_);
    }
    if (epoch_ != 0) {
      output.writeInt32(3, epoch_);
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (xCoord_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(1, xCoord_);
    }
    if (yCoord_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(2, yCoord_);
    }
    if (epoch_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(3, epoch_);
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
    if (!(obj instanceof hacontract.UserAtLocation)) {
      return super.equals(obj);
    }
    hacontract.UserAtLocation other = (hacontract.UserAtLocation) obj;

    boolean result = true;
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

  public static hacontract.UserAtLocation parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static hacontract.UserAtLocation parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static hacontract.UserAtLocation parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static hacontract.UserAtLocation parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static hacontract.UserAtLocation parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static hacontract.UserAtLocation parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static hacontract.UserAtLocation parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static hacontract.UserAtLocation parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static hacontract.UserAtLocation parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static hacontract.UserAtLocation parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static hacontract.UserAtLocation parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static hacontract.UserAtLocation parseFrom(
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
  public static Builder newBuilder(hacontract.UserAtLocation prototype) {
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
   * Protobuf type {@code hacontract.UserAtLocation}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:hacontract.UserAtLocation)
      hacontract.UserAtLocationOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return hacontract.HAContract.internal_static_hacontract_UserAtLocation_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return hacontract.HAContract.internal_static_hacontract_UserAtLocation_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              hacontract.UserAtLocation.class, hacontract.UserAtLocation.Builder.class);
    }

    // Construct using hacontract.UserAtLocation.newBuilder()
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
      xCoord_ = 0;

      yCoord_ = 0;

      epoch_ = 0;

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return hacontract.HAContract.internal_static_hacontract_UserAtLocation_descriptor;
    }

    public hacontract.UserAtLocation getDefaultInstanceForType() {
      return hacontract.UserAtLocation.getDefaultInstance();
    }

    public hacontract.UserAtLocation build() {
      hacontract.UserAtLocation result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public hacontract.UserAtLocation buildPartial() {
      hacontract.UserAtLocation result = new hacontract.UserAtLocation(this);
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
      if (other instanceof hacontract.UserAtLocation) {
        return mergeFrom((hacontract.UserAtLocation)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(hacontract.UserAtLocation other) {
      if (other == hacontract.UserAtLocation.getDefaultInstance()) return this;
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
      hacontract.UserAtLocation parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (hacontract.UserAtLocation) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private int xCoord_ ;
    /**
     * <code>int32 xCoord = 1;</code>
     */
    public int getXCoord() {
      return xCoord_;
    }
    /**
     * <code>int32 xCoord = 1;</code>
     */
    public Builder setXCoord(int value) {
      
      xCoord_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 xCoord = 1;</code>
     */
    public Builder clearXCoord() {
      
      xCoord_ = 0;
      onChanged();
      return this;
    }

    private int yCoord_ ;
    /**
     * <code>int32 yCoord = 2;</code>
     */
    public int getYCoord() {
      return yCoord_;
    }
    /**
     * <code>int32 yCoord = 2;</code>
     */
    public Builder setYCoord(int value) {
      
      yCoord_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 yCoord = 2;</code>
     */
    public Builder clearYCoord() {
      
      yCoord_ = 0;
      onChanged();
      return this;
    }

    private int epoch_ ;
    /**
     * <code>int32 epoch = 3;</code>
     */
    public int getEpoch() {
      return epoch_;
    }
    /**
     * <code>int32 epoch = 3;</code>
     */
    public Builder setEpoch(int value) {
      
      epoch_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 epoch = 3;</code>
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


    // @@protoc_insertion_point(builder_scope:hacontract.UserAtLocation)
  }

  // @@protoc_insertion_point(class_scope:hacontract.UserAtLocation)
  private static final hacontract.UserAtLocation DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new hacontract.UserAtLocation();
  }

  public static hacontract.UserAtLocation getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<UserAtLocation>
      PARSER = new com.google.protobuf.AbstractParser<UserAtLocation>() {
    public UserAtLocation parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new UserAtLocation(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<UserAtLocation> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<UserAtLocation> getParserForType() {
    return PARSER;
  }

  public hacontract.UserAtLocation getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
