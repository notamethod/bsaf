package org.jdesktop.application;

import junit.framework.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class ResourceLoaderTest {
    private static Logger log = Logger.getLogger("ResourceLoaderTest");


    @Test
    public void testLoadResource(){

        String path="org/jdesktop/application/resources/icons/cut.png";
        String failingPath="org/jdesktop/application/resources/othericons/cut.png";
        ApplicationContext context = new ApplicationContext();
        ClassLoader clApp = context.getResourceManager().getResourceMap().getClassLoader();
        URL url = context.getResourceManager().getResourceMap().getClassLoader().getResource(path);
        //found, but why ?
        url = context.getResourceManager().getResourceMap().getClassLoader().getResource(failingPath);
        assertTrue("found from resourceManager ",Boolean.valueOf(url!=null));

        //loading from module
        try (InputStream is = this.getClass().getModule().getResourceAsStream(path)){
            assertTrue("found from module ",Boolean.valueOf(is!=null));
        } catch (IOException e) {
            fail();
        }
        try (InputStream is = this.getClass().getModule().getResourceAsStream(failingPath)){
            assertTrue("found from module ",Boolean.valueOf(is!=null));
        } catch (IOException e) {
            fail();
        }
    }
}
