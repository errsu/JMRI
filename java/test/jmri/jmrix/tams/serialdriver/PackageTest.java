package jmri.jmrix.tams.serialdriver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   SerialDriverAdapterTest.class,
   jmri.jmrix.tams.serialdriver.configurexml.PackageTest.class

})
/**
 * Tests for the jmri.jmrix.tams.serialdriver package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
