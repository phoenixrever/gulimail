package com.phoenixhell.gulimall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.fastjson.JSON;
import com.phoenixhell.common.exception.BizCodeEnume;
import com.phoenixhell.common.utils.R;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class SentinelGateWayConfig {
    public SentinelGateWayConfig() {
        GatewayCallbackManager.setBlockHandler(new BlockRequestHandler(){
            //网关限流了请求就会调用此回调  Mono flux spring5 新特性 响应式编程
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
                R r = R.error(BizCodeEnume.TO_MANY_REQUEST.getCode(), BizCodeEnume.TO_MANY_REQUEST.getMsg());
                String error = JSON.toJSONString(r);
                Mono<ServerResponse> body = ServerResponse.ok().body(Mono.just(error), String.class);
                return body;
            }
        });
    }
}
