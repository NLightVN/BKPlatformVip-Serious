    package com.example.backend.dto.response;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import lombok.*;
    import lombok.experimental.FieldDefaults;

    import java.time.LocalDateTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class ApiResponse<T> {

        @Builder.Default
        int code = 1000;

        String message;
        T result;

        LocalDateTime timestamp;
        String path;
    }
