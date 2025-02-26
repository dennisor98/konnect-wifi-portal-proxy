package net.sasakonnect.wifi_portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    Optional<Payment> findByTxtId(String checkoutId);
    
    @Query("SELECT p FROM Payment p JOIN FETCH p.app WHERE p.mobileNumber = :mobileNumber AND p.verified = false AND DATE(p.createdAt) = CURRENT_DATE ORDER BY p.createdAt DESC LIMIT 1")
    Optional<Payment> findxByMobileNumber(@Param("mobileNumber") String mobileNumber);
    
    
    @Query("SELECT p FROM Payment p WHERE (p.mobileNumber LIKE %:searchTerm%) OR (p.txtId LIKE %:searchTerm%) OR (p.konnectCheckoutId LIKE %:searchTerm%)")
    Page<Payment> searchTx(@Param("searchTerm") String txId,Pageable pageable);
    
    Long countByIsSuccessfulTrue();
    Long countByIsSuccessfulFalse();
    
    Page<Payment> findByVerified(Boolean verified,Pageable pageable);
    
    
    
    @Query(value = "SELECT DATE(created_at) AS transactionDate, COUNT(*) AS transactionCount, " +
    		"SUM(CAST(amount AS DECIMAL(10,2))) AS totalAmount " +
    		"FROM payment " +
    		"WHERE DATE(created_at) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
    		"GROUP BY DATE(created_at) " +
    		"ORDER BY transactionDate DESC", nativeQuery = true)
    List<Object[]> getWeeklyTransactionSummary();


    
    
}
