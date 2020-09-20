package com.thoughtworks.rslist.dto;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rank")
public class RankDto {
    @Id
    @GeneratedValue
    private Integer id;
    private  int rankPoint;
    private int amount;
    private int rsEventId;
}
