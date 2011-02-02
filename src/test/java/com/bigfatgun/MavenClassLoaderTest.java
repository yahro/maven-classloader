package com.bigfatgun;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;

public class MavenClassLoaderTest {

    @Test
    public void jodaTime() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String gav = "joda-time:joda-time:1.6.2";
        String className = "org.joda.time.chrono.BuddhistChronology";
        ClassLoader loader = MavenClassLoader.forGAV(gav);
        assertNotNull(loader);
        Class<?> buddhistChronology = loader.loadClass(className);
        assertNotNull(buddhistChronology);
        Method factoryMethod = buddhistChronology.getMethod("getInstance");
        assertNotNull(factoryMethod.invoke(null));
    }
}
