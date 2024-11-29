package net.sasakonnect.wifi_portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.sasakonnect.wifi_portal.domain.Payment;
import net.sasakonnect.wifi_portal.domain.User;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByUser(User user);
    Optional<Payment> findByKonnectCheckoutId(String konnectCheckoutId);
    
    @Query("SELECT p FROM Payment p JOIN FETCH p.app JOIN FETCH p.user WHERE p.txtId = :txtId")
    Optional<Payment> findByTxtIdIgnoreCase(@Param("txtId") String txtId);

}
