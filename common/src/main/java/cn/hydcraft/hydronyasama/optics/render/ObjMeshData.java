package cn.hydcraft.hydronyasama.optics.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ObjMeshData {

    public final List<Vec3> vertices;
    public final List<Vec2> texCoords;
    public final List<Vec3> normals;
    public final List<Triangle> triangles;

    public ObjMeshData(
            List<Vec3> vertices,
            List<Vec2> texCoords,
            List<Vec3> normals,
            List<Triangle> triangles
    ) {
        this.vertices = Collections.unmodifiableList(new ArrayList<>(vertices));
        this.texCoords = Collections.unmodifiableList(new ArrayList<>(texCoords));
        this.normals = Collections.unmodifiableList(new ArrayList<>(normals));
        this.triangles = Collections.unmodifiableList(new ArrayList<>(triangles));
    }

    public static final class Vec3 {
        public final float x;
        public final float y;
        public final float z;

        public Vec3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static final class Vec2 {
        public final float u;
        public final float v;

        public Vec2(float u, float v) {
            this.u = u;
            this.v = v;
        }
    }

    public static final class VertexRef {
        public final int vertexIndex;
        public final int texCoordIndex;
        public final int normalIndex;

        public VertexRef(int vertexIndex, int texCoordIndex, int normalIndex) {
            this.vertexIndex = vertexIndex;
            this.texCoordIndex = texCoordIndex;
            this.normalIndex = normalIndex;
        }
    }

    public static final class Triangle {
        public final VertexRef a;
        public final VertexRef b;
        public final VertexRef c;

        public Triangle(VertexRef a, VertexRef b, VertexRef c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }
}
