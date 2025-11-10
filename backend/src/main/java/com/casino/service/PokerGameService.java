package com.casino.service;

import com.casino.entity.*;
import com.casino.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PokerGameService {

    private final PokerTableRepository pokerTableRepository;
    private final PokerTableSeatRepository seatRepository;
    private final PokerHandRepository handRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    // Card deck
    private static final String[] SUITS = {"♠", "♥", "♦", "♣"};
    private static final String[] RANKS = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

    @Transactional
    public PokerTable createTable(String tableName, BigDecimal smallBlind, BigDecimal bigBlind,
                                   BigDecimal minBuyIn, BigDecimal maxBuyIn, Integer maxPlayers) {
        PokerTable table = new PokerTable();
        table.setTableCode(UUID.randomUUID().toString());
        table.setTableName(tableName);
        table.setSmallBlind(smallBlind);
        table.setBigBlind(bigBlind);
        table.setMinBuyIn(minBuyIn);
        table.setMaxBuyIn(maxBuyIn);
        table.setMaxPlayers(maxPlayers);
        table.setStatus(PokerTable.TableStatus.WAITING);

        return pokerTableRepository.save(table);
    }

    @Transactional
    public void joinTable(Long tableId, Long userId, BigDecimal buyInAmount, Integer seatNumber) {
        PokerTable table = pokerTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate buy-in
        if (buyInAmount.compareTo(table.getMinBuyIn()) < 0 ||
            buyInAmount.compareTo(table.getMaxBuyIn()) > 0) {
            throw new RuntimeException("Invalid buy-in amount");
        }

        // Check user balance
        if (user.getBalance().compareTo(buyInAmount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Check seat availability
        Optional<PokerTableSeat> existingSeat = seatRepository.findByTableAndSeatNumber(table, seatNumber);
        if (existingSeat.isPresent() && existingSeat.get().getStatus() != PokerTableSeat.SeatStatus.EMPTY) {
            throw new RuntimeException("Seat already occupied");
        }

        // Deduct from user balance
        user.setBalance(user.getBalance().subtract(buyInAmount));
        userRepository.save(user);

        // Create or update seat
        PokerTableSeat seat = existingSeat.orElse(new PokerTableSeat());
        seat.setTable(table);
        seat.setUser(user);
        seat.setSeatNumber(seatNumber);
        seat.setChipStack(buyInAmount);
        seat.setStatus(PokerTableSeat.SeatStatus.OCCUPIED);
        seat.setIsSittingOut(false);
        seatRepository.save(seat);

        // Update table player count
        table.setCurrentPlayers(table.getCurrentPlayers() + 1);
        pokerTableRepository.save(table);

        // Broadcast to table
        broadcastTableUpdate(table);

        // Start game if enough players
        if (table.getCurrentPlayers() >= table.getMinPlayers() &&
            table.getStatus() == PokerTable.TableStatus.WAITING) {
            startNewHand(table);
        }
    }

    @Transactional
    public void leaveTable(Long tableId, Long userId) {
        PokerTable table = pokerTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PokerTableSeat seat = seatRepository.findByTableAndUser(table, user)
                .orElseThrow(() -> new RuntimeException("User not at table"));

        // Return chips to user balance
        user.setBalance(user.getBalance().add(seat.getChipStack()));
        userRepository.save(user);

        // Remove seat
        seatRepository.delete(seat);

        // Update table
        table.setCurrentPlayers(table.getCurrentPlayers() - 1);
        if (table.getCurrentPlayers() < table.getMinPlayers()) {
            table.setStatus(PokerTable.TableStatus.WAITING);
        }
        pokerTableRepository.save(table);

        broadcastTableUpdate(table);
    }

    @Transactional
    public void startNewHand(PokerTable table) {
        List<PokerTableSeat> seats = seatRepository.findByTableOrderBySeatNumber(table);
        List<PokerTableSeat> activeSeats = seats.stream()
                .filter(s -> s.getStatus() == PokerTableSeat.SeatStatus.OCCUPIED && !s.getIsSittingOut())
                .collect(Collectors.toList());

        if (activeSeats.size() < table.getMinPlayers()) {
            return;
        }

        // Create new hand
        PokerHand hand = new PokerHand();
        hand.setTable(table);
        hand.setHandId(UUID.randomUUID().toString());
        hand.setDealerPosition(table.getDealerPosition());

        // Calculate blinds positions
        int sbPos = (table.getDealerPosition() + 1) % table.getMaxPlayers();
        int bbPos = (table.getDealerPosition() + 2) % table.getMaxPlayers();
        hand.setSmallBlindPosition(sbPos);
        hand.setBigBlindPosition(bbPos);

        // Create and shuffle deck
        List<String> deck = createDeck();
        Collections.shuffle(deck);

        // Deal hole cards
        Map<Long, List<String>> playerHands = new HashMap<>();
        for (int i = 0; i < 2; i++) {
            for (PokerTableSeat seat : activeSeats) {
                playerHands.computeIfAbsent(seat.getUser().getId(), k -> new ArrayList<>())
                        .add(deck.remove(0));
            }
        }

        try {
            hand.setDeck(objectMapper.writeValueAsString(deck));
            hand.setPlayerHands(objectMapper.writeValueAsString(playerHands));
            hand.setCommunityCards(objectMapper.writeValueAsString(new ArrayList<>()));
            hand.setActivePlayers(objectMapper.writeValueAsString(
                    activeSeats.stream().map(s -> s.getUser().getId()).collect(Collectors.toList())));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize hand data", e);
        }

        hand.setStage(PokerHand.HandStage.PRE_FLOP);
        hand.setStatus(PokerHand.HandStatus.IN_PROGRESS);
        hand.setCurrentBet(table.getBigBlind());
        hand.setPotAmount(table.getSmallBlind().add(table.getBigBlind()));

        // Post blinds
        postBlinds(table, activeSeats, hand);

        handRepository.save(hand);

        // Update table
        table.setStatus(PokerTable.TableStatus.PLAYING);
        table.setCurrentHandId(hand.getHandId());
        table.setDealerPosition((table.getDealerPosition() + 1) % table.getMaxPlayers());
        pokerTableRepository.save(table);

        broadcastHandStart(table, hand);
    }

    private void postBlinds(PokerTable table, List<PokerTableSeat> activeSeats, PokerHand hand) {
        // Find seats for blinds
        PokerTableSeat sbSeat = activeSeats.stream()
                .filter(s -> s.getSeatNumber().equals(hand.getSmallBlindPosition()))
                .findFirst().orElse(null);
        PokerTableSeat bbSeat = activeSeats.stream()
                .filter(s -> s.getSeatNumber().equals(hand.getBigBlindPosition()))
                .findFirst().orElse(null);

        if (sbSeat != null) {
            sbSeat.setChipStack(sbSeat.getChipStack().subtract(table.getSmallBlind()));
            seatRepository.save(sbSeat);
        }

        if (bbSeat != null) {
            bbSeat.setChipStack(bbSeat.getChipStack().subtract(table.getBigBlind()));
            seatRepository.save(bbSeat);
        }
    }

    private List<String> createDeck() {
        List<String> deck = new ArrayList<>();
        for (String suit : SUITS) {
            for (String rank : RANKS) {
                deck.add(rank + suit);
            }
        }
        return deck;
    }

    private void broadcastTableUpdate(PokerTable table) {
        messagingTemplate.convertAndSend("/topic/poker/table/" + table.getId(), Map.of(
                "type", "TABLE_UPDATE",
                "table", table
        ));
    }

    private void broadcastHandStart(PokerTable table, PokerHand hand) {
        messagingTemplate.convertAndSend("/topic/poker/table/" + table.getId(), Map.of(
                "type", "HAND_START",
                "hand", hand
        ));
    }

    public List<PokerTable> getAvailableTables() {
        return pokerTableRepository.findByStatusIn(
                Arrays.asList(PokerTable.TableStatus.WAITING, PokerTable.TableStatus.PLAYING));
    }

    public PokerTable getTable(Long tableId) {
        return pokerTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));
    }

    public List<PokerTableSeat> getTableSeats(Long tableId) {
        PokerTable table = getTable(tableId);
        return seatRepository.findByTableOrderBySeatNumber(table);
    }
}
