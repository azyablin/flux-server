package com.magnit.flux.dao;

import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import lombok.val;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class StreamResultProducer<T> {

    private EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;

    private StatelessSession statelessSession;

    public Flux<T> execute(String qlString, Class<T> resultClass) {
        Optional.ofNullable(entityManager).ifPresent(e -> {
            throw new RuntimeException("Cursor already open");
        });
        entityManager = entityManagerFactory.createEntityManager();
        statelessSession = entityManager.unwrap(Session.class).getSessionFactory()
            .openStatelessSession();
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
        if (!entityManager.isOpen()) {
            return Mono.empty();
        }
        if (entityManager.getTransaction().isActive()) {
            if (commitTran) {
                statelessSession.getTransaction().commit();
            } else {
                statelessSession.getTransaction().rollback();
            }

        }
        statelessSession.close();
        entityManager.close();
        return Mono.empty();
    }

    public static <T> Mono<StreamResultProducer<T>> getInstance(
        EntityManagerFactory entityManagerFactory) {
        return Mono.just(new StreamResultProducer(entityManagerFactory));
    }

    private StreamResultProducer(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;

    }

    private StreamResultProducer() {

    }

}