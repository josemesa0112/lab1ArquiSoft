package com.udea.lab1arq.service;

import com.udea.lab1arq.DTO.TransactionDTO;
import com.udea.lab1arq.entity.Customer;
import com.udea.lab1arq.entity.Transaction;
import com.udea.lab1arq.repository.CustomerRepository;
import com.udea.lab1arq.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private TransactionService transactionService;

    private final LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Test
    void transferMoneyMovesBalanceAndSavesTransaction() {
        Customer sender = new Customer(1L, "ACC-1", "Ana", "Lopez", 1000.0);
        Customer receiver = new Customer(2L, "ACC-2", "Luis", "Perez", 200.0);
        when(customerRepository.findByAccountNumber("ACC-1")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("ACC-2")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });

        TransactionDTO result = transactionService.transferMoney(
                new TransactionDTO(null, "ACC-1", "ACC-2", 300.0, now));

        assertThat(sender.getBalance()).isEqualTo(700.0);
        assertThat(receiver.getBalance()).isEqualTo(500.0);
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getSenderAccountNumber()).isEqualTo("ACC-1");
        assertThat(result.getReceiverAccountNumber()).isEqualTo("ACC-2");
        assertThat(result.getAmount()).isEqualTo(300.0);
        assertThat(result.getTimestamp()).isEqualTo(now);
        verify(customerRepository).save(sender);
        verify(customerRepository).save(receiver);
    }

    @Test
    void transferMoneyThrowsWhenSenderAccountNull() {
        TransactionDTO dto = new TransactionDTO(null, null, "ACC-2", 10.0, now);

        assertThatThrownBy(() -> transactionService.transferMoney(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatorios");
    }

    @Test
    void transferMoneyThrowsWhenReceiverAccountNull() {
        TransactionDTO dto = new TransactionDTO(null, "ACC-1", null, 10.0, now);

        assertThatThrownBy(() -> transactionService.transferMoney(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatorios");
    }

    @Test
    void transferMoneyThrowsWhenSenderMissing() {
        when(customerRepository.findByAccountNumber("ACC-1")).thenReturn(Optional.empty());
        TransactionDTO dto = new TransactionDTO(null, "ACC-1", "ACC-2", 10.0, now);

        assertThatThrownBy(() -> transactionService.transferMoney(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("remitente no existe");
    }

    @Test
    void transferMoneyThrowsWhenReceiverMissing() {
        when(customerRepository.findByAccountNumber("ACC-1"))
                .thenReturn(Optional.of(new Customer(1L, "ACC-1", "Ana", "Lopez", 1000.0)));
        when(customerRepository.findByAccountNumber("ACC-2")).thenReturn(Optional.empty());
        TransactionDTO dto = new TransactionDTO(null, "ACC-1", "ACC-2", 10.0, now);

        assertThatThrownBy(() -> transactionService.transferMoney(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("receptor no existe");
    }

    @Test
    void transferMoneyThrowsWhenInsufficientBalance() {
        when(customerRepository.findByAccountNumber("ACC-1"))
                .thenReturn(Optional.of(new Customer(1L, "ACC-1", "Ana", "Lopez", 50.0)));
        when(customerRepository.findByAccountNumber("ACC-2"))
                .thenReturn(Optional.of(new Customer(2L, "ACC-2", "Luis", "Perez", 0.0)));
        TransactionDTO dto = new TransactionDTO(null, "ACC-1", "ACC-2", 100.0, now);

        assertThatThrownBy(() -> transactionService.transferMoney(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Saldo insuficiente");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getTransactionsForAccountReturnsMappedList() {
        Transaction transaction = new Transaction(5L, "ACC-1", "ACC-2", 20.0, now);
        when(transactionRepository.findBySenderAccountNumberOrReceiverAccountNumber("ACC-1", "ACC-1"))
                .thenReturn(List.of(transaction));

        List<TransactionDTO> result = transactionService.getTransactionsForAccount("ACC-1");

        assertThat(result).hasSize(1);
        TransactionDTO dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getSenderAccountNumber()).isEqualTo("ACC-1");
        assertThat(dto.getReceiverAccountNumber()).isEqualTo("ACC-2");
        assertThat(dto.getAmount()).isEqualTo(20.0);
        assertThat(dto.getTimestamp()).isEqualTo(now);
    }
}
