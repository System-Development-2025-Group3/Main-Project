package application.studyspace.services.DataBase;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDHelper {

    /**
     * Converts a UUID into a 16-byte array representation.
     *
     * @param uuid the UUID to convert
     * @return a byte array representing the UUID
     */
    public static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    /**
     * Converts a byte array (BINARY(16) format) into a UUID.
     *
     * @param bytes the byte array representing the UUID
     * @return the UUID object
     */
    public static UUID BytesToUUID(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }

    /**
     * Converts a string to a UUID.
     *
     * @param uuidString The string representation of the UUID.
     * @return The UUID object.
     */
    public static UUID stringToUUID(String uuidString) {
        if (uuidString == null || uuidString.isEmpty()) {
            throw new IllegalArgumentException("UUID string cannot be null or empty.");
        }
        return UUID.fromString(uuidString);
    }

    /**
     * Converts a UUID to a string.
     *
     * @param uuid The UUID object to convert.
     * @return The string representation of the UUID.
     */
    public static String uuidToString(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null.");
        }
        return uuid.toString();
    }

}
