package cn.hydcraft.hydronyasama.telecom.compat.webservice;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/** Compatibility crypto helper replacing legacy webservice CryptManager utility. */
public final class CryptManager {
  private CryptManager() {}

  public static String md5(String input) {
    return digest("MD5", input);
  }

  public static String sha1(String input) {
    return digest("SHA-1", input);
  }

  public static String sha256(String input) {
    return digest("SHA-256", input);
  }

  public static String base64Encode(String input) {
    byte[] bytes = input == null ? new byte[0] : input.getBytes(StandardCharsets.UTF_8);
    return Base64.getEncoder().encodeToString(bytes);
  }

  public static String base64Decode(String input) {
    if (input == null || input.isEmpty()) {
      return "";
    }
    try {
      return new String(Base64.getDecoder().decode(input), StandardCharsets.UTF_8);
    } catch (RuntimeException ignored) {
      return "";
    }
  }

  public static String randomToken() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  private static String digest(String algorithm, String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance(algorithm);
      byte[] hashed = digest.digest((input == null ? "" : input).getBytes(StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder(hashed.length * 2);
      for (int i = 0; i < hashed.length; i++) {
        String hex = Integer.toHexString(hashed[i] & 0xFF);
        if (hex.length() < 2) {
          builder.append('0');
        }
        builder.append(hex);
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException e) {
      return "";
    }
  }
}
