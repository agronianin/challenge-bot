package com.challengebot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "programs")
public class Program {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Column(name = "days_total", nullable = false)
    private Integer daysTotal;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "post_time", nullable = false, length = 5)
    private String postTime;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone;

    @Column(name = "status", nullable = false, length = 32)
    private String status = "active";

    @Column(name = "exercises_per_day", nullable = false)
    private Integer exercisesPerDay = 3;

    @Column(name = "groups_per_day", nullable = false)
    private Integer groupsPerDay = 2;

    @OneToMany(mappedBy = "program")
    private List<ProgramDay> days = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public Integer getDaysTotal() {
        return daysTotal;
    }

    public void setDaysTotal(Integer daysTotal) {
        this.daysTotal = daysTotal;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getExercisesPerDay() {
        return exercisesPerDay;
    }

    public void setExercisesPerDay(Integer exercisesPerDay) {
        this.exercisesPerDay = exercisesPerDay;
    }

    public Integer getGroupsPerDay() {
        return groupsPerDay;
    }

    public void setGroupsPerDay(Integer groupsPerDay) {
        this.groupsPerDay = groupsPerDay;
    }

    public List<ProgramDay> getDays() {
        return days;
    }

    public void setDays(List<ProgramDay> days) {
        this.days = days;
    }
}
