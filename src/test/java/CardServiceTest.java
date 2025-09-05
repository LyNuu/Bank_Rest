import jakarta.persistence.EntityNotFoundException;
import org.example.model.Card;
import org.example.model.dto.CardDto;
import org.example.model.dto.mapper.CardMapper;
import org.example.model.status.Status;
import org.example.repository.CardRepository;
import org.example.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    private final String testUserEmail = "user@example.com";
    private final String testCardNumber = "4111111111111111";

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardService cardService;

    private CardDto testCardDto;
    private Card testCard;

    @BeforeEach
    void setUp() {
        testCardDto = new CardDto();
        testCardDto.setExpirationDate(LocalDate.now().plusYears(2));
        testCardDto.setStatus(Status.ACTIVE);
        testCardDto.setAmount(new BigDecimal("1000.00"));

        testCard = new Card();
        testCard.setId(1L);
        testCard.setNumber(testCardNumber);
        testCard.setUserEmail(testUserEmail);
        testCard.setExpirationDate(LocalDate.now().plusYears(2));
        testCard.setStatus(Status.ACTIVE);
        testCard.setAmount(new BigDecimal("1000.00"));
    }

    @Test
    void createCard_ShouldCreateCardSuccessfully() {
        when(cardMapper.toEntity(testCardDto)).thenReturn(testCard);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toDto(testCard)).thenReturn(testCardDto);
        CardDto result = cardService.createCard(testCardDto, testUserEmail);
        assertNotNull(result);
        assertEquals(testCardDto.getStatus(), result.getStatus());
        verify(cardRepository, times(1)).save(any(Card.class));
        verify(cardMapper, times(1)).toDto(testCard);
    }

    @Test
    void getUserCards_ShouldReturnUserCards() {
        List<Card> userCards = List.of(testCard);
        when(cardRepository.findByUserEmail(testUserEmail)).thenReturn(userCards);
        when(cardMapper.toDto(testCard)).thenReturn(testCardDto);
        List<CardDto> result = cardService.getUserCards(testUserEmail);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardRepository, times(1)).findByUserEmail(testUserEmail);
    }

    @Test
    void getAllCards_ShouldReturnAllCards() {
        List<Card> allCards = List.of(testCard);
        when(cardRepository.findAll()).thenReturn(allCards);
        when(cardMapper.toDto(testCard)).thenReturn(testCardDto);
        List<CardDto> result = cardService.getAllCards();
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardRepository, times(1)).findAll();
    }

    @Test
    void transferFunds_ShouldTransferSuccessfully() {
        String fromCardNumber = "4111111111111111";
        String toCardNumber = "4222222222222222";
        BigDecimal amount = new BigDecimal("100.00");

        Card fromCard = new Card();
        fromCard.setNumber(fromCardNumber);
        fromCard.setUserEmail(testUserEmail);
        fromCard.setStatus(Status.ACTIVE);
        fromCard.setAmount(new BigDecimal("500.00"));

        Card toCard = new Card();
        toCard.setNumber(toCardNumber);
        toCard.setUserEmail("other@example.com");
        toCard.setStatus(Status.ACTIVE);
        toCard.setAmount(new BigDecimal("200.00"));

        when(cardRepository.findByNumber(fromCardNumber)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByNumber(toCardNumber)).thenReturn(Optional.of(toCard));
        when(cardRepository.saveAll(anyList())).thenReturn(List.of(fromCard, toCard));
        String result = cardService.transferFunds(testUserEmail, fromCardNumber, toCardNumber, amount);

        assertEquals("Success", result);
        assertEquals(new BigDecimal("400.00"), fromCard.getAmount());
        assertEquals(new BigDecimal("300.00"), toCard.getAmount());
        verify(cardRepository, times(1)).saveAll(anyList());
    }

    @Test
    void transferFunds_ShouldThrowException_WhenFromCardNotFound() {
        String fromCardNumber = "invalid_card";
        when(cardRepository.findByNumber(fromCardNumber)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                cardService.transferFunds(testUserEmail, fromCardNumber, "4222222222222222", new BigDecimal("100.00"))
        );
    }

    @Test
    void transferFunds_ShouldThrowSecurityException_WhenUserNotOwner() {
        String differentUserEmail = "different@example.com";
        testCard.setUserEmail(differentUserEmail);
        when(cardRepository.findByNumber(testCardNumber)).thenReturn(Optional.of(testCard));
        assertThrows(SecurityException.class, () ->
                cardService.transferFunds(testUserEmail, testCardNumber, "4222222222222222", new BigDecimal("100.00"))
        );
    }

    @Test
    void transferFunds_ShouldThrowException_WhenCardBlocked() {
        testCard.setStatus(Status.BLOCKED);
        when(cardRepository.findByNumber(testCardNumber)).thenReturn(Optional.of(testCard));
        assertThrows(SecurityException.class, () ->
                cardService.transferFunds(testUserEmail, testCardNumber, "4222222222222222", new BigDecimal("100.00"))
        );
    }

    @Test
    void transferFunds_ShouldThrowException_WhenInsufficientFunds() {
        String fromCardNumber = "4111111111111111";
        String toCardNumber = "4222222222222222";
        BigDecimal largeAmount = new BigDecimal("2000.00");
        Card fromCard = new Card();
        fromCard.setNumber(fromCardNumber);
        fromCard.setUserEmail(testUserEmail);
        fromCard.setStatus(Status.ACTIVE);
        fromCard.setAmount(new BigDecimal("1000.00"));
        Card toCard = new Card();
        toCard.setNumber(toCardNumber);
        toCard.setUserEmail("recipient@example.com");
        toCard.setStatus(Status.ACTIVE);
        toCard.setAmount(new BigDecimal("500.00"));
        when(cardRepository.findByNumber(fromCardNumber)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByNumber(toCardNumber)).thenReturn(Optional.of(toCard));
        assertThrows(IllegalArgumentException.class, () ->
                cardService.transferFunds(testUserEmail, fromCardNumber, toCardNumber, largeAmount)
        );
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                cardService.transferFunds(testUserEmail, fromCardNumber, toCardNumber, largeAmount)
        );
        assertTrue(exception.getMessage().contains("Insufficient funds"));
    }

    @Test
    void deleteUserCard_ShouldDeleteSuccessfully() {
        when(cardRepository.findByNumber(testCardNumber)).thenReturn(Optional.of(testCard));
        doNothing().when(cardRepository).delete(testCard);
        String result = cardService.deleteUserCard(testUserEmail, testCardNumber);
        assertEquals("Success", result);
        verify(cardRepository, times(1)).delete(testCard);
    }

    @Test
    void updateCardStatus_ShouldUpdateStatusSuccessfully() {
        Status newStatus = Status.BLOCKED;
        when(cardRepository.findByNumber(testCardNumber)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(testCard)).thenReturn(testCard);
        when(cardMapper.toDto(testCard)).thenReturn(testCardDto);
        ResponseEntity<CardDto> result = cardService.updateCardStatus(testUserEmail, newStatus, testCardNumber);
        assertNotNull(result);
        assertEquals(newStatus, testCard.getStatus());
        verify(cardRepository, times(1)).save(testCard);
    }
}