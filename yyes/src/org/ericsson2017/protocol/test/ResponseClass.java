// Generated by Cap'n Proto compiler, DO NOT EDIT
// source: Response.capnp

package org.ericsson2017.protocol.test;

public final class ResponseClass {
  public static class Response {
    public static final org.capnproto.StructSize STRUCT_SIZE = new org.capnproto.StructSize((short)1,(short)2);
    public static final class Factory extends org.capnproto.StructFactory<Builder, Reader> {
      public Factory() {
      }
      public final Reader constructReader(org.capnproto.SegmentReader segment, int data,int pointers, int dataSize, short pointerCount, int nestingLimit) {
        return new Reader(segment,data,pointers,dataSize,pointerCount,nestingLimit);
      }
      public final Builder constructBuilder(org.capnproto.SegmentBuilder segment, int data,int pointers, int dataSize, short pointerCount) {
        return new Builder(segment, data, pointers, dataSize, pointerCount);
      }
      public final org.capnproto.StructSize structSize() {
        return Response.STRUCT_SIZE;
      }
      public final Reader asReader(Builder builder) {
        return builder.asReader();
      }
    }
    public static final Factory factory = new Factory();
    public static final org.capnproto.StructList.Factory<Builder,Reader> listFactory =
      new org.capnproto.StructList.Factory<Builder, Reader>(factory);
    public static final class Builder extends org.capnproto.StructBuilder {
      Builder(org.capnproto.SegmentBuilder segment, int data, int pointers,int dataSize, short pointerCount){
        super(segment, data, pointers, dataSize, pointerCount);
      }
      public Which which() {
        switch(_getShortField(0)) {
          case 0 : return Which.BUGFIX;
          case 1 : return Which.END;
          default: return Which._NOT_IN_SCHEMA;
        }
      }
      public final Reader asReader() {
        return new Reader(segment, data, pointers, dataSize, pointerCount, 0x7fffffff);
      }
      public final boolean hasStatus() {
        return !_pointerFieldIsNull(0);
      }
      public final org.capnproto.Text.Builder getStatus() {
        return _getPointerField(org.capnproto.Text.factory, 0, null, 0, 0);
      }
      public final void setStatus(org.capnproto.Text.Reader value) {
        _setPointerField(org.capnproto.Text.factory, 0, value);
      }
      public final void setStatus(String value) {
        _setPointerField(org.capnproto.Text.factory, 0, new org.capnproto.Text.Reader(value));
      }
      public final org.capnproto.Text.Builder initStatus(int size) {
        return _initPointerField(org.capnproto.Text.factory, 0, size);
      }
      public final boolean isBugfix() {
        return which() == Response.Which.BUGFIX;
      }
      public final org.ericsson2017.protocol.test.BugfixClass.Bugfix.Builder getBugfix() {
        assert which() == Response.Which.BUGFIX:
                    "Must check which() before get()ing a union member.";
        return _getPointerField(org.ericsson2017.protocol.test.BugfixClass.Bugfix.factory, 1, null, 0);
      }
      public final void setBugfix(org.ericsson2017.protocol.test.BugfixClass.Bugfix.Reader value) {
        _setShortField(0, (short)Response.Which.BUGFIX.ordinal());
        _setPointerField(org.ericsson2017.protocol.test.BugfixClass.Bugfix.factory,1, value);
      }
      public final org.ericsson2017.protocol.test.BugfixClass.Bugfix.Builder initBugfix() {
        _setShortField(0, (short)Response.Which.BUGFIX.ordinal());
        return _initPointerField(org.ericsson2017.protocol.test.BugfixClass.Bugfix.factory,1, 0);
      }
      public final boolean isEnd() {
        return which() == Response.Which.END;
      }
      public final boolean getEnd() {
        assert which() == Response.Which.END:
                    "Must check which() before get()ing a union member.";
        return _getBooleanField(16);
      }
      public final void setEnd(boolean value) {
        _setShortField(0, (short)Response.Which.END.ordinal());
        _setBooleanField(16, value);
      }

    }

    public static final class Reader extends org.capnproto.StructReader {
      Reader(org.capnproto.SegmentReader segment, int data, int pointers,int dataSize, short pointerCount, int nestingLimit){
        super(segment, data, pointers, dataSize, pointerCount, nestingLimit);
      }

      public Which which() {
        switch(_getShortField(0)) {
          case 0 : return Which.BUGFIX;
          case 1 : return Which.END;
          default: return Which._NOT_IN_SCHEMA;
        }
      }
      public boolean hasStatus() {
        return !_pointerFieldIsNull(0);
      }
      public org.capnproto.Text.Reader getStatus() {
        return _getPointerField(org.capnproto.Text.factory, 0, null, 0, 0);
      }

      public final boolean isBugfix() {
        return which() == Response.Which.BUGFIX;
      }
      public boolean hasBugfix() {
        return !_pointerFieldIsNull(1);
      }
      public org.ericsson2017.protocol.test.BugfixClass.Bugfix.Reader getBugfix() {
        assert which() == Response.Which.BUGFIX:
                    "Must check which() before get()ing a union member.";
        return _getPointerField(org.ericsson2017.protocol.test.BugfixClass.Bugfix.factory,1,null, 0);
      }

      public final boolean isEnd() {
        return which() == Response.Which.END;
      }
      public final boolean getEnd() {
        assert which() == Response.Which.END:
                    "Must check which() before get()ing a union member.";
        return _getBooleanField(16);
      }

    }

    public enum Which {
      BUGFIX,
      END,
      _NOT_IN_SCHEMA,
    }
  }



public static final class Schemas {
public static final org.capnproto.SegmentReader b_ac45fd4d695dcf29 =
   org.capnproto.GeneratedClassSupport.decodeRawBytes(
   "\u0000\u0000\u0000\u0000\u0005\u0000\u0006\u0000" +
   "\u0029\u00cf\u005d\u0069\u004d\u00fd\u0045\u00ac" +
   "\u000f\u0000\u0000\u0000\u0001\u0000\u0001\u0000" +
   "\u00fa\u008e\u0052\u00bb\u0051\u00d8\u0037\u00c0" +
   "\u0002\u0000\u0007\u0000\u0000\u0000\u0002\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0015\u0000\u0000\u0000\u00c2\u0000\u0000\u0000" +
   "\u001d\u0000\u0000\u0000\u0007\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0019\u0000\u0000\u0000\u00af\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0052\u0065\u0073\u0070\u006f\u006e\u0073\u0065" +
   "\u002e\u0063\u0061\u0070\u006e\u0070\u003a\u0052" +
   "\u0065\u0073\u0070\u006f\u006e\u0073\u0065\u0000" +
   "\u0000\u0000\u0000\u0000\u0001\u0000\u0001\u0000" +
   "\u000c\u0000\u0000\u0000\u0003\u0000\u0004\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0045\u0000\u0000\u0000\u003a\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0040\u0000\u0000\u0000\u0003\u0000\u0001\u0000" +
   "\u004c\u0000\u0000\u0000\u0002\u0000\u0001\u0000" +
   "\u0001\u0000\u00ff\u00ff\u0001\u0000\u0000\u0000" +
   "\u0000\u0000\u0001\u0000\u0001\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0049\u0000\u0000\u0000\u003a\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0044\u0000\u0000\u0000\u0003\u0000\u0001\u0000" +
   "\u0050\u0000\u0000\u0000\u0002\u0000\u0001\u0000" +
   "\u0002\u0000\u00fe\u00ff\u0010\u0000\u0000\u0000" +
   "\u0000\u0000\u0001\u0000\u0002\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u004d\u0000\u0000\u0000\"\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0048\u0000\u0000\u0000\u0003\u0000\u0001\u0000" +
   "\u0054\u0000\u0000\u0000\u0002\u0000\u0001\u0000" +
   "\u0073\u0074\u0061\u0074\u0075\u0073\u0000\u0000" +
   "\u000c\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u000c\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0062\u0075\u0067\u0066\u0069\u0078\u0000\u0000" +
   "\u0010\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u005d\u00a7\u00ee\u0011\u0065\u007a\u00ec\u00f0" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0010\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0065\u006e\u0064\u0000\u0000\u0000\u0000\u0000" +
   "\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
   "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + "");
}
}

