package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.patrykdziurkowski.microserviceschat.application.SetFavoriteCommand;
import com.patrykdziurkowski.microserviceschat.application.UnsetFavoriteCommand;

@RestController
public class FavoriteChatController {

    private final SetFavoriteCommand setFavoriteCommand;
    private final UnsetFavoriteCommand unsetFavoriteCommand;

    public FavoriteChatController(SetFavoriteCommand setFavoriteCommand, UnsetFavoriteCommand unsetFavoriteCommand) {
        this.setFavoriteCommand = setFavoriteCommand;
        this.unsetFavoriteCommand = unsetFavoriteCommand;
    }

    @PostMapping("/favorites")
    public ResponseEntity<String> addFavorite(@RequestParam UUID currentUserId, @RequestParam UUID chatId) {
        boolean isSet = setFavoriteCommand.execute(currentUserId, chatId);
        if(isSet == false) {
            return new ResponseEntity<>("Failed to add chat to favorites.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Chat added to favorites successfully.", HttpStatus.CREATED);
    }

    @DeleteMapping("/favorites")
    public ResponseEntity<String> removeFavorite(@RequestParam UUID currentUserId, @RequestParam UUID chatId) {
        boolean isUnset = unsetFavoriteCommand.execute(currentUserId, chatId);
        if(isUnset == false) {
            return new ResponseEntity<>("Failed to remove chat from favorites.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Chat removed from favorites successfully.", HttpStatus.NO_CONTENT);
    }
}
