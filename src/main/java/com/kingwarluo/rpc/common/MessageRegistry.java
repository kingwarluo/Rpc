package com.kingwarluo.rpc.common;

import java.util.HashMap;
import java.util.Map;


public class MessageRegistry {

    private String type;
    private Map<String, Class<?>> registrys = new HashMap<>();

    public void register(String type, Class<?> clazz){
        System.out.println("register:" + type + "," + clazz);
        registrys.put(type, clazz);
    }

    public Class<?> get(String type){
        return registrys.get(type);
    }

}
