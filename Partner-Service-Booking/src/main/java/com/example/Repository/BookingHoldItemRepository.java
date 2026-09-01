package com.example.Repository;

import com.example.Model.BookingHoldItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingHoldItemRepository extends JpaRepository<BookingHoldItem, String> {

    List<BookingHoldItem> findAllByHoldIdOrderByInventoryIdAsc(String holdId);
}
