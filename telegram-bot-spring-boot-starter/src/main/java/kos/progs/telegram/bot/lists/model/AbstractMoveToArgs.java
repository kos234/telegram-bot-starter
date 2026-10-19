package kos.progs.telegram.bot.lists.model;

import kos.progs.telegram.bot.button.model.ButtonArgs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.ByteBuffer;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractMoveToArgs implements ButtonArgs {
    private int offset;
    private int startMessageId;

    @Override
    public void fromByteBuffer(ByteBuffer byteBuffer) {
        offset = byteBuffer.getInt();
        startMessageId = byteBuffer.getInt();
        extraFromByteBuffer(byteBuffer);
    }

    @Override
    public ByteBuffer toByteBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[Integer.BYTES * 2 + getExtraBytesSize()]);
        byteBuffer.putInt(offset);
        byteBuffer.putInt(startMessageId);
        extraToByteBuffer(byteBuffer);
        return byteBuffer;
    }

    protected abstract int getExtraBytesSize();
    protected abstract void extraFromByteBuffer(ByteBuffer byteBuffer);
    protected abstract void extraToByteBuffer(ByteBuffer byteBuffer);
}
