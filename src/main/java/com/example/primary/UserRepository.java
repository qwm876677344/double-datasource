package com.example.primary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author qiuwm.
 * @version 1.00.00
 * @date 2019/5/23.
 */
@Repository
public interface  UserRepository extends JpaRepository<User, Long> {
}
