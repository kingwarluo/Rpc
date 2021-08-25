package com.kingwarluo.rpc.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageOutput {

    private String requestId;
    private String type;
    private Object payload;

}
