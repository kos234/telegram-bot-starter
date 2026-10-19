package kos.progs.telegram.bot.button.model;

import java.nio.ByteBuffer;

public class EmptyArgs implements ButtonArgs {
    public static final EmptyArgs INSTANCE = new EmptyArgs();

    @Override
    public void fromByteBuffer(ByteBuffer byteBuffer) {
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(new byte[0]);
    }
}
