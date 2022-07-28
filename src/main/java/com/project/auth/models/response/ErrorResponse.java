package com.project.auth.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.project.auth.exceptions.BuilderException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonPropertyOrder({"code", "message", "request_id", "details", "timestamp"})
@Getter
@NoArgsConstructor
public class ErrorResponse {


    private String code;

    private String message;

    @JsonProperty("request_id")
    private String requestId;

    @Setter(AccessLevel.NONE)
    private LocalDateTime timestamp;

    private List<ErrorDetail> details;

    public ErrorResponse(String code, String message, String requestId, List<ErrorDetail> details) {
        this.code = code;
        this.message = message;
        this.requestId = requestId;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public static class Builder {

        private final List<ErrorDetail> details = new ArrayList<>();

        private final String code;

        private final String message;

        private final String requestId;

        public Builder(String code, String message, String requestId) {

            this.code = code;
            this.message = message;
            this.requestId = requestId;
        }


        public Builder withErrorDetail(List<ErrorDetail> errorDetails) {
            if (errorDetails != null && !errorDetails.isEmpty()) {
                for (ErrorDetail ed : errorDetails) {
                    this.details.add(new ErrorDetail(ed.getProperty(), ed.getMessages()));
                }
            }
            return this;
        }

        public ErrorResponse build() {
            String errorMessage;
            if (!this.details.isEmpty() && this.details.stream()
                    .anyMatch(errorDetail ->
                            errorDetail.getProperty() == null || errorDetail.getProperty()
                                    .isEmpty() || errorDetail.getMessages() == null || errorDetail
                                    .getMessages()
                                    .isEmpty()
                    )) {
                errorMessage = "Can't create " + this.getClass()
                        + ": Some property or messages are missing";
                throw new BuilderException(errorMessage);
            } else {
                return new ErrorResponse(this.code, this.message, this.requestId, this.details);
            }
        }
    }
}
