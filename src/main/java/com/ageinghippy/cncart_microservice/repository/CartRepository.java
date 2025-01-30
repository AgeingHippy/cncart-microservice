package com.ageinghippy.cncart_microservice.repository;

import com.ageinghippy.cncart_microservice.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {

    public Cart findByUserId(Long userId);

}
