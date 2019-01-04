package com.kingwarluo.rpc.common;

import java.util.HashMap;
import java.util.Map;

public class MessageHandlers {

    private Map<String, IMessageHandler> handlers = new HashMap<String, IMessageHandler>();
    private IMessageHandler defaultHandler;

    public void put(String type, IMessageHandler handler){
        handlers.put(type, handler);
    }

    public IMessageHandler get(String type){
        return handlers.get(type);
    }

    public MessageHandlers defaultHandler(IMessageHandler<MessageInput> defaultHandler) {
        this.defaultHandler = defaultHandler;
        return this;
    }

    public IMessageHandler<MessageInput> defaultHandler() {
        return defaultHandler;
    }

}
