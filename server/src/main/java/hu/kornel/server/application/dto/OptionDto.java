package hu.kornel.server.application.dto;

import lombok.Data;

@Data
public class OptionDto {
    private String id;
    private String text;
    private boolean correct;

     public String getId() {
        return id;
    }

    public boolean isCorrect() {
        return correct;
    }
}
