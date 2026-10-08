package com.udea.lab1arq.mapper;

import com.udea.lab1arq.DTO.CustomerDTO;
import com.udea.lab1arq.DTO.TransactionDTO;
import com.udea.lab1arq.entity.Customer;
import com.udea.lab1arq.entity.Transaction;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    private final CustomerMapper customerMapper = Mappers.getMapper(CustomerMapper.class);
    private final TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    @Test
    void customerToDtoCopiesFields() {
        CustomerDTO dto = customerMapper.toDTO(new Customer(1L, "ACC-1", "Ana", "Lopez", 1000.0));

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getAccountNumber()).isEqualTo("ACC-1");
        assertThat(dto.getFirstName()).isEqualTo("Ana");
        assertThat(dto.getLastName()).isEqualTo("Lopez");
        assertThat(dto.getBalance()).isEqualTo(1000.0);
    }

    @Test
    void customerToEntityCopiesFields() {
        Customer customer = customerMapper.toEntity(new CustomerDTO(1L, "Ana", "Lopez", "ACC-1", 1000.0));

        assertThat(customer.getId()).isEqualTo(1L);
        assertThat(customer.getAccountNumber()).isEqualTo("ACC-1");
        assertThat(customer.getFirstName()).isEqualTo("Ana");
        assertThat(customer.getLastName()).isEqualTo("Lopez");
        assertThat(customer.getBalance()).isEqualTo(1000.0);
    }

    @Test
    void customerMapperHandlesNull() {
        assertThat(customerMapper.toDTO(null)).isNull();
        assertThat(customerMapper.toEntity(null)).isNull();
    }

    @Test
    void transactionToDtoCopiesFields() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0);
        TransactionDTO dto = transactionMapper.toDTO(new Transaction(1L, "ACC-1", "ACC-2", 50.0, now));

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getSenderAccountNumber()).isEqualTo("ACC-1");
        assertThat(dto.getReceiverAccountNumber()).isEqualTo("ACC-2");
        assertThat(dto.getAmount()).isEqualTo(50.0);
        assertThat(dto.getTimestamp()).isEqualTo(now);
    }

    @Test
    void transactionMapperHandlesNull() {
        assertThat(transactionMapper.toDTO(null)).isNull();
    }
}
