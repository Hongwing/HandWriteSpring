package cn.henryhe.io;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;


/**
 * 根据包路径扫描包含的类
 */
public class ResourceResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceResolver.class);

    private String basePackage;


    public ResourceResolver(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * 扫描路径下所有的类
     *
     * @param mapper，函数映射
     * @return
     */
    public <R> List<R> scan(Function<Resource, R> mapper) {
        // 类路径转为文件路径，准备扫描
        String basePackagePath = this.basePackage.replace(".", "/");
        String path = basePackagePath;
        try {
            List<R> collector = new ArrayList<>();
            scan0(basePackagePath, path, collector, mapper);
            return collector;
        } catch (Exception exception) {
            LOGGER.error("");
        }


        return Arrays.asList(mapper.apply(new Resource("", "")));
    }

    private <R> void scan0(String basePackagePath, String path, List<R> collector, Function<Resource,R> mapper) throws IOException, URISyntaxException {
        LOGGER.warn("scan basePackagePath at " + basePackagePath);
        Enumeration<URL> theUrlPaths = getContextClassLoader().getResources(path);
        while (theUrlPaths.hasMoreElements()) {
            URL url = theUrlPaths.nextElement();
            URI uri = url.toURI();
            String uriStr = removeTrailingSlash(uri.toString());
            String uriBaseStr = uriStr.substring(0, uriStr.length() - basePackagePath.length());
            // 根据前缀确认类型
            if (uriBaseStr.startsWith("file:")) {
                uriBaseStr = uriBaseStr.substring(5);
            }
            if (uriStr.startsWith("jar:")) {
                // jar包
                scanFile(true, uriBaseStr, jarUriToPath(basePackagePath, uri), collector, mapper);
            } else {
                // 单独class文件
                scanFile(false, uriBaseStr, Paths.get(uri), collector, mapper);
            }
        }
    }

    private <R> void scanFile(boolean isJar, String uriBaseStr, Path root, List<R> collector, Function<Resource,R> mapper) throws IOException {
        String baseDir = removeTrailingSlash(uriBaseStr);
        // 遍历
        Files.walk(root).filter(Files::isRegularFile).forEach(file -> {
            Resource res = null;
            if (isJar) {
                res = new Resource(baseDir, removeLeadingSlash(file.toString()));
            } else {
                String path = file.toString();
                String name = removeLeadingSlash(path.substring(baseDir.length()));
                res = new Resource("file:" + path, name);
            }
            LOGGER.warn("found resources " + res.toString());
            // 真正需要确认是否是class 或者是 properties被放置在Function中.Nice
            R r = mapper.apply(res);
            if (r != null) {
                collector.add(r);
            }
        });
    }

    private Path jarUriToPath(String basePackagePath, URI uri) throws IOException {
        return FileSystems.newFileSystem(uri, new HashMap<>()).getPath(basePackagePath);
    }

    private String removeTrailingSlash(String string) {
        if (string.endsWith("/") || string.endsWith("\\")) {
            string = string.substring(0, string.length() - 1);
        }
        return string;
    }

    private String removeLeadingSlash(String string) {
        if (string.startsWith("/") || string.startsWith("\\")) {
            string = string.substring(1);
        }
        return string;
    }


    private ClassLoader getContextClassLoader() {
        ClassLoader cl = null;
        cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = getClass().getClassLoader();
        }
        return cl;
    }
}
