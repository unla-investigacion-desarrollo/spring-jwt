package com.project.auth.models.response;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class ErrorDetail {

    private String property;

    private List<String> messages;

    public ErrorDetail(String property, List<String> messages) {
        this.property = property;
        this.messages = messages;
    }
}
