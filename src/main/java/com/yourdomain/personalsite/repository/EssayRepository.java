package com.yourdomain.personalsite.repository;

import com.yourdomain.personalsite.entity.Essay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EssayRepository extends JpaRepository<Essay, Long> {
}
