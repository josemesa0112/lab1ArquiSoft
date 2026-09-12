package com.udea.lab1arq.service;

import com.udea.lab1arq.DTO.CustomerDTO;
import com.udea.lab1arq.entity.Customer;
import com.udea.lab1arq.mapper.CustomerMapper;
import com.udea.lab1arq.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    public List<CustomerDTO> getAllCustomer(){
        return customerRepository.findAll().stream()
                .map(customerMapper::toDTO).toList();
    }

    public CustomerDTO getCustomerById(Long id){
        return customerRepository.findById(id).map(customerMapper::toDTO)
                .orElseThrow(()-> new RuntimeException("Customer not found"));
    }

    public CustomerDTO createCustomer(CustomerDTO customerDTO){

        Customer customer= customerMapper.toEntity(customerDTO);
        return customerMapper.toDTO(customerRepository.save(customer));
    }

    //Actualizar un cliente: solo se cambian los campos que llegan en el DTO
    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO){

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El cliente con id " + id + " no existe."));

        String accountNumber = customerDTO.getAccountNumber();
        if (accountNumber != null && !accountNumber.isBlank()
                && !accountNumber.equals(customer.getAccountNumber())) {

            if (customerRepository.findByAccountNumber(accountNumber).isPresent()) {
                throw new IllegalArgumentException("Ya existe un cliente con el numero de cuenta " + accountNumber + ".");
            }
            customer.setAccountNumber(accountNumber);
        }

        if (customerDTO.getFirstName() != null && !customerDTO.getFirstName().isBlank()) {
            customer.setFirstName(customerDTO.getFirstName());
        }

        if (customerDTO.getLastName() != null && !customerDTO.getLastName().isBlank()) {
            customer.setLastName(customerDTO.getLastName());
        }

        if (customerDTO.getBalance() != null) {
            if (customerDTO.getBalance() < 0) {
                throw new IllegalArgumentException("El saldo no puede ser negativo.");
            }
            customer.setBalance(customerDTO.getBalance());
        }

        return customerMapper.toDTO(customerRepository.save(customer));
    }

    //Borrar un cliente por su Id
    public void deleteCustomer(Long id){

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El cliente con id " + id + " no existe."));

        customerRepository.delete(customer);
    }

}