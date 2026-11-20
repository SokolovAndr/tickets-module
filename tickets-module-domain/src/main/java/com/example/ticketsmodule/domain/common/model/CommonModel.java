package com.example.ticketsmodule.domain.common.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class CommonModel {
    private UUID id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
