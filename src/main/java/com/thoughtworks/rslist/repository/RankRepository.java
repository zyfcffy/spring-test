package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.RankDto;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RankRepository extends CrudRepository<RankDto,Integer> {
    List<RankDto> findAll();
    Optional<RankDto> findByRankPoint(int rankPoint);
}
