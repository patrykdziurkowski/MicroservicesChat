package com.patrykdziurkowski.microserviceschat.domain.shared;

import java.util.ArrayList;
import java.util.List;

public class AggreggateRoot {
    private List<DomainEvent> domainEvents = new ArrayList<>();

    public List<DomainEvent> getDomainEvents() {
        return domainEvents;
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public void raiseDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }
}
