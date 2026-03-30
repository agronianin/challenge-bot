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
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "exercises",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_exercises_name_group", columnNames = {"name", "group_id"})
        }
)
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ExerciseGroup group;

    @Column(name = "base_reps", nullable = false)
    private Integer baseReps;

    @Column(name = "comment")
    private String comment;

    @Column(name = "video_path", length = 1024)
    private String videoPath;

    @Column(name = "file_id", length = 512)
    private String fileId;

    @OneToMany(mappedBy = "exercise")
    private List<DayExercise> dayLinks = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExerciseGroup getGroup() {
        return group;
    }

    public void setGroup(ExerciseGroup group) {
        this.group = group;
    }

    public Integer getBaseReps() {
        return baseReps;
    }

    public void setBaseReps(Integer baseReps) {
        this.baseReps = baseReps;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public List<DayExercise> getDayLinks() {
        return dayLinks;
    }

    public void setDayLinks(List<DayExercise> dayLinks) {
        this.dayLinks = dayLinks;
    }
}
