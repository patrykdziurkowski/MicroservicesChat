package com.patrykdziurkowski.microserviceschat.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.patrykdziurkowski.microserviceschat.application.ChatRepository;
import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.FavoriteChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.Message;
import com.patrykdziurkowski.microserviceschat.domain.UserMessage;
import com.patrykdziurkowski.microserviceschat.domain.domainevents.*;
import com.patrykdziurkowski.microserviceschat.domain.shared.DomainEvent;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class ChatRepositoryImpl implements ChatRepository{
    @PersistenceContext
    private EntityManager entityManager;

    public List<ChatRoom> get() {
        return entityManager
            .createQuery("SELECT c FROM ChatRoom c", ChatRoom.class)
            .getResultList();
    }

    public Optional<ChatRoom> getById(UUID chatId) {
        return Optional.ofNullable(entityManager.find(ChatRoom.class, chatId));
    }

    public List<ChatRoom> getByMemberId(UUID memberId, int lastChatPosition, int chatsToRetrieve) {
        final String query = "SELECT c FROM ChatRoom c JOIN c.memberIds m WHERE m = :memberId OR c.isPublic ";
        return entityManager
            .createQuery(query, ChatRoom.class)
            .setParameter("memberId", memberId)
            .setFirstResult(lastChatPosition)
            .setMaxResults(chatsToRetrieve)
            .getResultList();
    }

    public void save(ChatRoom chatRoom) {
        final boolean chatExists = chatExists(chatRoom.getId());
        if(chatExists) {
            entityManager.merge(chatRoom);
        } else {
            entityManager.persist(chatRoom);
        }
        final boolean chatDissolved = chatRoom
            .getDomainEvents()
            .contains(new ChatDissolvedEvent());
        if(chatDissolved) {
            removeChat(chatRoom);
        } else {
            addAnnouncements(chatRoom);
        }
        entityManager.flush();
    }

    private void removeChat(ChatRoom chatRoom) {
        List<Message> messagesInChat = getMessagesInChat(chatRoom.getId());
        if(messagesInChat.isEmpty() == false) {
            for(Message message : messagesInChat) {
                entityManager.remove(message);
            }
        }
        List<FavoriteChatRoom> favorited = getFavoriteInstances(chatRoom.getId());
        if(favorited.isEmpty() == false) {
            for(FavoriteChatRoom chat : favorited) {
                entityManager.remove(chat);
            }
        }
        entityManager.remove(chatRoom);
    }

    private void addAnnouncements(ChatRoom chatRoom) {
        if (chatRoom.getDomainEvents().isEmpty()) {
            return;
        }
        List<DomainEvent> domainEvents = chatRoom.getDomainEvents();
        for(DomainEvent event : domainEvents) {
            String announcement = getAnnouncementString(event);
            UserMessage message = new UserMessage(chatRoom.getId(), announcement, null);
            entityManager.persist(message);
        }
        chatRoom.clearDomainEvents();
    }

    private String getAnnouncementString(DomainEvent event) {
        if(event instanceof MemberInvitedEvent) {
            return ((MemberInvitedEvent)event).memberUsername() + " joined through invite!";
        }
        if(event instanceof MemberJoinedEvent) {
            return ((MemberJoinedEvent)event).memberUsername() + " joined!";
        }
        if(event instanceof MemberLeftEvent) {
            return ((MemberLeftEvent)event).memberUsername() + " left!";
        }
        if(event instanceof MemberRemovedEvent) {
            return ((MemberRemovedEvent)event).memberUsername() + " got removed!";
        }
        return "Error: ChatRepositoryImpl: unknown DomainEvent raised";
    }

    private List<Message> getMessagesInChat(UUID chatId) {
        final String query = "SELECT m FROM UserMessage m WHERE m.chatRoomId = :chatId";
        return entityManager.createQuery(query, Message.class)
            .setParameter("chatId", chatId)
            .getResultList();
    }

    private List<FavoriteChatRoom> getFavoriteInstances(UUID chatId) {
        final String query = "SELECT f FROM FavoriteChatRoom f WHERE f.chatRoomId = :chatId";
        return entityManager.createQuery(query, FavoriteChatRoom.class)
            .setParameter("chatId", chatId)
            .getResultList();
    }

    private boolean chatExists(UUID id) {
        final String query = "SELECT COUNT(c) FROM ChatRoom c WHERE c.id = :id";
        return entityManager
            .createQuery(query, Long.class)
            .setParameter("id", id)
            .getSingleResult() > 0;
    }
}
