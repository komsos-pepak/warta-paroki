package komsos.wartaparoki.helper;

import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionFilter {
    private final EntityManager entityManager;

    public void openSessionFilterIsDeletedAndIsActive(Boolean isDeleted, Optional<Boolean> isActive) {
        Session session = entityManager.unwrap(Session.class);
        Filter filterDeleted = session.enableFilter("isDeletedFilter");
        filterDeleted.setParameter("isDeleted", isDeleted);
        if (isActive.isPresent()) {
            Filter filterAccount = session.enableFilter("isActiveFilter");
            filterAccount.setParameter("isActive", isActive.get());
        }
    }

    public void closeSessionFilterIsDeletedAndIsActive() {
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("isDeletedFilter");
        session.disableFilter("isActiveFilter");
    }

    public void openSessionFilterIsDeleted(Boolean isDeleted) {
        Session session = entityManager.unwrap(Session.class);
        Filter filterDeleted = session.enableFilter("isDeletedFilter");
        filterDeleted.setParameter("isDeleted", isDeleted);
    }

    public void closeSessionFilterIsDeleted() {
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("isDeletedFilter");
    }
}
