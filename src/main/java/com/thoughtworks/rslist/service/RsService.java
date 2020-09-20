package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.*;
import com.thoughtworks.rslist.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RsService {
  final RsEventRepository rsEventRepository;
  final UserRepository userRepository;
  final VoteRepository voteRepository;
  final TradeRecordRepository tradeRecordRepository;
  final RankRepository rankRepository;

  public RsService(RsEventRepository rsEventRepository,
                   UserRepository userRepository,
                   VoteRepository voteRepository,
                   TradeRecordRepository tradeRecordRepository,
                   RankRepository rankRepository) {
    this.rsEventRepository = rsEventRepository;
    this.userRepository = userRepository;
    this.voteRepository = voteRepository;
    this.tradeRecordRepository = tradeRecordRepository;
    this.rankRepository = rankRepository;
  }

  public void vote(Vote vote, int rsEventId) {
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
    Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
    if (!rsEventDto.isPresent()
        || !userDto.isPresent()
        || vote.getVoteNum() > userDto.get().getVoteNum()) {
      throw new RuntimeException();
    }
    VoteDto voteDto =
        VoteDto.builder()
            .localDateTime(vote.getTime())
            .num(vote.getVoteNum())
            .rsEvent(rsEventDto.get())
            .user(userDto.get())
            .build();
    voteRepository.save(voteDto);
    UserDto user = userDto.get();
    user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
    userRepository.save(user);
    RsEventDto rsEvent = rsEventDto.get();
    rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
    rsEventRepository.save(rsEvent);
  }

  @Transactional
  public void buy(Trade trade, int rsEventId) {
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
    int rankPoint = trade.getRank();
    Optional<RankDto> rankDto = rankRepository.findByRankPoint(rankPoint);
    if(!rankDto.isPresent()){
      RankDto newRankDto =RankDto.builder()
              .amount(trade.getAmount())
              .rankPoint(rankPoint)
              .rsEventId(rsEventId).build();
      rankRepository.save(newRankDto);
      TradeRecordDto tradeRecordDto =TradeRecordDto.builder()
              .amount(trade.getAmount())
              .rankPoint(rankPoint)
              .rsEventId(rsEventId).build();
      tradeRecordRepository.save(tradeRecordDto);
      RsEventDto rsEvent = rsEventDto.get();
      rsEvent.setIsTraded(true);
      rsEventRepository.save(rsEvent);
    }
  }
}
