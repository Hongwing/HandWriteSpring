package cn.henryhe.io;

import mockit.Deencapsulation;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class ResourceResolverTest {
    @Tested
    ResourceResolver resourceResolver;

    @Before
    public void setUp() {
        resourceResolver = new ResourceResolver("cn.henryhe.io");
    }

    /**
     * JMockit已经去除了通过Deencapsulation.invoke调用方式
     *
     * JMockit's Release notes page mentions the retirement of support for mocking/faking private methods (do a text search for "private"). That was made because mocking privates is commonly regarded as a bad practice, and there is no known legitimate use case for doing it, to the best of my knowledge.
     *
     * As for testing private methods, the commonly accepted way to do it is by testing the public methods that call them. Generally, a proper test should never target implementation details.
     *
     * The Tutorial already provides some guidance, but the problem is, most users won't ever read it, or won't pay much attention to it.
     *
     * @throws Exception
     */
    @Test
    public void testStringSlash_removeTrailingSlash() throws Exception {
        String basePackage = "cn/henryhe/io/";
        Assert.assertEquals("cn/henryhe/io", Deencapsulation.invoke(resourceResolver, "removeTrailingSlash", basePackage));
    }

    @Test
    public void testStringSlash_removeLeadingSlash() throws Exception {
        String basePackage = "/cn/henryhe/io";
        Assert.assertEquals("cn/henryhe/io", Deencapsulation.invoke(resourceResolver, "removeLeadingSlash", basePackage));
    }

    @Test
    public void testScan() throws Exception {
        String pkg = "cn.henryhe.io";
        ResourceResolver rr = new ResourceResolver(pkg);
        List<String> classes = rr.scan(res -> {
            String name = res.getName();
            if (name.endsWith(".class")) {
                // 重新洗刷成类名
                return name.substring(0, name.length() - 6).replace("/", ".").replace("\\", ".");
            }
            return null;
        });
        Collections.sort(classes);
        System.out.println(classes);
        String[] listClasses = new String[] {
                // list of some scan classes:
                "cn.henryhe.io.Resource"
        };
        for (String clazz : listClasses) {
            Assert.assertTrue(classes.contains(clazz));
        }
    }
}
