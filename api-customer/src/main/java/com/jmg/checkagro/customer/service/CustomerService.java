package com.jmg.checkagro.customer.service;

import com.jmg.checkagro.customer.client.CheckMSClient;
import com.jmg.checkagro.customer.exception.CustomerException;
import com.jmg.checkagro.customer.exception.MessageCode;
import com.jmg.checkagro.customer.model.Customer;
import com.jmg.checkagro.customer.repository.CustomerRepository;
import com.jmg.checkagro.customer.utils.DateTimeUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CheckMSClient client;

    public CustomerService(CustomerRepository customerRepository, CheckMSClient client) {
        this.customerRepository = customerRepository;
        this.client = client;
    }

    @Transactional
    public Long create(Customer entity) throws CustomerException {
        if(customerRepository.findByDocumentTypeAndDocumentNumber(entity.getDocumentType(),entity.getDocumentNumber()).isPresent()){
            throw new CustomerException(MessageCode.CUSTOMER_EXISTS);
        }
        entity.setCreation(DateTimeUtils.now());
        entity.setActive(true);
        customerRepository.save(entity);
        registerCustomerInMSCheck(entity);
        return entity.getId();
    }

    private void registerCustomerInMSCheck(Customer entity) {
        client.registerCustomer(CheckMSClient.DocumentRequest.builder()
                .documentType(entity.getDocumentType())
                .documentValue(entity.getDocumentNumber())
                .build());
    }

    private void deleteCustomerInMSCheck(Customer entity) {
        client.deleteCustomer(CheckMSClient.DocumentRequest.builder()
                .documentType(entity.getDocumentType())
                .documentValue(entity.getDocumentNumber())
                .build());
    }

    public void update(Long id, Customer entity) throws CustomerException {
        var entityUpdate = customerRepository.findById(id).orElseThrow(() -> new CustomerException(MessageCode.CUSTOMER_NOT_FOUND));
        entity.setDocumentType(entityUpdate.getDocumentType());
        entity.setDocumentNumber(entityUpdate.getDocumentNumber());
        entity.setId(entityUpdate.getId());
        entity.setActive(entityUpdate.getActive());
        entity.setCreation(entityUpdate.getCreation());
        customerRepository.save(entity);
    }

    public void deleteById(Long id) throws CustomerException {
        var entityDelete = customerRepository.findById(id).orElseThrow(() -> new CustomerException(MessageCode.CUSTOMER_NOT_FOUND));
        customerRepository.updateActive(false, id);
        deleteCustomerInMSCheck(entityDelete);

    }

    public Customer getById(Long id) throws CustomerException {
        return customerRepository.findByIdAndActive(id, true).orElseThrow(() -> new CustomerException(MessageCode.CUSTOMER_NOT_FOUND));
    }

}