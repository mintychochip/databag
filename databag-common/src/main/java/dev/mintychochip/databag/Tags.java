package dev.mintychochip.databag;

final class Tags {

  static final byte BOOL = 1;
  static final byte INT = 2;
  static final byte LONG = 3;
  static final byte FLOAT = 4;
  static final byte DOUBLE = 5;
  static final byte STRING = 6;
  static final byte BYTES = 7;
  static final byte BYTE = 8;
  static final byte SHORT = 9;
  static final byte INTS = 10;
  static final byte LONGS = 11;
  static final byte UUID = 12;
  static final byte FORMATTED = 13;

  private Tags() {}

  static boolean known(byte tag) {
    return switch (tag) {
      case BOOL, INT, LONG, FLOAT, DOUBLE, STRING, BYTES, BYTE, SHORT, INTS, LONGS, UUID,
          FORMATTED -> true;
      default -> false;
    };
  }
}
