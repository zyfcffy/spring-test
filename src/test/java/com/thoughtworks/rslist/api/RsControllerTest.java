package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.dto.RankDto;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RsControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RsEventRepository rsEventRepository;
    @Autowired
    VoteRepository voteRepository;
    @Autowired
    TradeRecordRepository tradeRecordRepository;
    @Autowired
    RankRepository rankRepository;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        voteRepository.deleteAll();
        rsEventRepository.deleteAll();
        userRepository.deleteAll();
        userDto =
                UserDto.builder()
                        .voteNum(10)
                        .phone("188888888888")
                        .gender("female")
                        .email("a@b.com")
                        .age(19)
                        .userName("idolice")
                        .build();
    }

    @Test
    public void shouldBuyRankIfRankIsNotTraded() throws Exception {
        UserDto save = userRepository.save(userDto);
        RsEventDto rsEventDto =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
        rsEventRepository.save(rsEventDto);
        Trade trade = new Trade(2, 1);
        mockMvc.perform(post("/rs/buy/{rsEventId}", rsEventDto.getId())
                .content(new ObjectMapper().writeValueAsString(trade))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        int tradeRecordSize = tradeRecordRepository.findAll().size();
        RsEventDto rsEvent = rsEventRepository.findById(rsEventDto.getId()).get();
        assertEquals(1, tradeRecordSize);
        assertEquals(1, rsEvent.getIsTraded());
    }

    @Test
    public void shouldReturnBadRequestWhenRsEventNotExist() throws Exception {
        Trade trade = new Trade(2, 1);
        mockMvc.perform(post("/rs/buy/5")
                .content(new ObjectMapper().writeValueAsString(trade))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldBuyRankSuccessWhenTradeAmountIsEnough() throws Exception {
        UserDto save = userRepository.save(userDto);
        RsEventDto rsEventDto1 =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
        RsEventDto rsEventDto2 =
                RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
        RsEventDto rsEventDto3 =
                RsEventDto.builder().keyword("无分类").eventName("第三条事件").user(save).build();
        rsEventRepository.save(rsEventDto1);
        rsEventRepository.save(rsEventDto2);
        rsEventRepository.save(rsEventDto3);
        RankDto rankDto = RankDto.builder().rankPoint(1).amount(2).rsEventId(rsEventDto1.getId()).build();
        rankRepository.save(rankDto);
        Trade trade = new Trade(3, 1);
        mockMvc.perform(post("/rs/buy/{rsEventId}", rsEventDto2.getId())
                .content(new ObjectMapper().writeValueAsString(trade))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        int rsEventSize = rsEventRepository.findAll().size();
        int tradeRecordSize = tradeRecordRepository.findAll().size();
        int amount = rankRepository.findByRankPoint(1).get().getAmount();
        Optional<RsEventDto> rsEvent1 = rsEventRepository.findById(rsEventDto1.getId());
        assertEquals(2, rsEventSize);
        assertEquals(1, tradeRecordSize);
        assertEquals(3, amount);
        assertFalse(rsEvent1.isPresent());
    }

    @Test
    void shouldReturnBadRequestWhenTradeAmountIsNotEnough() throws Exception {
        UserDto save = userRepository.save(userDto);
        RsEventDto rsEventDto1 =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
        RsEventDto rsEventDto2 =
                RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
        RsEventDto rsEventDto3 =
                RsEventDto.builder().keyword("无分类").eventName("第三条事件").user(save).build();
        rsEventRepository.save(rsEventDto1);
        rsEventRepository.save(rsEventDto2);
        rsEventRepository.save(rsEventDto3);
        RankDto rankDto = RankDto.builder().rankPoint(1).amount(2).rsEventId(rsEventDto1.getId()).build();
        rankRepository.save(rankDto);
        Trade trade = new Trade(1, 1);
        mockMvc.perform(post("/rs/buy/{rsEventId}", rsEventDto2.getId())
                .content(new ObjectMapper().writeValueAsString(trade))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldGetRsEventListByVotesWhenAllEventAreNotTraded() throws Exception {
        UserDto save = userRepository.save(userDto);
        RsEventDto rsEventDto1 =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).voteNum(1).build();
        RsEventDto rsEventDto2 =
                RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).voteNum(2).build();
        RsEventDto rsEventDto3 =
                RsEventDto.builder().keyword("无分类").eventName("第三条事件").user(save).voteNum(3).build();
        rsEventRepository.save(rsEventDto1);
        rsEventRepository.save(rsEventDto2);
        rsEventRepository.save(rsEventDto3);
        mockMvc.perform(get("/rs/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventName", is("第三条事件")))
                .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
                .andExpect(jsonPath("$[2].eventName", is("第一条事件")));
    }

    @Test
    public void shouldGetRsEventByVotesAndTradedRank() throws Exception {
        UserDto save = userRepository.save(userDto);
        RsEventDto rsEventDto1 =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).voteNum(3).build();
        RsEventDto rsEventDto2 =
                RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).voteNum(2).build();
        RsEventDto rsEventDto3 =
                RsEventDto.builder().keyword("无分类").eventName("第三条事件").user(save).voteNum(4).build();
        RsEventDto rsEventDto4 =
                RsEventDto.builder().keyword("无分类").eventName("第四条事件").user(save).voteNum(1).isTraded(1).rank(1).build();
        rsEventRepository.save(rsEventDto1);
        rsEventRepository.save(rsEventDto2);
        rsEventRepository.save(rsEventDto3);
        rsEventRepository.save(rsEventDto4);
        RankDto rankDto = RankDto.builder().rankPoint(1).amount(2).rsEventId(rsEventDto4.getId()).build();
        rankRepository.save(rankDto);
        mockMvc.perform(get("/rs/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventName", is("第四条事件")))
                .andExpect(jsonPath("$[1].eventName", is("第三条事件")))
                .andExpect(jsonPath("$[2].eventName", is("第一条事件")))
                .andExpect(jsonPath("$[3].eventName", is("第二条事件")));
    }

    @Test
    public void shouldGetOneEvent() throws Exception {
        UserDto save = userRepository.save(userDto);
        RsEventDto rsEventDto =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).voteNum(1).build();
        rsEventRepository.save(rsEventDto);
        rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).voteNum(1).build();
        rsEventRepository.save(rsEventDto);
        mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.eventName", is("第一条事件")));
        mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.keyword", is("无分类")));
        mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.eventName", is("第二条事件")));
        mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.keyword", is("无分类")));
    }

    @Test
    public void shouldGetErrorWhenIndexInvalid() throws Exception {
        mockMvc
                .perform(get("/rs/4"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("invalid index")));
    }

    @Test
    public void shouldGetRsListBetween() throws Exception {
        UserDto save = userRepository.save(userDto);

        RsEventDto rsEventDto =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).voteNum(3).build();

        rsEventRepository.save(rsEventDto);
        rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).voteNum(2).build();
        rsEventRepository.save(rsEventDto);
        rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第三条事件").user(save).voteNum(1).build();
        rsEventRepository.save(rsEventDto);
        mockMvc
                .perform(get("/rs/list?start=1&end=2"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
                .andExpect(jsonPath("$[0].keyword", is("无分类")))
                .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
                .andExpect(jsonPath("$[1].keyword", is("无分类")));
        mockMvc
                .perform(get("/rs/list?start=2&end=3"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].eventName", is("第二条事件")))
                .andExpect(jsonPath("$[0].keyword", is("无分类")))
                .andExpect(jsonPath("$[1].eventName", is("第三条事件")))
                .andExpect(jsonPath("$[1].keyword", is("无分类")));
        mockMvc
                .perform(get("/rs/list?start=1&end=3"))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].keyword", is("无分类")))
                .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
                .andExpect(jsonPath("$[1].keyword", is("无分类")))
                .andExpect(jsonPath("$[2].eventName", is("第三条事件")))
                .andExpect(jsonPath("$[2].keyword", is("无分类")));
    }

    @Test
    public void shouldAddRsEventWhenUserExist() throws Exception {

        UserDto save = userRepository.save(userDto);

        String jsonValue =
                "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": " + save.getId() + "}";

        mockMvc
                .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        List<RsEventDto> all = rsEventRepository.findAll();
        assertNotNull(all);
        assertEquals(all.size(), 1);
        assertEquals(all.get(0).getEventName(), "猪肉涨价了");
        assertEquals(all.get(0).getKeyword(), "经济");
        assertEquals(all.get(0).getUser().getUserName(), save.getUserName());
        assertEquals(all.get(0).getUser().getAge(), save.getAge());
    }

    @Test
    public void shouldAddRsEventWhenUserNotExist() throws Exception {
        String jsonValue = "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": 100}";
        mockMvc
                .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldVoteSuccess() throws Exception {
        UserDto save = userRepository.save(userDto);
        RsEventDto rsEventDto =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
        rsEventDto = rsEventRepository.save(rsEventDto);

        String jsonValue =
                String.format(
                        "{\"userId\":%d,\"time\":\"%s\",\"voteNum\":1}",
                        save.getId(), LocalDateTime.now().toString());
        mockMvc
                .perform(
                        post("/rs/vote/{id}", rsEventDto.getId())
                                .content(jsonValue)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        UserDto userDto = userRepository.findById(save.getId()).get();
        RsEventDto newRsEvent = rsEventRepository.findById(rsEventDto.getId()).get();
        assertEquals(userDto.getVoteNum(), 9);
        assertEquals(newRsEvent.getVoteNum(), 1);
        List<VoteDto> voteDtos = voteRepository.findAll();
        assertEquals(voteDtos.size(), 1);
        assertEquals(voteDtos.get(0).getNum(), 1);
    }
}
