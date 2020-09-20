package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.TradeRecordDto;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TradeRecordRepository extends CrudRepository<TradeRecordDto,Integer> {
    List<TradeRecordDto> findAll();
}
