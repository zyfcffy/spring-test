package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "trade_record")
public class TradeRecordDto {
    @Id
    @GeneratedValue
    private Integer id;
    private Integer rankPoint;
    private Integer amount;
    private Integer rsEventId;
}
