package de.czymm.serversigns.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Version {
    CURRENT(0, 0),
    V1_7(1, 7),
    V1_8(1, 8),
    V1_9(1, 9),
    V1_10(1, 10),
    V1_11(1, 11),
    V1_12(1, 12),
    V1_13(1, 13),
    V1_14(1, 14),
    V1_15(1, 15),
    V1_16(1, 16),
    V1_17(1, 17),
    V1_18(1, 18),
    V1_19(1, 19),
    V1_20(1, 20);

    private int major;
    private int minor;

    private static final Pattern versionRegex = Pattern.compile("(\\d+)[.](\\d+)");

    Version(final int major, final int minor) {
        this.major = major;
        this.minor = minor;
    }

    private void updateVersion(final String strVersion) {
        final Matcher matcher = versionRegex.matcher(strVersion);

        if (matcher.find() && matcher.groupCount() >= 2) {
            major = Integer.parseInt(matcher.group(1));
            minor = Integer.parseInt(matcher.group(2));
        }
    }

    public static void initVersion(final String strVersion) {
        Version.CURRENT.updateVersion(strVersion);
    }

    public static boolean isLowerThan(final Version newVersion) {
        return (CURRENT.minor < newVersion.minor && CURRENT.major == newVersion.major) || CURRENT.major < newVersion.major;
    }

    public static boolean isLowerOrEqualsTo(final Version newVersion) {
        return (CURRENT.minor == newVersion.minor && CURRENT.major == newVersion.major) ||
            ((CURRENT.minor < newVersion.minor && CURRENT.major == newVersion.major) || CURRENT.major < newVersion.major);
    }

    public static boolean isEqualsTo(final Version newVersion) {
        return CURRENT.minor == newVersion.minor && CURRENT.major == newVersion.major;
    }

    public static boolean isHigherOrEqualsTo(final Version newVersion) {
        return (CURRENT.minor == newVersion.minor && CURRENT.major == newVersion.major) ||
            ((CURRENT.minor > newVersion.minor && CURRENT.major == newVersion.major) || CURRENT.major > newVersion.major);
    }

    public static boolean isHigherThan(final Version newVersion) {
        return  (CURRENT.minor > newVersion.minor && CURRENT.major == newVersion.major) || CURRENT.major > newVersion.major;
    }
}
