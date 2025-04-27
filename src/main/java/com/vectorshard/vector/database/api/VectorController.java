package com.vectorshard.vector.database.api;


import com.vectorshard.vector.database.embedding.EmbeddingService;
import com.vectorshard.vector.database.embedding.DFTEmbeddingService;
import com.vectorshard.vector.database.store.VectorShardManager;
import com.vectorshard.vector.database.store.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/vectors")
public class VectorController {

    private final VectorShardManager shardManager;
    private final EmbeddingService embeddingService;

    public VectorController() {
        this.shardManager = new VectorShardManager(4);
        this.embeddingService = new DFTEmbeddingService();
    }

    // DTO for add/update vector by statement
    public static class VectorRequest {
        public String id;
        public String statement;
    }

    @PostMapping
    public ResponseEntity<String> addOrUpdateVector(@RequestBody VectorRequest request) {
        if (request.id == null || request.id.isEmpty()) {
            return ResponseEntity.badRequest().body("ID is required");
        }
        if (request.statement == null || request.statement.isEmpty()) {
            return ResponseEntity.badRequest().body("Statement is required");
        }
        try {
            float[] vector = embeddingService.embed(request.statement);
            VectorStore shard = shardManager.getShard(request.id);
            shard.addVector(request.id, vector);
            return ResponseEntity.ok("Vector added/updated from statement");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVector(@PathVariable String id) {
        VectorStore shard = shardManager.getShard(id);
        float[] vector = shard.getVector(id);
        if (vector == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vector not found");
        }
        List<Float> vectorList = new ArrayList<>();
        for (float v : vector) {
            vectorList.add(v);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("vector", vectorList);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVector(@PathVariable String id) {
        VectorStore shard = shardManager.getShard(id);
        boolean removed = shard.deleteVector(id);
        if (removed) {
            return ResponseEntity.ok("Vector deleted");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vector not found");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchVectors(
            @RequestParam int k,
            @RequestParam String statement) {

        if (statement == null || statement.isEmpty()) {
            return ResponseEntity.badRequest().body("Statement is required");
        }
        if (k <= 0) {
            return ResponseEntity.badRequest().body("k must be positive");
        }

        float[] queryVector;
        try {
            queryVector = embeddingService.embed(statement);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        // Aggregate results from all shards
        Map<String, Float> allResults = new HashMap<>();
        for (VectorStore shard : shardManager.getAllShards()) {
            List<String> shardResults = shard.search(queryVector, k);
            for (String id : shardResults) {
                float[] v = shard.getVector(id);
                float sim = cosineSimilarity(queryVector, v);
                allResults.put(id, sim);
            }
        }

        // Sort global top-k
        List<Map.Entry<String, Float>> sorted = new ArrayList<>(allResults.entrySet());
        sorted.sort((e1, e2) -> Float.compare(e2.getValue(), e1.getValue()));

        List<String> topK = new ArrayList<>();
        for (int i = 0; i < Math.min(k, sorted.size()); i++) {
            topK.add(sorted.get(i).getKey());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("topK", topK);
        return ResponseEntity.ok(response);
    }

    private float cosineSimilarity(float[] v1, float[] v2) {
        float dot = 0f, normV1 = 0f, normV2 = 0f;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            normV1 += v1[i] * v1[i];
            normV2 += v2[i] * v2[i];
        }
        if (normV1 == 0 || normV2 == 0) {
            return 0f;
        }
        return dot / ((float) (Math.sqrt(normV1) * Math.sqrt(normV2)));
    }
}
