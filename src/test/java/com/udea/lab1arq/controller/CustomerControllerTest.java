package com.udea.lab1arq.controller;

import com.udea.lab1arq.DTO.CustomerDTO;
import com.udea.lab1arq.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private final CustomerDTO dto = new CustomerDTO(1L, "Ana", "Lopez", "ACC-1", 1000.0);

    @Test
    void getAllCustomersReturnsOk() {
        when(customerService.getAllCustomer()).thenReturn(List.of(dto));

        ResponseEntity<List<CustomerDTO>> response = customerController.getAllCustomers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(dto);
    }

    @Test
    void getCustomerByIdReturnsOk() {
        when(customerService.getCustomerById(1L)).thenReturn(dto);

        ResponseEntity<CustomerDTO> response = customerController.getCustomerById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void createCustomerReturnsOk() {
        when(customerService.createCustomer(dto)).thenReturn(dto);

        ResponseEntity<CustomerDTO> response = customerController.createCustomer(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void createCustomerRejectsNullBalance() {
        CustomerDTO withoutBalance = new CustomerDTO(null, "Ana", "Lopez", "ACC-1", null);

        assertThatThrownBy(() -> customerController.createCustomer(withoutBalance))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Balance cannot be null");
    }

    @Test
    void updateCustomerReturnsOk() {
        when(customerService.updateCustomer(1L, dto)).thenReturn(dto);

        ResponseEntity<?> response = customerController.updateCustomer(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void updateCustomerReturnsBadRequestOnError() {
        when(customerService.updateCustomer(1L, dto)).thenThrow(new IllegalArgumentException("error"));

        ResponseEntity<?> response = customerController.updateCustomer(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("error");
    }

    @Test
    void deleteCustomerReturnsNoContent() {
        ResponseEntity<?> response = customerController.deleteCustomer(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(customerService).deleteCustomer(1L);
    }

    @Test
    void deleteCustomerReturnsBadRequestOnError() {
        doThrow(new IllegalArgumentException("no existe")).when(customerService).deleteCustomer(99L);

        ResponseEntity<?> response = customerController.deleteCustomer(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("no existe");
    }
}
