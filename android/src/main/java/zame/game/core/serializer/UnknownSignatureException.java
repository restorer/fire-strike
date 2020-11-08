package zame.game.core.serializer;

@SuppressWarnings("WeakerAccess")
public class UnknownSignatureException extends Exception {
    private static final long serialVersionUID = 0L;

    static final String INVALID_SIGNATURE = "Invalid signature";
    static final String UNKNOWN_SIGNATURE = "Unknown signature";
    static final String INVALID_VERSION = "Invalid version";
    static final String UNSUPPORTED_VERSION = "Unsupported version";
    static final String INVALID_CHECKSUM = "Invalid checksum";

    UnknownSignatureException(String detailMessage) {
        super(detailMessage);
    }

    UnknownSignatureException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
