package com.magnit.flux.service;

import com.magnit.flux.entity.Customer;
import com.magnit.flux.entity.Operation;
import com.magnit.flux.repository.CustomerRepository;
import com.magnit.flux.repository.OperationRepository;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class InitDataService {

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
        Flux.range(0, 100000)
            .map(i -> Customer.builder().name("Customer " + i).build())
            .subscribe(c -> customerRepository.save(c));

    }

    private void fillOperation() {
        List<Customer> customers = (List<Customer>) customerRepository.findAll();
        Flux.range(0, 100000)
            .map(i -> Operation.builder()
                .quantity((long) (Math.random() * 100))
                .customer(customers.get((int) (Math.random() * customers.size())))
                .build())
            .buffer(100000)
            .subscribe(operations -> operationRepository.saveAll(operations));
    }
}
