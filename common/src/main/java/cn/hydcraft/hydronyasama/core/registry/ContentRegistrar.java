package cn.hydcraft.hydronyasama.core.registry;

import java.util.Objects;

public interface ContentRegistrar {

    Object registerBlock(BlockDefinition definition);

    Object registerItem(ItemDefinition definition);

    final class BlockDefinition {

        public final ContentId id;
        public final String contentGroup;
        public final String kind;
        public final String material;
        public final String texture;
        public final int lightLevel;
        public final ContentId baseBlockId;

        public BlockDefinition(
                ContentId id,
                String contentGroup,
                String kind,
                String material,
                String texture,
                int lightLevel,
                ContentId baseBlockId
        ) {
            this.id = Objects.requireNonNull(id, "id");
            this.contentGroup = requireNonEmpty(contentGroup, "contentGroup");
            this.kind = requireNonEmpty(kind, "kind");
            this.material = material;
            this.texture = texture;
            this.lightLevel = lightLevel;
            this.baseBlockId = baseBlockId;
        }
    }

    final class ItemDefinition {

        public final ContentId id;
        public final String contentGroup;
        public final String kind;
        public final ContentId blockId;

        public ItemDefinition(ContentId id, String contentGroup, String kind, ContentId blockId) {
            this.id = Objects.requireNonNull(id, "id");
            this.contentGroup = requireNonEmpty(contentGroup, "contentGroup");
            this.kind = requireNonEmpty(kind, "kind");
            this.blockId = blockId;
        }
    }

    static String requireNonEmpty(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " is empty");
        }
        return value;
    }
}

