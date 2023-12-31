/*
 * MIT License
 *
 * Copyright (c) 2023 Henry HE (henryhe.cn)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cn.henryhe.io;

import cn.henryhe.utils.ClassPathUtils;
import cn.henryhe.utils.YamlUtils;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.util.*;

public class PropertyResolverTest {
    @Tested
    PropertyResolver propertyResolver;

    @Before
    public void setUp() {
        Properties properties = new Properties();
        propertyResolver = new PropertyResolver(properties);
    }

    @Test
    public void testContainsProperty() throws Exception {
        boolean result = propertyResolver.containsProperty("name");
        Assert.assertEquals(false, result);
    }

    @Test
    public void testGetPropertyByFile() throws Exception {
        Properties properties  = ClassPathUtils.readPropertiesByName("config.properties");
        propertyResolver = new PropertyResolver(properties);
        Assert.assertEquals("henryhe", propertyResolver.getProperty("name"));
        Assert.assertEquals("1.0.0", propertyResolver.getProperty("${app.version}"));
    }

    @Test
    public void testGetPropertiesToString() throws Exception {
        String properties  = ClassPathUtils.readString("config.properties");
        Assert.assertTrue(properties.contains("name=henryhe"));
    }

    @Test
    public void testGetPropertyInteger() throws Exception {
        Properties properties  = ClassPathUtils.readPropertiesByName("config.properties");
        propertyResolver = new PropertyResolver(properties);
        Assert.assertTrue(Integer.class.isInstance(propertyResolver.getProperty("timeout", Integer.class)));
        Assert.assertEquals(Optional.of(1722222222).get(), propertyResolver.getProperty("timeout", Integer.class));
    }

    @Test
    public void testGetPropertyFromYaml() throws Exception {
        Map<String, Object> dataMap = YamlUtils.loadYamlAsPlainMap("application.yaml");
        Properties properties = new Properties();
        properties.putAll(dataMap);
        propertyResolver = new PropertyResolver(properties);
        Assert.assertEquals("1.0.0", propertyResolver.getProperty("${app.version}"));
        Assert.assertTrue(propertyResolver.getProperty("app.position").contains("home"));
    }
}