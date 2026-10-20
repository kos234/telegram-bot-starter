package kos.progs.telegram.bot.button.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

@Getter
@Setter
public class PrimitiveArgs<T> implements ButtonArgs {
    private T value;
    private final int bytes;
    private final Consumer<ByteBuffer> readFrom;
    private final Consumer<ByteBuffer> writeTo;

    public PrimitiveArgs(T primitive) {
        this((Class<T>) primitive.getClass());
        this.value = primitive;
    }

    public PrimitiveArgs(Class<T> type) {
        if(type == Integer.class){
            bytes = Integer.BYTES;
            readFrom = byteBuffer -> this.value = type.cast(byteBuffer.getInt());
            writeTo = byteBuffer -> byteBuffer.putInt((int)this.value);
        }else if(type == Long.class){
            bytes = Long.BYTES;
            readFrom = byteBuffer -> this.value = type.cast(byteBuffer.getLong());
            writeTo = byteBuffer -> byteBuffer.putLong((long)this.value);
        }else if(type == Short.class){
            bytes = Short.BYTES;
            readFrom = byteBuffer -> this.value = type.cast(byteBuffer.getShort());
            writeTo = byteBuffer -> byteBuffer.putShort((short)this.value);
        }else if(type == Byte.class){
            bytes = Byte.BYTES;
            readFrom = byteBuffer -> this.value = type.cast(byteBuffer.get());
            writeTo = byteBuffer -> byteBuffer.put((byte)this.value);
        }else if(type == Boolean.class){
            bytes = Byte.BYTES;
            readFrom = byteBuffer -> this.value = type.cast(byteBuffer.get() == 1);
            writeTo = byteBuffer -> byteBuffer.put((byte) ((boolean)this.value ? 1 : 0));
        }else {
            throw new RuntimeException("класс " + type.getName() + " не поддерживается");
        }
    }



    @Override
    public void fromByteBuffer(ByteBuffer byteBuffer) {
        this.readFrom.accept(byteBuffer);
    }

    @Override
    public ByteBuffer toByteBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[this.bytes]);
        this.writeTo.accept(byteBuffer);
        return byteBuffer;
    }
}