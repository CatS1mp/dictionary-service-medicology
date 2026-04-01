package com.medicology.dictionary.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Cái nào null thì không hiện trong JSON
public class ApiResponse<T> {
    private int code;           // Mã lỗi nội bộ (ví dụ: 1001, 1002)
    private String message;     // Thông báo cho người dùng
    private T data;           // Dữ liệu thực tế (User, List, v.v.)
}
