package kos.progs.telegram.bot.button.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.ByteBuffer;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrimitiveArgs<T> implements ButtonArgs {
    private T value;

    @Override
    public ByteBuffer toByteBuffer() {
        if (value instanceof Long l) {
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(l);
            return buffer;
        } else if (value instanceof Integer i) {
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.putInt(i);
            return buffer;
        } else if (value instanceof String s) {
            return ByteBuffer.wrap(s.getBytes());
        }
        return ByteBuffer.wrap(new byte[0]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fromByteBuffer(ByteBuffer byteBuffer) {
        if (value instanceof Long) {
            value = (T) Long.valueOf(byteBuffer.getLong());
        } else if (value instanceof Integer) {
            value = (T) Integer.valueOf(byteBuffer.getInt());
        }
    }
}
