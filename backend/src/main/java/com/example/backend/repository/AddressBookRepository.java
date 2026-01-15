package com.example.backend.repository;

import com.example.backend.entity.AddressBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressBookRepository extends JpaRepository<AddressBook, String> {
    // Các hàm tìm kiếm tùy chỉnh nếu cần (VD: tìm list địa chỉ của 1 user)
    // List<AddressBook> findAllByUserId(String userId); (Cần mapping trong entity AddressBook nếu muốn dùng)
}