package com.challengebot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "challenge.bot")
public class BotProperties {
    private String token;
    private String username;
    private String timezone;
    private String postTime;
    private int exercisesPerDay;
    private int groupsPerDay;
    private double repsGrowthPercent;
    private String repsRoundMode;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean hasConfiguredToken() {
        return token != null
                && !token.isBlank()
                && !"replace_me".equalsIgnoreCase(token.trim());
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public int getExercisesPerDay() {
        return exercisesPerDay;
    }

    public void setExercisesPerDay(int exercisesPerDay) {
        this.exercisesPerDay = exercisesPerDay;
    }

    public int getGroupsPerDay() {
        return groupsPerDay;
    }

    public void setGroupsPerDay(int groupsPerDay) {
        this.groupsPerDay = groupsPerDay;
    }

    public double getRepsGrowthPercent() {
        return repsGrowthPercent;
    }

    public void setRepsGrowthPercent(double repsGrowthPercent) {
        this.repsGrowthPercent = repsGrowthPercent;
    }

    public String getRepsRoundMode() {
        return repsRoundMode;
    }

    public void setRepsRoundMode(String repsRoundMode) {
        this.repsRoundMode = repsRoundMode;
    }
}
