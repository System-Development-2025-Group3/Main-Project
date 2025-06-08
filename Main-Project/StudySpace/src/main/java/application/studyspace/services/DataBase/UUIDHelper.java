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
}
