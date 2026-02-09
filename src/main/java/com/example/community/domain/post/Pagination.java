package com.example.community.domain.post;

import lombok.Getter;
import lombok.ToString;

import org.springframework.data.domain.Pageable;

@Getter
@ToString
public class Pagination {

    private final long totalElements;
    private final int pageSize;
    private final int lastPage;
    private final int pageNumber;
    private final int pagesPerViewport;
    private final int startPage;
    private final int endPage;
    private final boolean prev;
    private final boolean next;

    private Pagination(Pageable pageable, long totalElements, int lastPage, int pagesPerViewport) {
        this.totalElements = totalElements;
        this.pageSize = pageable.getPageSize();
        this.lastPage = lastPage;
        this.pageNumber = pageable.getPageNumber() + 1;
        this.pagesPerViewport = pagesPerViewport;

        int endPage = (int)(Math.ceil((double)pageNumber / pagesPerViewport)) * pagesPerViewport;

        int startPage = endPage - pagesPerViewport + 1;
        if (startPage <= 0) startPage = 1;
        if (lastPage < endPage) endPage = lastPage;

        this.startPage = startPage;
        this.endPage = endPage;

        this.prev = this.startPage > 1;
        this.next = this.endPage < lastPage;
    }

    public static Pagination of(Pageable pageable, long totalElements, int lastPage) {
        return new Pagination(pageable, totalElements, lastPage, 10);
    }

    public static Pagination of(Pageable pageable, long totalElements) {
        return new Pagination(pageable, totalElements,
                (int)(Math.ceil((double)totalElements / pageable.getPageSize())), 10);
    }
}
