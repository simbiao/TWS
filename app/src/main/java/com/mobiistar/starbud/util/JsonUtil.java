package com.mobiistar.starbud.util;

import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * Description: Json operation.
 * Date：18-11-13-下午8:37
 * Author: black
 */
public class JsonUtil {

    public static <T> T parseObject(String json, Class<T> cls) {
        return JSON.parseObject(json, cls);
    }

    public static <T> List<T> parseArray(String json, Class<T> cls) {
        return JSON.parseArray(json, cls);
    }

    public static String toJSONString(Object object) {
        return JSON.toJSONString(object);
    }
}
