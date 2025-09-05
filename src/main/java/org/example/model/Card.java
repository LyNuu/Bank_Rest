package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import org.example.model.dto.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String number;
    @Column(name = "user_email")
    private String userEmail;
    @Column(name = "expiration_date")
    private LocalDate expirationDate;
    private Status status;
    private BigDecimal amount;
}
