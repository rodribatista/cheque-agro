package com.jmg.checkagro.customer.client;

import com.jmg.checkagro.customer.config.LoadBalancerConfig;
import feign.Headers;
import lombok.*;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.constraints.Size;

@FeignClient(name = "api-check")
@LoadBalancerClient(value = "api-customer", configuration = LoadBalancerConfig.class)
public interface CheckMSClient {

    @PostMapping("/api/v1/check/customer/register")
    void registerCustomer(DocumentRequest request);

    @PostMapping("/api/v1/check/customer/delete")
    void deleteCustomer(DocumentRequest request);

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