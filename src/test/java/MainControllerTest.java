import org.example.controller.MainController;
import org.example.model.dto.CardDto;
import org.example.model.status.Status;
import org.example.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainControllerTest {

    private final String testUserEmail = "user@example.com";
    @Mock
    private CardService cardService;
    @InjectMocks
    private MainController mainController;
    private Authentication authentication;
    private CardDto testCardDto;

    @BeforeEach
    void setUp() {
        authentication = new TestingAuthenticationToken(testUserEmail, "password");

        testCardDto = new CardDto();
        testCardDto.setNumber("4111111111111111");
        testCardDto.setExpirationDate(LocalDate.now().plusYears(2));
        testCardDto.setStatus(Status.ACTIVE);
        testCardDto.setAmount(new BigDecimal("1000.00"));
    }

    @Test
    void create_ShouldCreateCard() {
        when(cardService.createCard(any(CardDto.class), eq(testUserEmail))).thenReturn(testCardDto);
        CardDto result = mainController.create(testCardDto, authentication, Status.ACTIVE);
        assertNotNull(result);
        assertEquals(Status.ACTIVE, result.getStatus());
        verify(cardService, times(1)).createCard(any(CardDto.class), eq(testUserEmail));
    }

    @Test
    void getCards_ShouldReturnUserCards() {
        List<CardDto> userCards = List.of(testCardDto);
        when(cardService.getUserCards(testUserEmail)).thenReturn(userCards);
        List<CardDto> result = mainController.getCards(authentication);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardService, times(1)).getUserCards(testUserEmail);
    }

    @Test
    void getAllCards_ShouldReturnAllCards() {
        List<CardDto> allCards = List.of(testCardDto);
        when(cardService.getAllCards()).thenReturn(allCards);
        List<CardDto> result = mainController.getAllCards();
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardService, times(1)).getAllCards();
    }

    @Test
    void changeBalance_ShouldTransferFunds() {
        String fromCard = "4111111111111111";
        String toCard = "4222222222222222";
        BigDecimal amount = new BigDecimal("100.00");
        when(cardService.transferFunds(testUserEmail, fromCard, toCard, amount)).thenReturn("Success");
        ResponseEntity<String> result = mainController.changeBalance(authentication, amount, fromCard, toCard);
        assertNotNull(result);
        assertEquals("Success", result.getBody());
        verify(cardService, times(1)).transferFunds(testUserEmail, fromCard, toCard, amount);
    }

    @Test
    void deleteCard_ShouldDeleteCard() {
        String cardNumber = "4111111111111111";
        when(cardService.deleteUserCard(testUserEmail, cardNumber)).thenReturn("Success");
        ResponseEntity<String> result = mainController.deleteCard(authentication, cardNumber);
        assertNotNull(result);
        assertEquals("Success", result.getBody());
        verify(cardService, times(1)).deleteUserCard(testUserEmail, cardNumber);
    }

    @Test
    void changeStatus_ShouldUpdateCardStatus() {
        String cardNumber = "4111111111111111";
        Status newStatus = Status.BLOCKED;
        when(cardService.updateCardStatus(testUserEmail, newStatus, cardNumber))
                .thenReturn(ResponseEntity.ok(testCardDto));
        ResponseEntity<CardDto> result = mainController.changeStatus(authentication, newStatus, cardNumber);
        assertNotNull(result);
        assertTrue(result.getStatusCode().is2xxSuccessful());
        verify(cardService, times(1)).updateCardStatus(testUserEmail, newStatus, cardNumber);
    }
}