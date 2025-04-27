package com.vectorshard.vector.database.store;

import java.util.ArrayList;
import java.util.List;

public class VectorShardManager {
    private final List<VectorStore> shards;
    private final int shardCount;

    public VectorShardManager(int shardCount) {
        if (shardCount <= 0) {
            throw new IllegalArgumentException("Shard count must be positive");
        }
        this.shardCount = shardCount;
        this.shards = new ArrayList<>(shardCount);
        for (int i = 0; i < shardCount; i++) {
            shards.add(new VectorStore());
        }
    }

    private int getShardIndex(String id) {
        return Math.abs(id.hashCode()) % shardCount;
    }

    public VectorStore getShard(String id) {
        int index = getShardIndex(id);
        return shards.get(index);
    }

    public List<VectorStore> getAllShards() {
        return shards;
    }
}
