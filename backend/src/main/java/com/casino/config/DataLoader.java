package com.casino.config;

import com.casino.entity.Admin;
import com.casino.entity.Game;
import com.casino.entity.GameProvider;
import com.casino.entity.User;
import com.casino.repository.AdminRepository;
import com.casino.repository.GameProviderRepository;
import com.casino.repository.GameRepository;
import com.casino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final GameProviderRepository gameProviderRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Save or get existing provider
        GameProvider provider = gameProviderRepository.findByCode("HOUSE")
                .orElseGet(() -> {
                    GameProvider newProvider = new GameProvider();
                    newProvider.setCode("HOUSE");
                    newProvider.setName("House Games");
                    newProvider.setIntegrationType(GameProvider.IntegrationType.IFRAME);
                    newProvider.setStatus(GameProvider.ProviderStatus.ACTIVE);
                    newProvider.setApiUrl("http://localhost:8888");
                    newProvider.setApiKey("house-key-2024");
                    newProvider.setLogoUrl("http://localhost:8888/logo.png");
                    return gameProviderRepository.save(newProvider);
                });

        // Create game 1 - Classic Fruit Slot (if doesn't exist)
        if (!gameRepository.findByGameCode("FRUIT_SLOT_01").isPresent()) {
            Game game1 = new Game();
            game1.setGameCode("FRUIT_SLOT_01");
            game1.setName("Classic Fruit Slot");
            game1.setDescription("Classic 3-reel fruit slot machine!");
            game1.setCategory(Game.GameCategory.SLOTS);
            game1.setProvider(provider);
            game1.setIframeUrl("http://localhost:8888/slots/first/index.html");
            game1.setThumbnailUrl("https://via.placeholder.com/300x200?text=Fruit+Slot");
            game1.setRtp(new BigDecimal("96.50"));
            game1.setFeatured(true);
            game1.setStatus(Game.GameStatus.ACTIVE);
            game1.setSortOrder(1);
            gameRepository.save(game1);
            log.info("Game data loaded: {}", game1.getName());
        } else {
            log.info("Game already exists: Classic Fruit Slot");
        }

        // Create game 2 - Simple 5-Reel Video Slots (if doesn't exist)
        if (!gameRepository.findByGameCode("VIDEO_SLOT_5REEL_01").isPresent()) {
            Game game2 = new Game();
            game2.setGameCode("VIDEO_SLOT_5REEL_01");
            game2.setName("Simple 5-Reel Video Slots");
            game2.setDescription("Modern 5-reel video slot with 25 paylines!");
            game2.setCategory(Game.GameCategory.SLOTS);
            game2.setProvider(provider);
            game2.setIframeUrl("http://localhost:8888/slots/simple5-reel-video-slots/index.html");
            game2.setThumbnailUrl("https://via.placeholder.com/300x200?text=5+Reel+Slots");
            game2.setRtp(new BigDecimal("96.80"));
            game2.setFeatured(true);
            game2.setStatus(Game.GameStatus.ACTIVE);
            game2.setSortOrder(2);
            gameRepository.save(game2);
            log.info("Game data loaded: {}", game2.getName());
        } else {
            log.info("Game already exists: Simple 5-Reel Video Slots");
        }

        // Create game 3 - Book of Fortune (if doesn't exist)
        if (!gameRepository.findByGameCode("BOOK_SLOT_01").isPresent()) {
            Game game3 = new Game();
            game3.setGameCode("BOOK_SLOT_01");
            game3.setName("Book of Fortune");
            game3.setDescription("Unlock the ancient mysteries! Book-style slot with expanding symbols!");
            game3.setCategory(Game.GameCategory.SLOTS);
            game3.setProvider(provider);
            game3.setIframeUrl("http://localhost:8888/slots/book-style-slots/index.html");
            game3.setThumbnailUrl("https://via.placeholder.com/300x200?text=Book+of+Fortune");
            game3.setRtp(new BigDecimal("96.90"));
            game3.setFeatured(true);
            game3.setStatus(Game.GameStatus.ACTIVE);
            game3.setSortOrder(3);
            gameRepository.save(game3);
            log.info("Game data loaded: {}", game3.getName());
        } else {
            log.info("Game already exists: Book of Fortune");
        }

        // Create game 4 - Fortune Hold & Win (if doesn't exist)
        if (!gameRepository.findByGameCode("HOLD_WIN_01").isPresent()) {
            Game game4 = new Game();
            game4.setGameCode("HOLD_WIN_01");
            game4.setName("Fortune Hold & Win");
            game4.setDescription("Lock the money symbols and win the Grand Jackpot!");
            game4.setCategory(Game.GameCategory.SLOTS);
            game4.setProvider(provider);
            game4.setIframeUrl("http://localhost:8888/slots/hold-and-win/index.html");
            game4.setThumbnailUrl("https://via.placeholder.com/300x200?text=Hold+%26+Win");
            game4.setRtp(new BigDecimal("97.00"));
            game4.setFeatured(true);
            game4.setStatus(Game.GameStatus.ACTIVE);
            game4.setSortOrder(4);
            gameRepository.save(game4);
            log.info("Game data loaded: {}", game4.getName());
        } else {
            log.info("Game already exists: Fortune Hold & Win");
        }

        // Create game 5 - Blackjack (if doesn't exist)
        if (!gameRepository.findByGameCode("BLACKJACK_01").isPresent()) {
            Game game5 = new Game();
            game5.setGameCode("BLACKJACK_01");
            game5.setName("Blackjack - Classic 21");
            game5.setDescription("Beat the dealer in the classic card game! Dealer stands on 17, Blackjack pays 3:2");
            game5.setCategory(Game.GameCategory.TABLE_GAMES);
            game5.setProvider(provider);
            game5.setIframeUrl("http://localhost:8888/table-games/blackjack/index.html");
            game5.setThumbnailUrl("https://via.placeholder.com/300x200?text=Blackjack");
            game5.setRtp(new BigDecimal("99.50"));
            game5.setFeatured(true);
            game5.setStatus(Game.GameStatus.ACTIVE);
            game5.setSortOrder(5);
            gameRepository.save(game5);
            log.info("Game data loaded: {}", game5.getName());
        } else {
            log.info("Game already exists: Blackjack - Classic 21");
        }

        // Create game 6 - Jacks or Better (if doesn't exist)
        if (!gameRepository.findByGameCode("JACKS_BETTER_01").isPresent()) {
            Game game6 = new Game();
            game6.setGameCode("JACKS_BETTER_01");
            game6.setName("Jacks or Better - Video Poker");
            game6.setDescription("Classic video poker! Get a pair of Jacks or better to win. Royal Flush pays 250x!");
            game6.setCategory(Game.GameCategory.VIDEO_POKER);
            game6.setProvider(provider);
            game6.setIframeUrl("http://localhost:8888/poker/jack-or-better/index.html");
            game6.setThumbnailUrl("https://via.placeholder.com/300x200?text=Jacks+or+Better");
            game6.setRtp(new BigDecimal("99.54"));
            game6.setFeatured(true);
            game6.setStatus(Game.GameStatus.ACTIVE);
            game6.setSortOrder(6);
            gameRepository.save(game6);
            log.info("Game data loaded: {}", game6.getName());
        } else {
            log.info("Game already exists: Jacks or Better - Video Poker");
        }

        // Create game 7 - Three Card Poker (if doesn't exist)
        if (!gameRepository.findByGameCode("THREE_CARD_POKER_01").isPresent()) {
            Game game7 = new Game();
            game7.setGameCode("THREE_CARD_POKER_01");
            game7.setName("Three Card Poker");
            game7.setDescription("Ante, Play, Win! Beat the dealer with your 3-card hand. Dealer qualifies with Queen high or better!");
            game7.setCategory(Game.GameCategory.TABLE_GAMES);
            game7.setProvider(provider);
            game7.setIframeUrl("http://localhost:8888/poker/three-card-poker/index.html");
            game7.setThumbnailUrl("https://via.placeholder.com/300x200?text=3+Card+Poker");
            game7.setRtp(new BigDecimal("98.20"));
            game7.setFeatured(true);
            game7.setStatus(Game.GameStatus.ACTIVE);
            game7.setSortOrder(7);
            gameRepository.save(game7);
            log.info("Game data loaded: {}", game7.getName());
        } else {
            log.info("Game already exists: Three Card Poker");
        }

        // Create game 8 - Caribbean Stud Poker (if doesn't exist)
        if (!gameRepository.findByGameCode("CARIBBEAN_STUD_01").isPresent()) {
            Game game8 = new Game();
            game8.setGameCode("CARIBBEAN_STUD_01");
            game8.setName("Caribbean Stud Poker");
            game8.setDescription("5-card poker against the dealer! Dealer qualifies with Ace-King or better. Royal Flush pays 100:1!");
            game8.setCategory(Game.GameCategory.TABLE_GAMES);
            game8.setProvider(provider);
            game8.setIframeUrl("http://localhost:8888/poker/caribbean-stud-poker/index.html");
            game8.setThumbnailUrl("https://via.placeholder.com/300x200?text=Caribbean+Stud");
            game8.setRtp(new BigDecimal("97.80"));
            game8.setFeatured(true);
            game8.setStatus(Game.GameStatus.ACTIVE);
            game8.setSortOrder(8);
            gameRepository.save(game8);
            log.info("Game data loaded: {}", game8.getName());
        } else {
            log.info("Game already exists: Caribbean Stud Poker");
        }

        // Create test user if doesn't exist (run every time)
        if (!userRepository.findByEmail("test@casino.ge").isPresent()) {
            User testUser = new User();
            testUser.setEmail("test@casino.ge");
            testUser.setPassword(passwordEncoder.encode("Test1234"));
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser.setPhone("+995555123456");
            testUser.setBalance(new BigDecimal("1000.00"));
            testUser.setStatus(User.UserStatus.ACTIVE);
            testUser.setKycStatus(User.KYCStatus.VERIFIED);

            userRepository.save(testUser);
            log.info("Test user created: test@casino.ge / Test1234 with balance: ₾1000");
        }

        // Create admins for available roles if they don't exist
        createAdminIfNotExists("owner", "owner@casino.ge", "Test1234", "გიორგი", "მესვეური", Admin.AdminRole.OWNER);
        createAdminIfNotExists("finance", "finance@casino.ge", "Test1234", "დავით", "ფინანსისტი", Admin.AdminRole.FINANCE);
        createAdminIfNotExists("support", "support@casino.ge", "Test1234", "მარიამ", "მხარდამჭერი", Admin.AdminRole.SUPPORT);
        createAdminIfNotExists("content", "content@casino.ge", "Test1234", "ლაშა", "კონტენტმენეჯერი", Admin.AdminRole.CONTENT);
    }

    private void createAdminIfNotExists(String username, String email, String password,
                                        String firstName, String lastName, Admin.AdminRole role) {
        if (!adminRepository.findByUsername(username).isPresent()) {
            Admin admin = new Admin();
            admin.setUsername(username);
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setFirstName(firstName);
            admin.setLastName(lastName);
            admin.setRole(role);
            admin.setStatus(Admin.AdminStatus.ACTIVE);

            adminRepository.save(admin);
            log.info("Admin created: {} / {} with role: {}", username, password, role);
        }
    }
}
