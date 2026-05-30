package com.tourly.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tourly.common.entity.SupportTicket;
import com.tourly.common.enums.TicketStatus;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    long countByStatusIn(List<TicketStatus> statuses);
}
