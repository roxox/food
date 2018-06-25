package com.sebastianfox.food.entity.user;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sebastianfox.food.entity.user.User;

import javax.persistence.*;

@Entity // This tells Hibernate to make a table out of this class
@Table(name="favorites")

public class Favorite {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "favorite_id")
    private Integer favoriteId;

   //private User favUser = new User();

    @ManyToOne
    @JoinColumn(name="user_id")
    @JsonBackReference
    private User user;

    public Integer getId() {
        return favoriteId;
    }

    public void setId(Integer id) {
        this.favoriteId = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}