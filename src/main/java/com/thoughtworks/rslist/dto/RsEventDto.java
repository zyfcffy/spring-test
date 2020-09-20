package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rs_event")
public class RsEventDto {
  @Id @GeneratedValue private int id;
  private String eventName;
  private String keyword;
  private int voteNum;
  private Boolean isTraded;
  @ManyToOne
  @JoinColumn(name = "user_id")
  private UserDto user;
}
