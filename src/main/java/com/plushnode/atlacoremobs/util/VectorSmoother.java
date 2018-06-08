package com.plushnode.atlacoremobs.util;

import com.plushnode.atlacore.internal.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

public class VectorSmoother {
    private List<Vector3D> values = new ArrayList<>();
    private int size;
    private int index;

    public VectorSmoother(int size) {
        this.size = size;
    }

    public Vector3D add(Vector3D v) {
        if (values.size() < size) {
            values.add(v);
        } else {
            values.set(index, v);
            index = (index + 1) % size;
        }

        return get();
    }

    public Vector3D get() {
        if (values.isEmpty()) {
            return Vector3D.ZERO;
        }

        Vector3D accum = Vector3D.ZERO;

        for (Vector3D v : values) {
            accum = accum.add(v);
        }

        return accum.scalarMultiply(1.0 / values.size());
    }

    public void clear() {
        values.clear();
    }
}
