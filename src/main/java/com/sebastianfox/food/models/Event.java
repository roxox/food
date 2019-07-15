package com.sebastianfox.food.models;

//import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sebastianfox.food.enums.EventTypes;
import com.sebastianfox.food.enums.PrivacyTypes;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "WeakerAccess"})
@Entity
@Table(name="events")
public class Event {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)", name = "event_id")
    private UUID id;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "open", nullable = false)
    private boolean open = true;

    private String text;

//    @OneToOne(cascade = CascadeType.ALL)
    @ManyToOne(fetch=FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            })
    @JoinColumn(name = "location_id", referencedColumnName = "location_id")
    @JsonBackReference
    @JsonIgnoreProperties({"event"})
    private Location location;

    private Date date;

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy_type")
    private PrivacyTypes privacyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventTypes eventType;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "owner_id", nullable=false)
    @JsonIgnoreProperties({"userName", "email", "name", "description", "facebookAccountId", "facebookAccountEmail", "facebookAccountUserName", "facebookAccount", "events", "ownedEvents", "interestedEvents", "friendshipsFriend1", "friendshipsFriend2", "acceptedFriends", "requestedFriendsByFriend", "requestedFriendsByCurrentUser"})
    private User owner;

    private String description;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            },
            mappedBy = "events")
    @JsonIgnoreProperties({"userName", "email", "name", "description", "facebookAccountId", "facebookAccountEmail", "facebookAccountUserName", "facebookAccount", "events", "ownedEvents", "interestedEvents", "friendshipsFriend1", "friendshipsFriend2", "acceptedFriends", "requestedFriendsByFriend", "requestedFriendsByCurrentUser"})
    private List<User> members;

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            },
            mappedBy = "interestedEvents")
    @JsonIgnoreProperties({"userName", "email", "name", "description", "facebookAccountId", "facebookAccountEmail", "facebookAccountUserName", "facebookAccount", "events", "ownedEvents", "interestedEvents", "friendshipsFriend1", "friendshipsFriend2", "acceptedFriends", "requestedFriendsByFriend", "requestedFriendsByCurrentUser"})
    private List<User> interesteds;

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            },
            mappedBy = "events")
    @JsonIgnoreProperties({"events"})
    @JsonIgnore
    private Set<Tag> tags = new HashSet<>();

    @JsonIgnore
    private Date updated;

    @JsonIgnore
    private Date created;

    /**
     * Constructor
     */
    public Event() {
        this.members = new ArrayList<>();
        this.interesteds = new ArrayList<>();
        eventType = EventTypes.OTHERS;
        privacyType = PrivacyTypes.PUBLIC;
    }

    //   Getter and Setter

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
        if (!location.getEvents().contains(this)){
            location.addEvent(this);
        }
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

//    @JsonIgnore
    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
        if (!owner.getOwnedEvents().contains(this)){
            owner.addOwnedEvent(this);
        }
        if (!owner.getEvents().contains(this)){
            owner.addEvent(this);
        }
    }

    // Members

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public void addMember(User member) {
            this.members.add(member);
            if (!member.getEvents().contains(this)) {
                member.addEvent(this);
            }
    }

    public void removeMember(User member) {
        this.members.remove(member);
        if (member.getEvents().contains(this)) {
            member.removeEvent(this);
        }
    }

    // Interested
    public List<User> getInteresteds() {
        return interesteds;
    }

    public void setInteresteds(List<User> interesteds) {
        this.interesteds = interesteds;
    }

    public void addInterested(User interested) {
        this.interesteds.add(interested);
        if (!interested.getInterestedEvents().contains(this)) {
            interested.addInterestedEvent(this);
        }
    }

    public void removeInterested(User interested) {
        this.interesteds.remove(interested);
        if (interested.getInterestedEvents().contains(this)) {
            interested.removeInterestedEvent(this);
        }
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
        if (!tag.getEvents().contains(this)) {
            tag.addEvent(this);
        }
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        if (tag.getEvents().contains(this)) {
            tag.removeEvent(this);
        }
    }

    public void removeFromOwnersList() {
        this.owner.removeOwnedEventFromList(this);
    }

    public EventTypes getEventType() {
        return eventType;
    }

    public void setEventType(EventTypes eventType) {
        this.eventType = eventType;
    }

    public PrivacyTypes getPrivacyType() {
        return privacyType;
    }

    public void setPrivacyTypes(PrivacyTypes privacyType) {
        this.privacyType = privacyType;
    }

    public boolean hasAvailableSpaces() {
        return this.getMembers().size() < this.getMaxParticipants();
    }

    @PreUpdate
    public void preUpdate() {
        updated = new Date();
    }

    @PrePersist
    public void prePersist() {
        updated = new Date();
        created = new Date();
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setPrivacyType(PrivacyTypes privacyType) {
        this.privacyType = privacyType;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * @param otherEvent data transformed as user from app
     */
    public void mergeDataFromOtherInstance(Event otherEvent) {
        this.text = otherEvent.getText();
        this.description = otherEvent.getDescription();
        this.eventType = otherEvent.getEventType();
        this.privacyType = otherEvent.getPrivacyType();
        this.maxParticipants = otherEvent.getMaxParticipants();
    }

    public void changeOwner(User newOwner){
        this.getOwner().getOwnedEvents().remove(this);
        this.setOwner(newOwner);
    }

}
