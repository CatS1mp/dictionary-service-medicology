package com.medicology.dictionary.common.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PaginatedResponse<T> {
    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final int numberOfElements;
    private final boolean first;
    private final boolean last;
    private final boolean empty;

    public static <T> PaginatedResponse<T> fromList(List<T> source, int page, int size) {
        List<T> safeSource = source == null ? Collections.emptyList() : source;
        int safeSize = Math.max(1, size);
        int safePage = Math.max(0, page);
        int fromIndex = Math.min(safePage * safeSize, safeSource.size());
        int toIndex = Math.min(fromIndex + safeSize, safeSource.size());
        List<T> content = safeSource.subList(fromIndex, toIndex);
        int totalPages = safeSource.isEmpty() ? 0 : (int) Math.ceil((double) safeSource.size() / safeSize);

        return PaginatedResponse.<T>builder()
                .content(content)
                .page(safePage)
                .size(safeSize)
                .totalElements(safeSource.size())
                .totalPages(totalPages)
                .numberOfElements(content.size())
                .first(safePage == 0)
                .last(totalPages == 0 || safePage >= totalPages - 1)
                .empty(content.isEmpty())
                .build();
    }
}
