package com.bigfatgun;

import com.google.common.collect.Multiset;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;

public class MavenClassLoaderTest {

    @Test
    public void jodaTime() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String gav = "joda-time:joda-time:[1.6,)";
        String className = "org.joda.time.chrono.BuddhistChronology";
        ClassLoader loader = MavenClassLoader.forGAV(gav);
        assertNotNull(loader);
        Class<?> buddhistChronology = loader.loadClass(className);
        assertNotNull(buddhistChronology);
        Method factoryMethod = buddhistChronology.getMethod("getInstance");
        assertNotNull(factoryMethod.invoke(null));
    }

    @Test(expected = ClassNotFoundException.class)
    public void jodaTimeClassLoaderDoesNotHaveMultiset() throws ClassNotFoundException {
        String gav = "joda-time:joda-time:[1.6,)";
        ClassLoader loader = MavenClassLoader.forGAV(gav);
        assertNotNull(loader);
        loader.loadClass(Multiset.class.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void classLoaderConstructionFailsOnBogusGAV() {
        MavenClassLoader.forGAV("this isn't going to work!");
    }
}
