package ru.kpfu.itis.model;


import ru.kpfu.itis.model.enums.BadgeCategory;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Rigen on 20.06.15.
 */

@Entity
@Table(name = "BADGE")
@AttributeOverride(name = "id", column = @Column(name = "BADGE_ID"))
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Badge extends BaseLongIdEntity {

    @Enumerated(EnumType.STRING)
    private BadgeCategory type;

    private String description;

    private String name;

    private String image;

    @OneToMany
    private List<Task> tasks;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BadgeCategory getType() {
        return type;
    }

    public void setType(BadgeCategory type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}