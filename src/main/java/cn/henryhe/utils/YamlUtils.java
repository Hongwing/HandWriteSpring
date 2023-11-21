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

import org.yaml.snakeyaml.Yaml;

import java.util.*;

/**
 * 提供yaml配置读取
 */
public class YamlUtils {

    public static Map<String, Object> loadYamlAsPlainMap(String name) {
        Yaml yaml = new Yaml();
        HashMap<String, Object> data = yaml.loadAs(ClassPathUtils.readString(name), HashMap.class);
        HashMap<String, Object> plainMap = new LinkedHashMap<>();
        convertToPlainMap(data, "", plainMap);
        return plainMap;
    }

    /**
     * 解析的Map对象 扁平化为 app.version类型的key
     * @param data
     * @param prefix
     * @param plainMap
     */
    private static void convertToPlainMap(HashMap<String, Object> data, String prefix, HashMap<String, Object> plainMap) {
        for (String key : data.keySet()) {
            Object valueObject = data.get(key);
            if (HashMap.class.isInstance(valueObject)) {
                convertToPlainMap((HashMap<String, Object>) valueObject, key + ".", plainMap);
            } else if (valueObject instanceof List){
                plainMap.put(prefix + key, valueObject);
            } else {
                plainMap.put(prefix + key, valueObject.toString());
            }
        }
    }
}
