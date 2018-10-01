package com.btp.me.classroom.Class;

public class ClassroomJava {
    public String name;
    public String status;
    public String profileImage;

    public ClassroomJava() {
        this.name = "default";
        this.status = "default";
        this.profileImage = "default";
    }

    public ClassroomJava(String name, String status, String profileImage) {
        this.name = name;
        this.status = status;
        this.profileImage = profileImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
