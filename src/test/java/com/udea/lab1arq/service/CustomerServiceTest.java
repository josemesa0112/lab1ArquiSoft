package com.udea.lab1arq.service;

import com.udea.lab1arq.DTO.CustomerDTO;
import com.udea.lab1arq.entity.Customer;
import com.udea.lab1arq.mapper.CustomerMapper;
import com.udea.lab1arq.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        CustomerMapper customerMapper = Mappers.getMapper(CustomerMapper.class);
        customerService = new CustomerService(customerRepository, customerMapper);
    }

    private Customer customer() {
        return new Customer(1L, "ACC-1", "Ana", "Lopez", 1000.0);
    }

    @Test
    void getAllCustomerReturnsMappedList() {
        when(customerRepository.findAll()).thenReturn(List.of(customer()));

        List<CustomerDTO> result = customerService.getAllCustomer();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountNumber()).isEqualTo("ACC-1");
    }

    @Test
    void getCustomerByIdReturnsCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer()));

        CustomerDTO result = customerService.getCustomerById(1L);

        assertThat(result.getFirstName()).isEqualTo("Ana");
    }

    @Test
    void getCustomerByIdThrowsWhenMissing() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Customer not found");
    }

    @Test
    void createCustomerSavesAndReturnsDto() {
        CustomerDTO input = new CustomerDTO(null, "Ana", "Lopez", "ACC-1", 1000.0);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer());

        CustomerDTO result = customerService.createCustomer(input);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBalance()).isEqualTo(1000.0);
    }

    @Test
    void updateCustomerChangesAllFields() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer()));
        when(customerRepository.findByAccountNumber("ACC-2")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerDTO result = customerService.updateCustomer(1L,
                new CustomerDTO(null, "Luis", "Perez", "ACC-2", 500.0));

        assertThat(result.getAccountNumber()).isEqualTo("ACC-2");
        assertThat(result.getFirstName()).isEqualTo("Luis");
        assertThat(result.getLastName()).isEqualTo("Perez");
        assertThat(result.getBalance()).isEqualTo(500.0);
    }

    @Test
    void updateCustomerIgnoresNullAndBlankFields() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer()));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerDTO result = customerService.updateCustomer(1L,
                new CustomerDTO(null, " ", null, "ACC-1", null));

        assertThat(result.getAccountNumber()).isEqualTo("ACC-1");
        assertThat(result.getFirstName()).isEqualTo("Ana");
        assertThat(result.getLastName()).isEqualTo("Lopez");
        assertThat(result.getBalance()).isEqualTo(1000.0);
        verify(customerRepository, never()).findByAccountNumber(any());
    }

    @Test
    void updateCustomerThrowsWhenMissing() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        CustomerDTO dto = new CustomerDTO(null, "Luis", null, null, null);

        assertThatThrownBy(() -> customerService.updateCustomer(99L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    void updateCustomerThrowsWhenAccountNumberTaken() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer()));
        when(customerRepository.findByAccountNumber("ACC-2"))
                .thenReturn(Optional.of(new Customer(2L, "ACC-2", "Otro", "Cliente", 10.0)));
        CustomerDTO dto = new CustomerDTO(null, null, null, "ACC-2", null);

        assertThatThrownBy(() -> customerService.updateCustomer(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe");
    }

    @Test
    void updateCustomerThrowsWhenBalanceNegative() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer()));
        CustomerDTO dto = new CustomerDTO(null, null, null, null, -5.0);

        assertThatThrownBy(() -> customerService.updateCustomer(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negativo");
    }

    @Test
    void deleteCustomerDeletesExisting() {
        Customer existing = customer();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));

        customerService.deleteCustomer(1L);

        verify(customerRepository).delete(existing);
    }

    @Test
    void deleteCustomerThrowsWhenMissing() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.deleteCustomer(99L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
