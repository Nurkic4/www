package com.example.www.dto;

import lombok.Data;
import java.util.List;

@Data
public class PageResponse<T> {
    private Long total;
    private Integer pages;
    private Integer current;
    private Integer size;
    private List<T> records;
    
    public PageResponse(Long total, Integer pages, Integer current, Integer size, List<T> records) {
        this.total = total;
        this.pages = pages;
        this.current = current;
        this.size = size;
        this.records = records;
    }
} 