# VectorShard - A Simple Vector Database

VectorShard is a lightweight vector database built in Java using Spring Boot and Maven. It supports storing, retrieving, deleting, and searching vectors derived from text statements. The vectors are generated using a Discrete Fourier Transform (DFT)-based embedding of input text, providing a simple yet effective way to convert text into fixed-length numeric vectors.

---

## Features

- **Text to Vector Embedding:** Converts input text statements into fixed-length vectors using a DFT-based embedding.
- **Sharded Storage:** Vectors are distributed across multiple shards for scalability.
- **Thread-safe Operations:** Concurrent access to vector storage with proper locking.
- **Similarity Search:** Find top-k most similar vectors to a query statement using cosine similarity.
- **RESTful API:** Easy-to-use HTTP endpoints for vector operations.

---

## Embedding Method

The embedding service converts a text statement into a 10-dimensional vector by computing the Discrete Fourier Transform (DFT) of the character codes of the string. The resulting vector is normalized to unit length. This approach captures frequency-domain features of the text, providing a simple semantic representation.

---

## API Endpoints

### 1. Add or Update Vector

- **URL:** `POST /vectors`
- **Description:** Adds or updates a vector for a given ID by embedding the provided text statement.
- **Request Body:**

```json
{
  "id": "unique_vector_id",
  "statement": "Your text statement here"
}
```

- **Response:**
  - `200 OK` on success with message `"Vector added/updated from statement"`.
  - `400 Bad Request` if input is invalid.


### 2. Get Vector by ID

- **URL:** `GET /vectors/{id}`
- **Description:** Retrieves the stored vector for the given ID.
- **Response:**

```json
{
  "id": "unique_vector_id",
  "vector": [0.123, 0.456, ..., 0.789]
}
```

- **Status Codes:**
  - `200 OK` if vector found.
  - `404 Not Found` if vector does not exist.

---

### 3. Delete Vector by ID

- **URL:** `DELETE /vectors/{id}`
- **Description:** Deletes the vector associated with the given ID.
- **Response:**
  - `200 OK` with message `"Vector deleted"` if successful.
  - `404 Not Found` if vector does not exist.

---

### 4. Search Top-k Similar Vectors

- **URL:** `GET /vectors/search`
- **Query Parameters:**
  - `k` (integer, required): Number of top similar vectors to return.
  - `statement` (string, required): Text statement to embed and search against stored vectors.
- **Response:**

```json
{
  "topK": ["id1", "id2", "id3"]
}
```

- **Status Codes:**
  - `200 OK` with list of top-k vector IDs sorted by similarity.
  - `400 Bad Request` if parameters are missing or invalid.

---

## Example Usage

### Add a Vector

```bash
curl -X POST http://localhost:8080/vectors \
  -H "Content-Type: application/json" \
  -d '{"id": "vec1", "statement": "Hello world"}'
```

### Get a Vector

```bash
curl http://localhost:8080/vectors/vec1
```

### Delete a Vector

```bash
curl -X DELETE http://localhost:8080/vectors/vec1
```

### Search Similar Vectors

```bash
curl "http://localhost:8080/vectors/search?k=3&statement=Hello"
```

---

## Project Structure

- `embedding/DFTEmbeddingService.java` — Implements the DFT-based text embedding.
- `store/VectorStore.java` — Thread-safe vector storage and similarity search per shard.
- `store/VectorShardManager.java` — Manages multiple shards and routes operations.
- `api/VectorController.java` — Spring REST controller exposing the API endpoints.
- `VectorDbApplication.java` — Spring Boot application entry point.

---

## Requirements

- Java 11 or higher
- Maven
- Spring Boot

---

## How to Run

1. Clone the repository.
2. Build the project with Maven:

```bash
mvn clean install
```

3. Run the Spring Boot application:

```bash
mvn spring-boot:run
```

4. The API will be available at `http://localhost:8080`.

---

## Future Improvements

- Replace DFT embedding with advanced pretrained models (e.g., Sentence-BERT).
- Add persistent storage for durability.
- Implement approximate nearest neighbor (ANN) indexing for faster search.
- Support batch operations and bulk imports.
- Add authentication and rate limiting.

```

---
