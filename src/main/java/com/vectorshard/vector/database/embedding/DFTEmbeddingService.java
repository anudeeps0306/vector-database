package com.vectorshard.vector.database.embedding;
import java.nio.charset.StandardCharsets;

public class DFTEmbeddingService implements EmbeddingService {

    private static final int VECTOR_DIMENSION = 10;

    @Override
    public float[] embed(String statement) {
        if (statement == null || statement.isEmpty()) {
            throw new IllegalArgumentException("Statement cannot be null or empty");
        }

        int n = statement.length();
        float[] vector = new float[VECTOR_DIMENSION];

        for (int k = 0; k < VECTOR_DIMENSION; k++) {
            double real = 0, imag = 0;
            for (int t = 0; t < n; t++) {
                double angle = 2 * Math.PI * t * k / n;
                int code = (int) statement.charAt(t);
                real += code * Math.cos(angle);
                imag -= code * Math.sin(angle);
            }
            vector[k] = (float) Math.sqrt(real * real + imag * imag);
        }

        // Normalize
        float norm = 0f;
        for (float v : vector) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }

        return vector;
    }
}
