package com.patrykdziurkowski.microserviceschat.domain.domainevents;

import com.patrykdziurkowski.microserviceschat.domain.shared.DomainEvent;

public record MemberRemovedEvent(String memberUsername) implements DomainEvent{

}
