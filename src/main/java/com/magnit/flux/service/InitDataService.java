package com.magnit.flux.service;

import com.magnit.flux.entity.Customer;
import com.magnit.flux.entity.Operation;
import com.magnit.flux.repository.CustomerRepository;
import com.magnit.flux.repository.OperationRepository;
import java.util.List;
import java.util.Random;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class InitDataService {

    private static final int OPERATION_RECORD_LIMIT = 100000;

    private static final int CUSTOMER_RECORD_LIMIT = 100;

    private static final Random random = new Random();

    private final OperationRepository operationRepository;

    private final CustomerRepository customerRepository;

    @PostConstruct
    public void init() {
        if (customerRepository.count() == 0) {
            fillCustomer();
        }
        if (operationRepository.count() == 0) {
            fillOperation();
        }
    }

    private void fillCustomer() {
        Flux.range(0, CUSTOMER_RECORD_LIMIT)
            .map(i -> Customer.builder().name("Customer " + i).build())
            .subscribe(customerRepository::save);

    }

    private void fillOperation() {
        List<Customer> customers = (List<Customer>) customerRepository.findAll();
        Flux.range(0, OPERATION_RECORD_LIMIT)
            .map(i -> Operation.builder()
                .quantity((long) random.nextInt(100))
                .customer(customers.get(random.nextInt(customers.size() - 1)))
                .build())
            .buffer(1000)
            .subscribe(operationRepository::saveAll);
    }
}
