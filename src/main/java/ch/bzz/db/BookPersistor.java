package ch.bzz.db;

import ch.bzz.model.Book;
import jakarta.persistence.EntityManager;
import java.util.List;

public class BookPersistor extends AbstractPersistor<Book> {

    public List<Book> getAll(int limit) {
        try (EntityManager em = getEntityManagerFactory().createEntityManager()) {
            var query = em.createQuery("SELECT b FROM Book b ORDER BY id", Book.class);
            if (limit > 0) query.setMaxResults(limit);
            return query.getResultList();
        }
    }
}
