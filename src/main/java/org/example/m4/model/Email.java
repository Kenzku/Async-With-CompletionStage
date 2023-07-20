package org.example.m4.model;

public class Email {
    private long id;

    public Email() {
    }

    public Email(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Email [id=" + id + "]";
    }
}
