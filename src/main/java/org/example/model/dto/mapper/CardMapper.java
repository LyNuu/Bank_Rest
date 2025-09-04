package org.example.model.dto.mapper;

import org.example.model.Card;
import org.example.model.dto.CardDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper {
    CardDto toDto(Card card);

    Card toEntity(CardDto cardDto);
}
