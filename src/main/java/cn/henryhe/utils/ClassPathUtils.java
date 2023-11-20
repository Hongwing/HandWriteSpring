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

package cn.henryhe.utils;

import cn.henryhe.io.InputStreamCallback;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;


public class ClassPathUtils {

    public static <T> T readInputStream(String filePath, InputStreamCallback<T> inputStreamCallback) {
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        try (InputStream input = getContextClassLoader().getResourceAsStream(filePath)) {
            if (input == null) {
                throw new FileNotFoundException("File not found: " + filePath);
            }
            return inputStreamCallback.doWithInputStream(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 文件读取为字符串
     * @param filePath
     * @return
     */
    public static String readStringByName(String filePath) {
        // 不含有路径符号
        if (!filePath.contains("/") && !filePath.contains("\\")) {
            filePath = getContextClassLoader().getResource(filePath).getPath();
        }
        String finalPath = filePath;
        return readInputStream(filePath, (input) -> {
            byte [] data = Files.readAllBytes(Paths.get(finalPath));
            return new String(data, StandardCharsets.UTF_8);
        });
    }

    /**
     * 文件读取为字符串
     * @param filePath
     * @return
     */
    public static String readString(String filePath) {
        return readInputStream(filePath, (input) -> {
            byte [] data = readAllBytes(input);
            return new String(data, StandardCharsets.UTF_8);
        });
    }


    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        final int bufLen = 4 * 0x400;
        byte[] buf = new byte[bufLen];
        int readLen;
        IOException exception = null;
        try {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
                    outputStream.write(buf, 0, readLen);            return outputStream.toByteArray();        }
        } catch (IOException e) {
            exception = e;        throw e;    } finally {
            if (exception == null) inputStream.close();        else try {
                inputStream.close();        } catch (IOException e) {
                exception.addSuppressed(e);        }
        }
    }

    public static Properties readPropertiesByName(String name) {
        Properties properties = new Properties();
        try (InputStream inputStream = getContextClassLoader().getResourceAsStream(name)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    private static ClassLoader getContextClassLoader() {
        ClassLoader cl = null;
        cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassPathUtils.class.getClassLoader();
        }
        return cl;
    }
}
