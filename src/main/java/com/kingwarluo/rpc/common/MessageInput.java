package com.kingwarluo.rpc.common;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageInput {

    private String requestId;
    private String type;
    private String payload;

    public <T> T getPayload(Class<T> clazz){
        if(payload == null){
            return null;
        }
        return JSON.parseObject(payload, clazz);
    }

}
