package com.udea.lab1arq.controller;

import com.udea.lab1arq.DTO.TransactionDTO;
import com.udea.lab1arq.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private final TransactionDTO dto =
            new TransactionDTO(1L, "ACC-1", "ACC-2", 50.0, LocalDateTime.of(2026, 1, 1, 10, 0));

    @Test
    void transferMoneyReturnsOk() {
        when(transactionService.transferMoney(dto)).thenReturn(dto);

        ResponseEntity<?> response = transactionController.transferMoney(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void transferMoneyReturnsBadRequestOnError() {
        when(transactionService.transferMoney(dto))
                .thenThrow(new IllegalArgumentException("Saldo insuficiente en la cuenta del remitente."));

        ResponseEntity<?> response = transactionController.transferMoney(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Saldo insuficiente en la cuenta del remitente.");
    }

    @Test
    void getTransactionsByAccountReturnsList() {
        when(transactionService.getTransactionsForAccount("ACC-1")).thenReturn(List.of(dto));

        List<TransactionDTO> result = transactionController.getTransactionsByAccount("ACC-1");

        assertThat(result).containsExactly(dto);
    }
}
