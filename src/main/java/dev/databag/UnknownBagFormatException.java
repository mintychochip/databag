package dev.databag;

/**
 * The byte[] starts with a DataBag magic header but the envelope version is
 * not readable by this library.
 */
public final class UnknownBagFormatException extends IllegalArgumentException {

  private final int version;

  public UnknownBagFormatException(int version) {
    super("unknown DataBag format version " + version);
    this.version = version;
  }

  public int version() {
    return version;
  }
}
