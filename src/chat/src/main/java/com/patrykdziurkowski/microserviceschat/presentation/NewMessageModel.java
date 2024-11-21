package com.patrykdziurkowski.microserviceschat.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NewMessageModel {

    @NotBlank
    @Size(max = 500)            // maximum size of message
    private String content;

    public NewMessageModel() {}

    public NewMessageModel(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
