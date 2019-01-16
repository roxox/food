package com.sebastianfox.food.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sebastianfox.food.entity.event.Event;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
@Entity // This tells Hibernate to make a table out of this class
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="user_id")
    private Integer id;

    private String username;

    private String email;

    private String session;

    private String firstname;

    private String lastname;

    @JsonIgnore
    private byte[] password;

    @JsonIgnore
    private byte[] salt;

    private String facebookMail;

    private String facebookUsername;

    private boolean socialMediaAccount = false;

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            })
    @JoinTable(name="users_events",
            joinColumns=@JoinColumn(name="userId"),
            inverseJoinColumns=@JoinColumn(name="eventId")
    )
    @JsonIgnoreProperties({"owner", "members", "memberRequests"})
    private List<Event> events;

    @OneToMany(mappedBy="owner")
    @JsonIgnoreProperties({"owner", "members", "memberRequests"})
    private List<Event> ownedEvents;

    @ManyToMany
    @JoinTable(name="favorites",
            joinColumns=@JoinColumn(name="friendId"),
            inverseJoinColumns=@JoinColumn(name="favoriteId")

    )
    private List<User> friends;


    @ManyToMany
   // @JoinTable(name="favorites",
     //       joinColumns=@JoinColumn(name="favoriteId"),
       //     inverseJoinColumns=@JoinColumn(name="friendId")
   // )
    private List<User> friendOf;


/**
    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            },
            mappedBy = "events")
    @JsonIgnoreProperties({"owner", "members", "memberRequests", "friends", "friendOf"})
    private Set<User> friendOf = new HashSet<>();
*/


    @OneToMany(mappedBy="user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<UserImage> userImages;

    //  Constructor

    public User(){
        this.userImages = new ArrayList<>();
        //this.friendOf = new ArrayList<>();
        this.friends = new ArrayList<>();
        this.events = new ArrayList<>();
    }

    //  functions

    /**
     *
     *
     * @param appUser data transformed as user from app
     * @return user with new data with data from app
     */
    public User mergeDataFromApp(User appUser) {
        this.username = appUser.username;
        this.email = appUser.email;
        this.lastname = appUser.lastname;
        this.firstname = appUser.firstname;
        return this;
    }

    //  getter and setter

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonIgnore
    public byte[] getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(byte[] password) {
        this.password = password;
    }

    @JsonIgnore
    public byte[] getSalt() {
        return salt;
    }

    @JsonProperty
    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getFacebookMail() {
        return facebookMail;
    }

    public void setFacebookMail(String facebookMail) {
        this.facebookMail = facebookMail;
    }

    public String getFacebookUsername() {
        return facebookUsername;
    }

    public void setFacebookUsername(String facebookUsername) {
        this.facebookUsername = facebookUsername;
    }

    public List<UserImage> getUserImages() {
        return userImages;
    }

    public void setUserImages(List<UserImage> userImages) {
        this.userImages = userImages;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void addUserImage(UserImage userImage){
        this.userImages.add(userImage);
        userImage.setUser(this);
    }

    public boolean isSocialMediaAccount() {
        return socialMediaAccount;
    }

    public void setSocialMediaAccount(boolean socialMediaAccount) {
        this.socialMediaAccount = socialMediaAccount;
    }

   /* public void addFriendOf(User user){
        user.friendOf.add(this);
        this.addFriend(user);
    }*/

    public void addFriend(User friend){
        this.friends.add(friend);
        if (!friend.friends.contains(this)){
            friend.addFriend(this);
        }
       // friend.addFriendOf(this);
        //userImage.setUser(this);
    }

    public List<User> getFriends() {
        return friends;
    }

    public void addEvent(Event event){
        this.events.add(event);
        if (!event.getMembers().contains(this)){
            event.addMember(this);
        }
    }

    public void removeEvent(Event event){
        this.events.remove(event);
        if (event.getMembers().contains(this)) {
            event.removeMember(this);
        }
    }

    public void removeOwnedEventFromList(Event event) {
        this.events.remove(event);
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<Event> getOwnedEvents() {
        return ownedEvents;
    }

    public void setOwnedEvents(List<Event> ownedEvents) {
        this.ownedEvents = ownedEvents;
    }

    public void addOwnedEvent(Event ownedEvent) {
        this.ownedEvents.add(ownedEvent);
    }
}



