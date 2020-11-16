package com.github.sczero.java.rpc.service;

import com.github.sczero.java.rpc.exception.RpcException;

public class HelloServiceImpl implements HelloService {
    @Override
    public String say(String sth, int times) {
        StringBuilder sb = new StringBuilder("Hello ");
        sb.append(sth).append(" ").append("当前时间:").append(System.currentTimeMillis()).append(" times: ").append(times);
        throw new RpcException("测试错误");
//        return sb.toString();
    }
}
