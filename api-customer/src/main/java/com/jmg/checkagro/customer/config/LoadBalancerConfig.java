package com.jmg.checkagro.customer.config;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class LoadBalancerConfig {

  @Bean
  ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(
    Environment environment,
    LoadBalancerClientFactory loadBalancerClientFactory
  ) {
    String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
    return new RoundRobinLoadBalancer(loadBalancerClientFactory
      .getLazyProvider(name, ServiceInstanceListSupplier.class), name);
  }

}