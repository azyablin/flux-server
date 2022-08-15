package com.magnit.flux.controller;

import com.magnit.flux.dao.StreamResultProducer;
import com.magnit.flux.entity.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OperationController {

    private final ObjectFactory<StreamResultProducer<Operation>> streamResultProducerObjectFactory;


    @GetMapping(path = "/operations")
    public Flux<Operation> getOperations() {
        return getOperationFlux();
    }

    @GetMapping(path = "/operations-stream", produces = "application/stream+json")
    public Flux<Operation> getOperationsStream() {
        return getOperationFlux();
    }

    @MessageMapping("operations")
    public Flux<Operation> getOperationsWs() {
        return getOperationFlux();
    }

    private Flux<Operation> getOperationFlux() {
        Mono<StreamResultProducer<Operation>> streamResultExecutorMono = Mono
            .just(streamResultProducerObjectFactory.getObject());
        return Flux.usingWhen(streamResultExecutorMono,
            se -> se.execute("select o from Operation o JOIN FETCH o.customer c", Operation.class),
            StreamResultProducer::commit);
    }

}
