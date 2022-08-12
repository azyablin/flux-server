package com.magnit.flux.controller;

import com.magnit.flux.dao.StreamResultProducer;
import com.magnit.flux.entity.Operation;
import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class OperationController {

    private final EntityManagerFactory entityManagerFactory;

    @MessageMapping("operations")
    public Flux<Operation> getWsOperations() {
        Mono<StreamResultProducer<Operation>> streamResultExecutorMono = StreamResultProducer
            .getInstance(entityManagerFactory);
        return Flux.usingWhen(streamResultExecutorMono,
            se -> se.execute("select o from Operation o JOIN FETCH o.customer c", Operation.class),
            se -> se.commit());
    }


}
