package com.udea.lab1arq.model;

import com.udea.lab1arq.DTO.CustomerDTO;
import com.udea.lab1arq.DTO.TransactionDTO;
import com.udea.lab1arq.DTO.TransferRequestDTO;
import com.udea.lab1arq.entity.Customer;
import com.udea.lab1arq.entity.Transaction;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ModelTest {

    private final LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Test
    void customerDtoSettersAndGetters() {
        CustomerDTO dto = new CustomerDTO(null, null, null, null, null);
        dto.setId(1L);
        dto.setFirstName("Ana");
        dto.setLastName("Lopez");
        dto.setAccountNumber("ACC-1");
        dto.setBalance(10.0);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFirstName()).isEqualTo("Ana");
        assertThat(dto.getLastName()).isEqualTo("Lopez");
        assertThat(dto.getAccountNumber()).isEqualTo("ACC-1");
        assertThat(dto.getBalance()).isEqualTo(10.0);
    }

    @Test
    void transactionDtoSettersAndGetters() {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(1L);
        dto.setSenderAccountNumber("ACC-1");
        dto.setReceiverAccountNumber("ACC-2");
        dto.setAmount(5.0);
        dto.setTimestamp(now);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getSenderAccountNumber()).isEqualTo("ACC-1");
        assertThat(dto.getReceiverAccountNumber()).isEqualTo("ACC-2");
        assertThat(dto.getAmount()).isEqualTo(5.0);
        assertThat(dto.getTimestamp()).isEqualTo(now);
    }

    @Test
    void transferRequestDtoSettersAndGetters() {
        TransferRequestDTO dto = new TransferRequestDTO();
        dto.setSenderAccountNumber("ACC-1");
        dto.setReceiverAccountNumber("ACC-2");
        dto.setAmount(5.0);

        assertThat(dto.getSenderAccountNumber()).isEqualTo("ACC-1");
        assertThat(dto.getReceiverAccountNumber()).isEqualTo("ACC-2");
        assertThat(dto.getAmount()).isEqualTo(5.0);
    }

    @Test
    void customerSettersAndGetters() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setAccountNumber("ACC-1");
        customer.setFirstName("Ana");
        customer.setLastName("Lopez");
        customer.setBalance(10.0);

        assertThat(customer.getId()).isEqualTo(1L);
        assertThat(customer.getAccountNumber()).isEqualTo("ACC-1");
        assertThat(customer.getFirstName()).isEqualTo("Ana");
        assertThat(customer.getLastName()).isEqualTo("Lopez");
        assertThat(customer.getBalance()).isEqualTo(10.0);
        assertThat(customer.toString()).contains("ACC-1", "Ana", "Lopez");
    }

    @Test
    void customerEqualsAndHashCodeUseId() {
        Customer a = new Customer(1L, "ACC-1", "Ana", "Lopez", 10.0);
        Customer sameId = new Customer(1L, "ACC-9", "Otro", "Nombre", 0.0);
        Customer otherId = new Customer(2L, "ACC-1", "Ana", "Lopez", 10.0);

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(sameId);
        assertThat(a).hasSameHashCodeAs(sameId);
        assertThat(a).isNotEqualTo(otherId);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("ACC-1");
    }

    @Test
    void transactionSettersAndGetters() {
        Transaction transaction = new Transaction();
        assertThat(transaction.getTimestamp()).isNotNull();

        transaction.setId(1L);
        transaction.setSenderAccountNumber("ACC-1");
        transaction.setReceiverAccountNumber("ACC-2");
        transaction.setAmount(5.0);
        transaction.setTimestamp(now);

        assertThat(transaction.getId()).isEqualTo(1L);
        assertThat(transaction.getSenderAccountNumber()).isEqualTo("ACC-1");
        assertThat(transaction.getReceiverAccountNumber()).isEqualTo("ACC-2");
        assertThat(transaction.getAmount()).isEqualTo(5.0);
        assertThat(transaction.getTimestamp()).isEqualTo(now);
    }
}
