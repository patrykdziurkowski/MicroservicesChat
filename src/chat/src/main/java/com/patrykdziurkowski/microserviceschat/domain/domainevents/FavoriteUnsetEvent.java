package com.patrykdziurkowski.microserviceschat.domain.domainevents;

import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.shared.DomainEvent;

public record FavoriteUnsetEvent(UUID favoriteId) implements DomainEvent {

}
