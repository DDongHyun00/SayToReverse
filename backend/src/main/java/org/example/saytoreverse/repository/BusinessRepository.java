package org.example.saytoreverse.repository;

import org.example.saytoreverse.domain.Business;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessRepository extends JpaRepository<Business,Long> {

}
