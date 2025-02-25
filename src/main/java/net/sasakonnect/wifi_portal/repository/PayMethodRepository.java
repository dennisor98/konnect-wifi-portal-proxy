package net.sasakonnect.wifi_portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.sasakonnect.wifi_portal.domain.PaymentMethod;

public interface PayMethodRepository extends JpaRepository<PaymentMethod,String> {
   Optional<PaymentMethod> findByName(String name);
}
