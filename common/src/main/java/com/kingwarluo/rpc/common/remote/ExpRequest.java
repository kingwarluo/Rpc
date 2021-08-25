package com.kingwarluo.rpc.common.remote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpRequest {

    private int base;
    private int exp;

}
