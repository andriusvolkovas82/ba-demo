package demo.payment.repository;

import demo.payment.model.Payment;
import demo.payment.model.PaymentStatusEnum;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PaymentRepositoryCustomImpl implements PaymentRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Payment> findAllByStatusAndAmount(PaymentStatusEnum status, BigDecimal amount) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Payment> cq = cb.createQuery(Payment.class);

        List<Predicate> predicates = new ArrayList<>();
        Root<Payment> payment = cq.from(Payment.class);
        predicates.add(cb.equal(payment.get("status"), status));
        if (amount != null) {
            predicates.add(cb.equal(payment.get("amount"), amount));
        }
        cq.where(predicates.toArray(new Predicate[0]));

        return em.createQuery(cq).getResultList();
    }
}
