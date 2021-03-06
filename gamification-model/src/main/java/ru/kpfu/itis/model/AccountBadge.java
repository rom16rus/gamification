package ru.kpfu.itis.model;


import org.hibernate.annotations.CreationTimestamp;
import ru.kpfu.itis.model.enums.BadgeAchievementStatus;

import javax.persistence.*;
import java.util.Date;


/**
 * Created by Rigen on 25.06.15.
 */
@Entity
@Table(name = "ACCOUNT_BADGE")
@AttributeOverride(name = "id", column = @Column(name = "ACCOUNT_BADGE_ID"))
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class AccountBadge extends BaseLongIdEntity {

    @ManyToOne
    @JoinColumn(name = "ACCOUNT_ID", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "BADGE_ID", nullable = false)
    private Badge badge;

    @Column(name = "THEORY")
    private Double theory = 0.0;

    @Column(name = "PRACTICE")
    private Double practice = 0.0;

    @Column(name = "PROGRESS")
    private Double progress = 0.0;

    @Column(name = "ACHIEVEMENT_STATUS", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private BadgeAchievementStatus achevementStatus = BadgeAchievementStatus.PERFORM;

    public void computeProgress() {
        progress = theory + practice;
        checkAchievement();
    }

    private void checkAchievement() {
        if (progress.equals(badge.getMaxMark() * 1.0)) {
            achevementStatus = BadgeAchievementStatus.COMPLETE;
        }
    }

    public BadgeAchievementStatus getAchevementStatus() {
        return achevementStatus;
    }

    public void setAchevementStatus(BadgeAchievementStatus achevementStatus) {
        this.achevementStatus = achevementStatus;
    }

    public Double getTheory() {
        return theory;
    }

    public void setTheory(Double theory) {
        this.theory = theory;
    }

    public Double getPractice() {
        return practice;
    }

    public void setPractice(Double practice) {
        this.practice = practice;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    @Override
    @CreationTimestamp
    public Date getCreateTime() {
        return super.getCreateTime();
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Badge getBadge() {
        return badge;
    }

    public void setBadge(Badge badge) {
        this.badge = badge;
    }
}
