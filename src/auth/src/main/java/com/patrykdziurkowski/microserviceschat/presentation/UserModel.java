package com.patrykdziurkowski.microserviceschat.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserModel {
    @NotBlank
    @Size(min = 3, max = 15)
    @Pattern(regexp = "^\\w+$") // alphanumeric only
    private String userName;
    @NotBlank
    @Size(min = 8)
    @Pattern(regexp = ".*\\d.*") // needs atleast one digit
    @Pattern(regexp = ".*\\W.*") // needs atleast one non-alphanumeric character
    private String password;

    public UserModel(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
