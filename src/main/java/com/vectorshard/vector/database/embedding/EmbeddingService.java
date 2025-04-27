package com.vectorshard.vector.database.embedding;

public interface EmbeddingService {
    float[] embed(String statement);
}
