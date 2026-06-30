package com.tourly.support.repository;

import com.tourly.support.entity.SupportTicket;
import com.tourly.support.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    Page<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<SupportTicket> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status, Pageable pageable);

    long countByUserIdAndStatus(Long userId, TicketStatus status);

    long countByStatus(TicketStatus status);

    long countByStatusIn(java.util.List<TicketStatus> statuses);
}
