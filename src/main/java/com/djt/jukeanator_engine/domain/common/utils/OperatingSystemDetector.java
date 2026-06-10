package com.djt.jukeanator_engine.domain.common.utils;

public class OperatingSystemDetector {

  public enum OSType {
    WINDOWS, LINUX, MACOS, UNKNOWN
  }

  private static final OSType DETECTED_OS;

  static {
    String osName = System.getProperty("os.name").toLowerCase();

    if (osName.contains("win")) {
      DETECTED_OS = OSType.WINDOWS;
    } else if (osName.contains("mac") || osName.contains("darwin")) {
      DETECTED_OS = OSType.MACOS;
    } else if (osName.contains("nux") || osName.contains("nix") || osName.contains("aix")) {
      DETECTED_OS = OSType.LINUX;
    } else {
      DETECTED_OS = OSType.UNKNOWN;
    }
  }

  public static OSType getOperatingSystem() {
    return DETECTED_OS;
  }

  public static void main(String[] args) {
    System.out.println("Running on: " + getOperatingSystem());
  }
}
