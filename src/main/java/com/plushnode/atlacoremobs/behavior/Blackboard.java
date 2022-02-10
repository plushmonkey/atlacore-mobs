package com.plushnode.atlacoremobs.behavior;

import com.plushnode.atlacore.internal.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.HashMap;
import java.util.Map;

public class Blackboard {
    private Map<String, Object> store = new HashMap<>();

    public boolean has(String key) {
        return store.containsKey(key);
    }

    public void set(String key, Object value) {
        store.put(key, value);
    }

    public <T> T get(String key) {
        Object result = store.get(key);

        return (T)result;
    }

    public String getString(String key) {
        Object result = store.get(key);

        return (String)result;
    }

    public Integer getInt(String key) {
        Object result = store.get(key);

        return (Integer)result;
    }

    public Long getLong(String key) {
        Object result = store.get(key);

        return (Long)result;
    }

    public Float getFloat(String key) {
        Object result = store.get(key);

        return (Float)result;
    }

    public Double getDouble(String key) {
        Object result = store.get(key);

        return (Double)result;
    }

    public Vector3D getVector(String key) {
        Object result = store.get(key);

        return (Vector3D)result;
    }

    public void clear() {
        store.clear();
    }

    public void erase(String key) {
        store.remove(key);
    }
}
