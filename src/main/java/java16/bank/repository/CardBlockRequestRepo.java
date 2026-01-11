package java16.bank.repository;

import java16.bank.entity.CardBlockRequest;
import java16.bank.entity.User;
import java16.bank.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardBlockRequestRepo extends JpaRepository<CardBlockRequest, Long> {
    Page<CardBlockRequest> findByStatus(RequestStatus status, Pageable pageable);
    List<CardBlockRequest> findByUser(User user);
    Page<CardBlockRequest> findByUser(User user, Pageable pageable);
}
