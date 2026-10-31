package com.example.ticketsmodule.domain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageRequest {
    private int page;
    private int size;
    private String sortField;
    private SortDirection direction;
}