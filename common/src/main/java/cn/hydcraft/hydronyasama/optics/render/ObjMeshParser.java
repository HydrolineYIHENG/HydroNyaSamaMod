package cn.hydcraft.hydronyasama.optics.render;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Minimal Wavefront OBJ parser for optics static meshes. */
public final class ObjMeshParser {

  private ObjMeshParser() {}

  public static ObjMeshData parse(InputStream inputStream) throws IOException {
    List<ObjMeshData.Vec3> vertices = new ArrayList<>();
    List<ObjMeshData.Vec2> texCoords = new ArrayList<>();
    List<ObjMeshData.Vec3> normals = new ArrayList<>();
    List<ObjMeshData.Triangle> triangles = new ArrayList<>();

    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }
        if (line.startsWith("v ")) {
          String[] parts = split(line, 4);
          vertices.add(
              new ObjMeshData.Vec3(
                  Float.parseFloat(parts[1]),
                  Float.parseFloat(parts[2]),
                  Float.parseFloat(parts[3])));
        } else if (line.startsWith("vt ")) {
          String[] parts = split(line, 3);
          texCoords.add(
              new ObjMeshData.Vec2(Float.parseFloat(parts[1]), Float.parseFloat(parts[2])));
        } else if (line.startsWith("vn ")) {
          String[] parts = split(line, 4);
          normals.add(
              new ObjMeshData.Vec3(
                  Float.parseFloat(parts[1]),
                  Float.parseFloat(parts[2]),
                  Float.parseFloat(parts[3])));
        } else if (line.startsWith("f ")) {
          parseFace(line, triangles);
        }
      }
    }

    return new ObjMeshData(vertices, texCoords, normals, triangles);
  }

  private static void parseFace(String line, List<ObjMeshData.Triangle> triangles) {
    String[] parts = line.split("\\s+");
    if (parts.length < 4) {
      return;
    }

    ObjMeshData.VertexRef first = parseVertexRef(parts[1]);
    ObjMeshData.VertexRef prev = parseVertexRef(parts[2]);
    for (int i = 3; i < parts.length; i++) {
      ObjMeshData.VertexRef next = parseVertexRef(parts[i]);
      triangles.add(new ObjMeshData.Triangle(first, prev, next));
      prev = next;
    }
  }

  private static ObjMeshData.VertexRef parseVertexRef(String token) {
    String[] indices = token.split("/");
    int vertex = parseIndex(indices, 0);
    int texCoord = parseIndex(indices, 1);
    int normal = parseIndex(indices, 2);
    return new ObjMeshData.VertexRef(vertex, texCoord, normal);
  }

  private static int parseIndex(String[] indices, int offset) {
    if (offset >= indices.length || indices[offset].isEmpty()) {
      return -1;
    }
    return Integer.parseInt(indices[offset]) - 1;
  }

  private static String[] split(String line, int minParts) {
    String[] parts = line.split("\\s+");
    if (parts.length < minParts) {
      throw new IllegalArgumentException("Invalid OBJ line: " + line);
    }
    return parts;
  }
}
