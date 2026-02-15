package cn.hydcraft.hydronyasama.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/** Helpers for splitting large payloads into smaller JSON-friendly chunks. */
public final class PayloadChunker {
  private static final int DEFAULT_MAX_CHUNK_CHARACTERS = 5_000_000;

  private PayloadChunker() {}

  public static JsonObject chunkEncodedPayload(
      String encoding, String encodedPayload, int decodedLength) {
    JsonArray chunks = chunkString(encodedPayload, DEFAULT_MAX_CHUNK_CHARACTERS);
    JsonObject wrapper = new JsonObject();
    wrapper.addProperty("encoding", encoding);
    wrapper.addProperty("decodedLength", decodedLength);
    wrapper.addProperty("chunkCount", chunks.size());
    wrapper.addProperty("chunkSize", DEFAULT_MAX_CHUNK_CHARACTERS);
    wrapper.add("chunks", chunks);
    return wrapper;
  }

  private static JsonArray chunkString(String value, int chunkSize) {
    JsonArray chunks = new JsonArray();
    int index = 0;
    for (int offset = 0; offset < value.length(); offset += chunkSize) {
      int end = Math.min(value.length(), offset + chunkSize);
      JsonObject chunk = new JsonObject();
      chunk.addProperty("index", index++);
      chunk.addProperty("data", value.substring(offset, end));
      chunk.addProperty("length", end - offset);
      chunks.add(chunk);
    }
    return chunks;
  }
}
