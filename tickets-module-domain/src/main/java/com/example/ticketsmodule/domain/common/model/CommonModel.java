package com.example.ticketsmodule.domain.common.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class CommonModel {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
