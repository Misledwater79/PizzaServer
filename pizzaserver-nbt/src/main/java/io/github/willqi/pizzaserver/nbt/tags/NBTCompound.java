package io.github.willqi.pizzaserver.nbt.tags;

import io.github.willqi.pizzaserver.nbt.exceptions.NBTLimitException;

import java.util.*;

public class NBTCompound extends NBTContainer implements Iterable<String> {

    private final Map<String, Object> data = new HashMap<>();
    private int depth;


    public NBTCompound() {}

    public NBTCompound(String name) {
        super(name);
    }

    public byte getByte(String name) {
        return ((byte)this.data.get(name));
    }

    public NBTCompound putByte(String name, byte value) {
        this.data.put(name, value);
        return this;
    }

    public short getShort(String name) {
        return (short)this.data.get(name);
    }

    public NBTCompound putShort(String name, short value) {
        this.data.put(name, value);
        return this;
    }

    public int getInteger(String name) {
        return (int)this.data.get(name);
    }

    public NBTCompound putInteger(String name, int value) {
        this.data.put(name, value);
        return this;
    }

    public long getLong(String name) {
        return (long)this.data.get(name);
    }

    public NBTCompound putLong(String name, long value) {
        this.data.put(name, value);
        return this;
    }

    public float getFloat(String name) {
        return (float)this.data.get(name);
    }

    public NBTCompound putFloat(String name, float value) {
        this.data.put(name, value);
        return this;
    }

    public double getDouble(String name) {
        return (double)this.data.get(name);
    }

    public NBTCompound putDouble(String name, double value) {
        this.data.put(name, value);
        return this;
    }

    public String getString(String name) {
        return (String)this.data.get(name);
    }

    public NBTCompound putString(String name, String value) {
        this.data.put(name, value);
        return this;
    }

    public NBTList<Object> getList(String name) {
        return ((NBTList<Object>)this.data.get(name));
    }

    public <T> NBTCompound putList(String name, NBTList<T> value) {
        this.data.put(name, value);
        return this;
    }

    public NBTCompound getCompound(String name) {
        return (NBTCompound)this.data.get(name);
    }

    public NBTCompound putCompound(String name, NBTCompound value) {
        value.setName(name);
        this.data.put(name, value);
        value.setDepth(this.getDepth() + 1);
        return this;
    }

    public byte[] getByteArray(String name) {
        return (byte[])this.data.get(name);
    }

    public NBTCompound putByteArray(String name, byte[] value) {
        this.data.put(name, value);
        return this;
    }

    public int[] getIntegerArray(String name) {
        return (int[])this.data.get(name);
    }

    public NBTCompound putIntegerArray(String name, int[] value) {
        this.data.put(name, value);
        return this;
    }

    public long[] getLongArray(String name) {
        return (long[])this.data.get(name);
    }

    public NBTCompound putLongArray(String name, long[] value) {
        this.data.put(name, value);
        return this;
    }

    public Set<String> keySet() {
        return this.data.keySet();
    }

    public Object get(String name) {
        return this.data.get(name);
    }

    @Override
    public int getDepth() {
        return this.depth;
    }

    @Override
    public void setDepth(int depth) {
        this.depth = depth;
        if (this.depth > 512) {
            throw new NBTLimitException("Reached maximum depth of 512.");
        }
        this.data.forEach((name, tag) -> {
            if (tag instanceof NBTContainer) {
                ((NBTContainer)tag).setDepth(this.getDepth() + 1);
            }
        });
    }

    public int size() {
        return this.data.size();
    }

    @Override
    public Iterator<String> iterator() {
        return this.data.keySet().iterator();
    }

    @Override
    public int hashCode() {
        return (31 * this.name.hashCode()) + (31 * this.data.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NBTCompound) {
            NBTCompound nbtCompound = (NBTCompound)obj;
            if ((nbtCompound.size() != this.size()) || !(nbtCompound.getName().equals(this.getName()))) {
                return false;
            }

            for (String key : nbtCompound) {
                boolean bothHaveKey = this.data.containsKey(key) && nbtCompound.data.containsKey(key);
                if (!bothHaveKey) {
                    return false;
                }
                Object element = this.get(key);
                Object otherElement = nbtCompound.get(key);

                // We can't call .equals directly on the maps since it uses .equal regardless of if it's an array or not.
                // So we have to handle arrays manually
                if (element instanceof byte[] && otherElement instanceof byte[]) {
                    if (!Arrays.equals((byte[])element, (byte[])otherElement)) {
                        return false;
                    }
                } else if (element instanceof int[] && otherElement instanceof int[]) {
                    if (!Arrays.equals((int[])element, (int[])otherElement)) {
                        return false;
                    }
                } else if (element instanceof long[] && otherElement instanceof long[]) {
                    if (!Arrays.equals((long[])element, (long[])otherElement)) {
                        return false;
                    }
                } else if (!element.equals(otherElement)) { // the elements were not an array or were both not of the same time
                    return false;
                }
            }

            return true;
        }
        return false;
    }

}
