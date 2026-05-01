package com.pos.utility;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class JsonResponse<T> {

    private final String message;
    private final T data;

    private JsonResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    private JsonResponse(String message) {
        this(message, null);
    }

    // ── Static factory methods ──────────────────────────────────────────

    public static <T> ResponseEntity<JsonResponse<T>> ok(T data) {
        return ResponseEntity.ok(new JsonResponse<>("Success", data));
    }

    public static <T> ResponseEntity<JsonResponse<T>> success(HttpStatus status, T data) {
        return ResponseEntity.status(status).body(new JsonResponse<>("Success", data));
    }

    public static <T> ResponseEntity<JsonResponse<T>> success(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status).body(new JsonResponse<>(message, data));
    }

    public static <T> ResponseEntity<JsonResponse<T>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new JsonResponse<>(message));
    }

    public static <T> ResponseEntity<JsonResponse<T>> error(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new JsonResponse<>(message));
    }

    public static <T> ResponseEntity<JsonResponse<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new JsonResponse<>(message));
    }

    public static <T> ResponseEntity<JsonResponse<T>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new JsonResponse<>(message));
    }
}