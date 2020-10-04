package de.czymm.serversigns.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static de.czymm.serversigns.utils.Version.*;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
public class VersionTest {
    private static class VersionData {
        final String v1;
        final Version v2;
        final boolean[] expectResults;

        VersionData(final String v1, final Version v2, boolean... expectedResults) {
            this.v1 = v1;
            this.v2 = v2;
            this.expectResults = expectedResults;
        }

        String getMessage(final String method, final int index) {
            return String.format("%s %s %s => %b", this.v1, method, this.v2, this.expectResults[index]);
        }
    }

    private List<VersionData> testDatas;

    @Before
    public void setUp() {
        testDatas = new ArrayList<>();
        testDatas.add(new VersionData("1.7", V1_7  , false, true, true, true, false));
        testDatas.add(new VersionData("1.7", V1_8  , false, false, false, true, true));
        testDatas.add(new VersionData("1.7", V1_12 , false, false, false, true, true));
        testDatas.add(new VersionData("1.9", V1_7  , true, true, false, false, false));
        testDatas.add(new VersionData("1.12", V1_7 , true, true, false, false, false));
        testDatas.add(new VersionData("1.14", V1_14, false, true, true, true, false));
    }

    @Test
    public void testVersion_initVersion() {
        assertTrue(isLowerThan(V1_7));

        initVersion("1.12");

        assertTrue(isEqualsTo(V1_12));
    }

    @Test
    public void testVersion_is_higher_than() {
        for (final VersionData data : testDatas) {
            Version.initVersion(data.v1);

            System.out.println(data.getMessage("isHigherThan", 0));
            assertEquals(data.expectResults[0], isHigherThan(data.v2));
        }
    }

    @Test
    public void testVersion_is_higher_or_equals_to() {
        for (final VersionData data : testDatas) {
            Version.initVersion(data.v1);

            System.out.println(data.getMessage("isHigherOrEqualsTo", 1));
            assertEquals(data.expectResults[1], isHigherOrEqualsTo(data.v2));
        }
    }

    @Test
    public void testVersion_is_equals_to() {
        for (final VersionData data : testDatas) {
            Version.initVersion(data.v1);

            System.out.println(data.getMessage("isEqualsTo", 2));
            assertEquals(data.expectResults[2], isEqualsTo(data.v2));
        }
    }

    @Test
    public void testVersion_is_lower_or_equals_to() {
        for (final VersionData data : testDatas) {
            Version.initVersion(data.v1);

            System.out.println(data.getMessage("isLowerOrEqualsTo", 3));
            assertEquals(data.expectResults[3], isLowerOrEqualsTo(data.v2));
        }
    }

    @Test
    public void testVersion_is_lower_than() {
        for (final VersionData data : testDatas) {
            Version.initVersion(data.v1);

            System.out.println(data.getMessage("isLowerThan", 4));
            assertEquals(data.expectResults[4], isLowerThan(data.v2));
        }
    }
}
