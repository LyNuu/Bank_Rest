package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.model.dto.CardDto;
import org.example.model.dto.enums.Status;
import org.example.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @PostMapping
    public CardDto create(CardDto cardDto, Authentication authentication,
                          @RequestParam Status status) {
        String userEmail = authentication.getName();
        cardDto.setStatus(status);
        return cardService.createCard(cardDto, userEmail);
    }

    @GetMapping
    public List<CardDto> getCards(Authentication authentication) {
        String email = authentication.getName();
        return cardService.getUserCards(email);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<CardDto> getAllCards() {
        return cardService.getAllCards();
    }

    @PutMapping("/transfers")
    public ResponseEntity<String> transferFunds(Authentication authentication,
                                                @RequestParam BigDecimal amount,
                                                @RequestParam String fromCardNumber,
                                                @RequestParam String toCardNumber) {
        String email = authentication.getName();
        return ResponseEntity.ok(cardService.transferFunds(email, fromCardNumber, toCardNumber, amount));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteCard(Authentication authentication,
                                             @RequestParam String number) {
        String email = authentication.getName();
        return ResponseEntity.ok(cardService.deleteUserCard(email, number));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/status")
    public ResponseEntity<CardDto> changeStatus(Authentication authentication,
                                                @RequestParam Status status,
                                                @RequestParam String number) {
        String email = authentication.getName();
        return cardService.updateCardStatus(email, status, number);
    }

}
