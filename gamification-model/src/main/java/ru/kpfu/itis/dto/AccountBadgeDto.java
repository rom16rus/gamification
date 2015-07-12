package ru.kpfu.itis.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;

import java.util.Date;


/**
 * Created by Rigen on 12.07.15.
 */
@ApiModel("AccountBadge")
public class AccountBadgeDto {

    private Long id;

    @JsonProperty(value = "account_id")
    private Long accountId;

    @JsonProperty(value = "badge_id")
    private Long badgeId;

    private Double theory = 0.0;

    private Double practice = 0.0;

    private Double progress = 0.0;

    private Date date;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(Long badgeId) {
        this.badgeId = badgeId;
    }
}
