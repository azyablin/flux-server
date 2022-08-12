package com.magnit.flux.controller;

import com.magnit.flux.dao.StreamResultProducer;
import com.magnit.flux.entity.Operation;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/test")
@RequiredArgsConstructor
public class TestController {

    private final EntityManager entityManager;

    private final EntityManagerFactory entityManagerFactory;


    @GetMapping("/test")
    @Transactional
    public String test() {
        val query = entityManager.createQuery("select o from Operation o", Operation.class);
        //entityManager.getTransaction().begin();
        AtomicLong result = new AtomicLong(0);
        query.getResultStream().forEach(op -> {
            Operation operation = (Operation) op;
            result.addAndGet(((Operation) op).getQuantity());
        });
        //    entityManager.getTransaction().commit();
        return result.toString();
    }

    @GetMapping(path = "/operation", produces = "application/stream+json")
    public Flux<Operation> getOperations() {
        Mono<StreamResultProducer<Operation>> streamResultExecutorMono = StreamResultProducer
            .getInstance(entityManagerFactory);
        return Flux.usingWhen(streamResultExecutorMono,
            se -> se.execute("select o from Operation o", Operation.class),
            se -> se.commit());
    }


}
