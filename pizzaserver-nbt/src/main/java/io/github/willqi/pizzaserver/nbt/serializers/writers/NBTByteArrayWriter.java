package io.github.willqi.pizzaserver.nbt.serializers.writers;

import io.github.willqi.pizzaserver.nbt.streams.ld.LittleEndianDataOutputStream;
import io.github.willqi.pizzaserver.nbt.tags.NBTByteArray;

import java.io.IOException;

public class NBTByteArrayWriter extends NBTWriter<NBTByteArray> {

    public NBTByteArrayWriter(LittleEndianDataOutputStream stream) {
        super(stream);
    }

    @Override
    protected void writeTagData(NBTByteArray tag) throws IOException {
        this.stream.writeInt(tag.getData().length);
        for (byte b : tag.getData()) {
            this.stream.writeByte(b);
        }
    }

}
