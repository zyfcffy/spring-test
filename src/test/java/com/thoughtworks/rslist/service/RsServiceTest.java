package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.*;
import com.thoughtworks.rslist.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class RsServiceTest {
    RsService rsService;

    @Mock
    RsEventRepository rsEventRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    VoteRepository voteRepository;
    @Mock
    TradeRecordRepository tradeRecordRepository;
    @Mock
    RankRepository rankRepository;
    LocalDateTime localDateTime;
    Vote vote;

    @BeforeEach
    void setUp() {
        initMocks(this);
        rsService = new RsService(rsEventRepository, userRepository, voteRepository, tradeRecordRepository, rankRepository);
        localDateTime = LocalDateTime.now();
        vote = Vote.builder().voteNum(2).rsEventId(1).time(localDateTime).userId(1).build();
    }

    @Test
    void shouldVoteSuccess() {
        // given

        UserDto userDto =
                UserDto.builder()
                        .voteNum(5)
                        .phone("18888888888")
                        .gender("female")
                        .email("a@b.com")
                        .age(19)
                        .userName("xiaoli")
                        .id(2)
                        .build();
        RsEventDto rsEventDto =
                RsEventDto.builder()
                        .eventName("event name")
                        .id(1)
                        .keyword("keyword")
                        .voteNum(2)
                        .user(userDto)
                        .build();

        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));
        // when
        rsService.vote(vote, 1);
        // then
        verify(voteRepository)
                .save(
                        VoteDto.builder()
                                .num(2)
                                .localDateTime(localDateTime)
                                .user(userDto)
                                .rsEvent(rsEventDto)
                                .build());
        verify(userRepository).save(userDto);
        verify(rsEventRepository).save(rsEventDto);
    }

    @Test
    void shouldThrowExceptionWhenUserNotExist() {
        // given
        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
        //when&then
        assertThrows(
                RuntimeException.class,
                () -> {
                    rsService.vote(vote, 1);
                });
    }

    @Test
    void shouldThrowExceptionWhenRsEventNotExist() {
        Trade trade = new Trade(2, 1);
        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(
                Exception.class,
                () -> {
                    rsService.buy(trade, 5);
                });
    }

    @Test
    void BuyRankIfRankIsNotTraded() throws Exception {
        UserDto userDto =
                UserDto.builder().voteNum(5).phone("18888888888").gender("female").email("a@b.com")
                        .age(19).userName("xiaoli").id(2).build();
        RsEventDto rsEventDto =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(userDto).build();
        Trade trade = new Trade(2, 1);
        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
        when(rankRepository.findByRankPoint(anyInt())).thenReturn(Optional.empty());
        rsService.buy(trade,rsEventDto.getId());
        verify(rankRepository).save(any());
        verify(tradeRecordRepository).save(TradeRecordDto.builder()
                .amount(trade.getAmount())
                .rankPoint(trade.getRank())
                .rsEventId(rsEventDto.getId()).build());
        rsEventDto.setRank(trade.getRank());
        rsEventDto.setIsTraded(1);
        verify(rsEventRepository).save(rsEventDto);
    }

    @Test
    void shouldBuyRankSuccessWhenTradeAmountIsEnough() throws Exception {
        RsEventDto rsEventDto =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").build();
        Trade trade = new Trade(3, 1);
        int rsEventId = 6;
        RankDto rankDto = RankDto.builder().rankPoint(1).amount(1).rsEventId(1).build();
        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
        when(rankRepository.findByRankPoint(anyInt())).thenReturn(Optional.of(rankDto));
        rsService.buy(trade,rsEventId);
        TradeRecordDto tradeRecordDto = TradeRecordDto.builder()
                .amount(trade.getAmount())
                .rankPoint(trade.getRank())
                .rsEventId(rsEventId).build();
        verify(tradeRecordRepository).save(tradeRecordDto);
        verify(rankRepository).save(rankDto);
        verify(rsEventRepository).save(rsEventDto);
    }
}
