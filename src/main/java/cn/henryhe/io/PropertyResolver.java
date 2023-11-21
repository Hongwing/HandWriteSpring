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

import com.mysql.cj.util.StringUtils;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import jakarta.annotation.Nullable;

import java.lang.annotation.Target;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

/**
 * 配置文件读取
 */
public class PropertyResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyResolver.class);

    private static Map<Class<?>, Function<String, Object>> converters = new HashMap<>();

    static {
        // 初始化转换器
        converters.put(Integer.class, (t) -> Integer.parseInt(t));
        // String类型:
        converters.put(String.class, s -> s);
        // boolean类型:
        converters.put(boolean.class, s -> Boolean.parseBoolean(s));
        converters.put(Boolean.class, s -> Boolean.valueOf(s));
        // int类型:
        converters.put(int.class, s -> Integer.parseInt(s));
        // 其他基本类型...
        // Date/Time类型:
        converters.put(LocalDate.class, s -> LocalDate.parse(s));
        converters.put(LocalTime.class, s -> LocalTime.parse(s));
        converters.put(LocalDateTime.class, s -> LocalDateTime.parse(s));
        converters.put(ZonedDateTime.class, s -> ZonedDateTime.parse(s));
    }

    Map<String, String> properties = new HashMap<>();

    /**
     * 提供注册转换器
     * @param clazz
     * @param function
     */
    public static void registerConverter(Class<?> clazz, Function<String, Object> function) {
        if (clazz != null && function != null) {
            converters.put(clazz, function);
        }
    }

    public PropertyResolver(Properties props) {
        // load env key-value
        this.properties.putAll(System.getenv());
        Set<String> names = props.stringPropertyNames();
        for (String name : names) {
            this.properties.put(name, props.getProperty(name));
        }
    }

    public boolean containsProperty(String name) {
        return this.properties.containsKey(name);
    }

    /**
     * string类型 - 获取value
     * - ${IP:0.0.0.0}:${PORT:3306} - 不支持此种表达 spring使用spring-expression来支持这种复杂解析额。
     * - ${IP}
     * - ${IP.IPV4:0.0.0.0}
     * - IP
     * - IP.IPV4
     * - ${app.title:APP_NAME:xxxx}
     * @param name
     * @return
     */
    @Nullable
    public String getProperty(String name) {
        if (StringUtils.isNullOrEmpty(name)) return null;
        PropertyExpr propertyExpr = parsePropertyExpr(name);
        // 带有$
        if (propertyExpr != null) {
            if (propertyExpr.getDefaultValue() != null) {
                return getproperty(propertyExpr.getKey(), propertyExpr.getDefaultValue());
            } else {
                return getRequiredProperty(propertyExpr.getKey());
            }
        }
        // 不带$
        String value = this.properties.get(name);
        if (value != null) {
            // 递归查询一下
            return parseValue(value);
        }
        return value;
    }

    private String getRequiredProperty(String key) {
        String value = getProperty(key);
        return Objects.requireNonNull(value, "Property '" + key + "' not found");
    }

    private <T> T getRequiredProperty(String key, Class<T> targetType) {
        T value = getProperty(key, targetType);
        return Objects.requireNonNull(value, "Property '" + key + "' not found");
    }

    private String getproperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value == null ? parseValue(defaultValue) : value;
    }

    /**
     * 提供类型转换：除string类型外，其他类型也可被获取
     * @param key
     * @param targetType
     * @return
     * @param <T>
     */
    public <T> T getProperty(String key, Class<T> targetType) {
        String value = getProperty(key);
        if (value == null) return null;

        return convert(targetType, value);
    }

    /**
     * 通过默认转换器提供转换
     * @param clazz
     * @param value
     * @return
     * @param <T>
     */
    private <T> T convert(Class<T> clazz, String value) {
        Function<String, Object> converter = converters.get(clazz);
        if (converter == null) {
            throw new IllformedLocaleException("Unsupported value type" + clazz.getName());
        }
        return (T) converter.apply(value);
    }

    private String parseValue(String value) {
        if (value == null) return null;
        while (value.indexOf(":") != -1) {
            value = value.substring(value.indexOf(":") + 1, value.length());
        }
        return value;
    }

    private PropertyExpr parsePropertyExpr(String name) {
        if (name.startsWith("${") && name.endsWith("}")) {
            int n = name.indexOf(":");
            if (n != -1) {
                // 带有默认值
                String key = name.substring(2, n);
                return new PropertyExpr(key, name.substring(n + 1, name.length() - 1));
            } else {
                // 不带默认值
                String key = name.substring(2, name.length() - 1);
                return new PropertyExpr(key, null);
            }
        }
        return null;
    }

}
