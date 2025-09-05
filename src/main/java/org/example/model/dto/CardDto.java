package org.example.model.dto;

import lombok.Data;
import org.example.model.dto.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardDto {
    private String number;
    private LocalDate expirationDate;
    private Status status;
    private BigDecimal amount;
}
