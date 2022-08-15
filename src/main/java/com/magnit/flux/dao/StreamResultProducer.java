package com.magnit.flux.dao;

import java.util.Optional;
import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class StreamResultProducer<T> {

    private final EntityManagerFactory entityManagerFactory;

    private StatelessSession statelessSession;

    public Flux<T> execute(String qlString, Class<T> resultClass) {
        Optional.ofNullable(statelessSession).ifPresent(e -> {
            throw new RuntimeException("Cursor already open");
        });
        val sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        statelessSession = sessionFactory.openStatelessSession();
        statelessSession.getTransaction().begin();
        val query = statelessSession.createQuery(qlString, resultClass);
        return Flux.fromStream(query.getResultStream());
    }

    public Mono<Void> commit() {
        return close(true);
    }

    public Mono<Void> rollback() {
        return close(false);
    }

    public Mono<Void> close(boolean commitTran) {
        if (statelessSession.getTransaction().isActive()) {
            if (commitTran) {
                statelessSession.getTransaction().commit();
            } else {
                statelessSession.getTransaction().rollback();
            }

        }
        statelessSession.close();
        return Mono.empty();
    }


}