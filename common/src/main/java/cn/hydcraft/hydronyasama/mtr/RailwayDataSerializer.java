package cn.hydcraft.hydronyasama.mtr;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import mtr.data.Rail;
import mtr.data.RailwayData;
import mtr.data.SerializedDataBase;
import mtr.data.SignalBlocks;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializes the runtime {@link RailwayData} contents into MessagePack so that an external consumer
 * can consume the same structure the mod uses internally.
 */
public final class RailwayDataSerializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(RailwayDataSerializer.class);
  private static final Field RAILS_FIELD = locateRailsField();
  private static final Field SIGNAL_BLOCKS_FIELD = locateSignalBlocksField();
  private static final int DATASET_COUNT = 8;

  private RailwayDataSerializer() {}

  public static byte[] serialize(MtrDimensionSnapshot snapshot) {
    if (snapshot == null) {
      return new byte[0];
    }
    RailwayData data = snapshot.getRailwayData();
    if (data == null) {
      return new byte[0];
    }
    try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
      packer.packMapHeader(DATASET_COUNT);
      RailwayData.writeMessagePackDataset(packer, safe(data.stations), "stations");
      RailwayData.writeMessagePackDataset(packer, safe(data.platforms), "platforms");
      RailwayData.writeMessagePackDataset(packer, safe(data.routes), "routes");
      RailwayData.writeMessagePackDataset(packer, safe(data.depots), "depots");
      RailwayData.writeMessagePackDataset(packer, safe(data.sidings), "sidings");
      RailwayData.writeMessagePackDataset(packer, safe(data.lifts), "lifts");
      RailwayData.writeMessagePackDataset(packer, safe(signalBlocks(data)), "signalBlocks");
      RailwayData.writeMessagePackDataset(packer, safe(flattenRails(data)), "rails");
      packer.flush();
      return packer.toByteArray();
    } catch (IOException ex) {
      LOGGER.warn(
          "Unable to serialize RailwayData for {}: {}", snapshot.getDimensionId(), ex.getMessage());
      return new byte[0];
    }
  }

  private static <T extends SerializedDataBase> Collection<T> safe(Collection<T> source) {
    return source == null ? Collections.emptyList() : source;
  }

  private static Collection<Rail> flattenRails(RailwayData data) {
    Map<Object, Map<Object, Rail>> rails = readRails(data);
    if (rails.isEmpty()) {
      return Collections.emptyList();
    }
    Set<Rail> flattened = Collections.newSetFromMap(new IdentityHashMap<>());
    rails
        .values()
        .forEach(
            entry -> {
              if (entry != null) {
                for (Rail rail : entry.values()) {
                  if (rail != null) {
                    flattened.add(rail);
                  }
                }
              }
            });
    return flattened;
  }

  @SuppressWarnings("unchecked")
  private static Collection<SerializedDataBase> signalBlocks(RailwayData data) {
    SignalBlocks blocks = readSignalBlocks(data);
    if (blocks == null || blocks.signalBlocks == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableCollection(
        (Collection<? extends SerializedDataBase>) blocks.signalBlocks);
  }

  @SuppressWarnings("unchecked")
  private static Map<Object, Map<Object, Rail>> readRails(RailwayData data) {
    if (data == null || RAILS_FIELD == null) {
      return Collections.emptyMap();
    }
    try {
      Object value = RAILS_FIELD.get(data);
      if (value instanceof Map) {
        return (Map<Object, Map<Object, Rail>>) value;
      }
    } catch (IllegalAccessException ex) {
      LOGGER.debug("Unable to read RailwayData.rails", ex);
    }
    return Collections.emptyMap();
  }

  private static Field locateRailsField() {
    try {
      Field field = RailwayData.class.getDeclaredField("rails");
      field.setAccessible(true);
      return field;
    } catch (NoSuchFieldException ex) {
      LOGGER.warn("Failed to locate RailwayData.rails field", ex);
      return null;
    }
  }

  private static Field locateSignalBlocksField() {
    try {
      Field field = RailwayData.class.getDeclaredField("signalBlocks");
      field.setAccessible(true);
      return field;
    } catch (NoSuchFieldException ex) {
      LOGGER.warn("Failed to locate RailwayData.signalBlocks field", ex);
      return null;
    }
  }

  private static SignalBlocks readSignalBlocks(RailwayData data) {
    if (data == null || SIGNAL_BLOCKS_FIELD == null) {
      return null;
    }
    try {
      Object value = SIGNAL_BLOCKS_FIELD.get(data);
      if (value instanceof SignalBlocks) {
        return (SignalBlocks) value;
      }
    } catch (IllegalAccessException ex) {
      LOGGER.debug("Unable to read RailwayData.signalBlocks", ex);
    }
    return null;
  }
}
