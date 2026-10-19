package kos.progs.telegram.bot.button.model;

import java.nio.ByteBuffer;

public interface ButtonArgs {
    void fromByteBuffer(ByteBuffer byteBuffer);
    ByteBuffer toByteBuffer();

    default byte[] toByteArray() {
        ByteBuffer buf = toByteBuffer();
        return buf != null ? buf.array() : new byte[0];
    }

    default void fromByteArray(ByteBuffer buffer) {
        fromByteBuffer(buffer);
    }
}
