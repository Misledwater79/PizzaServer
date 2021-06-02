package io.github.willqi.pizzaserver.nbt.serializers.readers;

import io.github.willqi.pizzaserver.nbt.streams.ld.LittleEndianDataInputStream;
import io.github.willqi.pizzaserver.nbt.tags.NBTTag;

import java.io.IOException;

public abstract class NBTReader<T extends NBTTag> {

    protected final LittleEndianDataInputStream stream;


    public NBTReader(LittleEndianDataInputStream stream) {
        this.stream = stream;
    }

    public T read() throws IOException {
        String name = this.stream.readUTF();
        return this.parse(name);
    }

    protected abstract T parse(String tagName) throws IOException;

}
