package it.uniroma3.fragment.test;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;

/**
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class FixtureUtils {
    
    static public URL getResource(String name) {
        final URL resource = FixtureUtils.class.getResource(name);
        assertNotNull("Cannot find a resource of logical name: "+name, resource);
        return resource;
    }
    
    static public boolean isExistingResource(String resourceName) {
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = FixtureUtils.class.getResourceAsStream(resourceName);
            if (resourceAsStream==null)
                return false;
            resourceAsStream.close();
            return true;
        } catch (Exception e) {
            return false;
        }
        finally {
            if (resourceAsStream!=null) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    /* just ignore it... */
                }
            }
        }
    }
    
    static public InputStream getResourceInputStream(String resourceName) {
        final InputStream resourceAsStream = FixtureUtils.class.getResourceAsStream(resourceName);
        assertNotNull("Cannot resolve resource of logical name "+resourceName+" loading by means of "+FixtureUtils.class, resourceAsStream);
        return resourceAsStream;
    }

    static public BufferedReader getResourceReader(String resourceName) {
        return new BufferedReader(new InputStreamReader(getResourceInputStream(resourceName)));
    }

    static public File makeTmpDirectory() throws IOException {
        final File tmp = Files.createTempDirectory(FixtureUtils.class.getSimpleName()).toFile();
        tmp.deleteOnExit();
        return tmp;
    }
    
    static public File makeTmpFileWithContent(String string) throws IOException {
        final File tmp = File.createTempFile("test", ".html");
        tmp.deleteOnExit();
        final PrintWriter writer = new PrintWriter(tmp);
        writer.print(string);
        writer.close();
        return tmp;
    }
    
}
