package de.czymm.serversigns.utils;

public enum Version {
    CURRENT(""),
    V1_7("1.7"),
    V1_8("1.8"),
    V1_9("1.9"),
    V1_10("1.10"),
    V1_11("1.11"),
    V1_12("1.12"),
    V1_13("1.13"),
    V1_14("1.14"),
    V1_15("1.15");

    private int major;
    private int minor;

    Version(final String strVersion) {
        final String[] splitVersion = strVersion.split("\\.");

        if (splitVersion.length >= 2) {
            major = Integer.parseInt(splitVersion[0]);
            minor = Integer.parseInt(splitVersion[1]);
        } else {
            major = 0;
            minor = 0;
        }
    }

    private void update_version(final String strVersion) {
        final String[] splitVersion = strVersion.split("\\.");

        if (splitVersion.length >= 2) {
            major = Integer.parseInt(splitVersion[0]);
            minor = Integer.parseInt(splitVersion[1]);
        }
    }

    public static void init_version(final String strVersion) {
        Version.CURRENT.update_version(strVersion);
    }

    public static boolean is_lower_than(final Version newVersion) {
        return (CURRENT.minor < newVersion.minor && CURRENT.major == newVersion.major) || CURRENT.major < newVersion.major;
    }

    public static boolean is_lower_or_equals_to(final Version newVersion) {
        return (CURRENT.minor == newVersion.minor && CURRENT.major == newVersion.major) ||
            ((CURRENT.minor < newVersion.minor && CURRENT.major == newVersion.major) || CURRENT.major < newVersion.major);
    }

    public static boolean is_equals_to(final Version newVersion) {
        return CURRENT.minor == newVersion.minor && CURRENT.major == newVersion.major;
    }

    public static boolean is_higher_or_equals_to(final Version newVersion) {
        return (CURRENT.minor == newVersion.minor && CURRENT.major == newVersion.major) ||
            ((CURRENT.minor > newVersion.minor && CURRENT.major == newVersion.major) || CURRENT.major > newVersion.major);
    }

    public static boolean is_higher_than(final Version newVersion) {
        return  (CURRENT.minor > newVersion.minor && CURRENT.major == newVersion.major) || CURRENT.major > newVersion.major;
    }
}
