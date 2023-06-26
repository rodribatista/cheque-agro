package com.jmg.checkagro.provider.service;

import com.jmg.checkagro.provider.client.CheckMSClient;
import com.jmg.checkagro.provider.exception.MessageCode;
import com.jmg.checkagro.provider.exception.ProviderException;
import com.jmg.checkagro.provider.model.Provider;
import com.jmg.checkagro.provider.repository.ProviderRepository;
import com.jmg.checkagro.provider.utils.DateTimeUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final CheckMSClient client;

    public ProviderService(ProviderRepository providerRepository, CheckMSClient checkMSClient) {
        this.providerRepository = providerRepository;
        this.client = checkMSClient;
    }

    @Transactional
    public Long create(Provider entity) throws ProviderException {
        if(providerRepository.findByDocumentTypeAndDocumentNumber(entity.getDocumentType(),entity.getDocumentNumber()).isPresent()){
            throw new ProviderException(MessageCode.PROVIDER_EXISTS);
        }
        entity.setCreation(DateTimeUtils.now());
        entity.setActive(true);
        providerRepository.save(entity);
        registerProviderInMSCheck(entity);
        return entity.getId();
    }
    @Retry(name = "retryRegisterProvider")
    @CircuitBreaker(name = "registerProvider", fallbackMethod = "registerProviderInMSCheckFallback")
    private void registerProviderInMSCheck(Provider entity) {
        client.registerProvider(CheckMSClient.DocumentRequest.builder()
                .documentType(entity.getDocumentType())
                .documentValue(entity.getDocumentNumber())
                .build());
    }

    public void registerProviderInMSCheckFallback(Provider entity, Throwable exception) throws Exception {
        throw new Exception("Circuit Breaker - No se puede registrar un proveedor en este momento.");
    }

    private void deleteProviderInMSCheck(Provider entity) {
        client.deleteProvider(CheckMSClient.DocumentRequest.builder()
                .documentType(entity.getDocumentType())
                .documentValue(entity.getDocumentNumber())
                .build());
    }

    public void update(Long id, Provider entity) throws ProviderException {
        var entityUpdate = providerRepository.findById(id).orElseThrow(() -> new ProviderException(MessageCode.PROVIDER_NOT_FOUND));
        entity.setDocumentType(entityUpdate.getDocumentType());
        entity.setDocumentNumber(entityUpdate.getDocumentNumber());
        entity.setId(entityUpdate.getId());
        entity.setActive(entityUpdate.getActive());
        entity.setCreation(entityUpdate.getCreation());
        providerRepository.save(entity);
    }

    public void deleteById(Long id) throws ProviderException {
        var entityDelete = providerRepository.findById(id).orElseThrow(() -> new ProviderException(MessageCode.PROVIDER_NOT_FOUND));
        providerRepository.updateActive(false, id);
        deleteProviderInMSCheck(entityDelete);
    }

    public Provider getById(Long id) throws ProviderException {
        return providerRepository.findByIdAndActive(id, true).orElseThrow(() -> new ProviderException(MessageCode.PROVIDER_NOT_FOUND));
    }

}