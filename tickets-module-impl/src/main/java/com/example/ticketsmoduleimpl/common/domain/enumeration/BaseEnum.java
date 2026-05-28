package com.example.ticketsmoduleimpl.common.domain.enumeration;

import com.example.ticketsmoduleimpl.common.domain.exception.EnumException;

import java.util.Objects;
import java.util.stream.Stream;

public interface BaseEnum {

    static <E extends Enum<E> & BaseEnum> E getById(Integer id, Class<E> clazz) {
        return Stream.of(clazz.getEnumConstants())
                .filter(value -> Objects.equals(value.getId(), id))
                .findFirst()
                .orElseThrow(
                        () -> new EnumException(
                                String.format("Couldn't find %s with id [%d]", clazz.getSimpleName(), id)));
    }

    Integer getId();

    String getDescription();
}