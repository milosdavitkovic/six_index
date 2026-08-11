package com.six.indexreview.infrastructure.persistence.repository;

import com.six.indexreview.infrastructure.persistence.entity.SecurityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityRepository extends JpaRepository<SecurityEntity, Integer> {
}
