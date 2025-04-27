package com.vectorshard.vector.database.store;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class VectorStore {
    private final Map<String, float[]> vectors = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private int dimension = -1;

    public void addVector(String id, float[] vector) {
        Objects.requireNonNull(id, "ID cannot be null");
        Objects.requireNonNull(vector, "Vector cannot be null");
        if (vector.length == 0) {
            throw new IllegalArgumentException("Vector cannot be empty");
        }

        rwLock.writeLock().lock();
        try {
            if (dimension == -1) {
                dimension = vector.length;
            } else if (vector.length != dimension) {
                throw new IllegalArgumentException(
                        "Vector dimension mismatch. Expected: " + dimension + ", got: " + vector.length);
            }
            vectors.put(id, vector);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public float[] getVector(String id) {
        Objects.requireNonNull(id, "ID cannot be null");
        rwLock.readLock().lock();
        try {
            return vectors.get(id);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public boolean deleteVector(String id) {
        Objects.requireNonNull(id, "ID cannot be null");
        rwLock.writeLock().lock();
        try {
            return vectors.remove(id) != null;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public List<String> search(float[] queryVector, int k) {
        Objects.requireNonNull(queryVector, "Query vector cannot be null");
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        rwLock.readLock().lock();
        try {
            if (dimension == -1) {
                return Collections.emptyList();
            }
            if (queryVector.length != dimension) {
                throw new IllegalArgumentException(
                        "Query vector dimension mismatch. Expected: " + dimension + ", got: " + queryVector.length);
            }

            return vectors.entrySet().parallelStream()
                    .map(entry -> Map.entry(entry.getKey(), cosineSimilarity(queryVector, entry.getValue())))
                    .sorted((e1, e2) -> Float.compare(e2.getValue(), e1.getValue()))
                    .limit(k)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private float cosineSimilarity(float[] v1, float[] v2) {
        float dot = 0f, normV1 = 0f, normV2 = 0f;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            normV1 += v1[i] * v1[i];
            normV2 += v2[i] * v2[i];
        }
        if (normV1 == 0 || normV2 == 0) {
            return 0f; // handle zero vector case
        }
        return dot / ((float) (Math.sqrt(normV1) * Math.sqrt(normV2)));
    }
}
