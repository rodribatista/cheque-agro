package com.jmg.checkagro.provider.client;

import com.jmg.checkagro.provider.config.LoadBalancerConfig;
import feign.Headers;
import lombok.*;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.constraints.Size;

@FeignClient(name = "api-provider")
@LoadBalancerClient(value = "api-provider", configuration = LoadBalancerConfig.class)
public interface CheckMSClient {

    @Headers("Content-Type: application/json")
    @PostMapping("/api/v1/check/provider/register")
    void registerProvider(DocumentRequest request);

    @Headers("Content-Type: application/json")
    @PostMapping("/api/v1/check/provider/delete")
    void deleteProvider(DocumentRequest request);

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    class DocumentRequest {
        @Size(min = 1, max = 10)
        private String documentType;
        @Size(min = 1, max = 20)
        private String documentValue;
    }

}