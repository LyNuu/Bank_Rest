package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.model.Card;
import org.example.model.dto.CardDto;
import org.example.model.dto.enums.Status;
import org.example.model.dto.mapper.CardMapper;
import org.example.repository.CardRepository;
import org.hibernate.tool.schema.spi.SqlScriptException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static net.andreinc.mockneat.types.enums.CreditCardType.VISA_16;
import static net.andreinc.mockneat.unit.financial.CreditCards.creditCards;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    public CardDto createCard(CardDto cardDto, String userEmail) {
        Card card = cardMapper.toEntity(cardDto);
        card.setUserEmail(userEmail);
        String visa16 = creditCards().type(VISA_16).get();
        card.setNumber(visa16);
        cardRepository.save(card);
        return cardMapper.toDto(card);
    }

    public List<CardDto> getUserCards(String email) {
        List<Card> cards = cardRepository.findByUserEmail(email);
        return cards.stream()
                .map(cardMapper::toDto)
                .toList();
    }

    public List<CardDto> getAllCards() {
        List<Card> cards = cardRepository.findAll();
        return cards.stream()
                .map(cardMapper::toDto)
                .toList();
    }

    @Transactional
    public String transferFunds(String email, String fromCardNumber,
                                String toCardNumber, BigDecimal amount) {
        if (fromCardNumber.equals(toCardNumber)) {
            throw new IllegalArgumentException("Can't transfer from and to the same cardNumbers");
        }
        Card fromCard = cardRepository.findByNumber(fromCardNumber).orElseThrow(
                () -> new EntityNotFoundException("Sender's card not found: " + fromCardNumber));
        Card toCard = cardRepository.findByNumber(toCardNumber).orElseThrow(
                () -> new EntityNotFoundException("Card not found: " + toCardNumber));

        validateCards(email, amount, fromCard, toCard);
        BigDecimal newFromBalance = fromCard.getAmount().subtract(amount);
        BigDecimal newToBalance = toCard.getAmount().add(amount);
        fromCard.setAmount(newFromBalance);
        toCard.setAmount(newToBalance);
        cardRepository.saveAll(List.of(fromCard, toCard));
        return "Success";
    }

    @Transactional
    public String deleteUserCard(String email, String number) {
        Card card = cardRepository.findByNumber(number)
                .orElseThrow(() -> new EntityNotFoundException("Sender's card not found: " + number));
        if (!email.equals(card.getUserEmail())) {
            throw new SecurityException("You are not the owner of the sender's card.");
        }
        cardRepository.delete(card);
        return "Success";
    }

    @Transactional
    public ResponseEntity<CardDto> updateCardStatus(String email, Status status, String number) {
        Card card = cardRepository.findByNumber(number)
                .orElseThrow(() -> new EntityNotFoundException("Card not found " + number));
        if (!email.equals(card.getUserEmail())) {
            throw new SqlScriptException("You are not the owner of the sender's card.");
        }
        card.setStatus(status);
        return ResponseEntity.ok(cardMapper.toDto(cardRepository.save(card)));
    }

    private static void validateCards(
            String email,
            BigDecimal amount,
            Card fromCard,
            Card toCard
    ) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Can't transfer non positive amount.");
        }
        validateStatus(fromCard);
        validateStatus(toCard);
        if (!email.equals(fromCard.getUserEmail())) {
            throw new SecurityException("You are not the owner of the sender's card.");
        }
        if (fromCard.getAmount().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds on the card: " + fromCard.getNumber());
        }
    }

    private static void validateStatus(Card fromCard) {
        if (fromCard.getStatus().equals(Status.BLOCKED) || fromCard.getStatus().equals(Status.EXPIRED)) {
            throw new SecurityException("Status of your card does not allow this operation: " + fromCard.getStatus());
        }
    }
}
