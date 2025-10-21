package ch.bzz.db;

import ch.bzz.model.User;
import jakarta.persistence.EntityManager;


public class UserPersistor extends AbstractPersistor<User> {

    public void saveUser(User user) {
        save(user); // nutzt die generische save() Methode aus AbstractPersistor
    }

    public User findByEmail(String email) {
        try (EntityManager em = getEntityManagerFactory().createEntityManager()) {
            var query = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            return query.getResultStream().findFirst().orElse(null);
        }
    }

    public User findById(Integer id) {
        try (EntityManager em = getEntityManagerFactory().createEntityManager()) {
            return em.find(User.class, id);
        }
    }
}



