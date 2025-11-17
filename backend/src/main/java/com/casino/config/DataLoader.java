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
    private final com.casino.service.VIPService vipService;

    @Override
    public void run(String... args) {
        // Initialize VIP tiers
        vipService.initializeDefaultTiers();
        log.info("VIP tiers initialization completed");

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

        // Create game 9 - Chicken Road (if doesn't exist)
        if (!gameRepository.findByGameCode("CHICKEN_ROAD").isPresent()) {
            Game game9 = new Game();
            game9.setGameCode("CHICKEN_ROAD");
            game9.setName("Chicken Road");
            game9.setDescription("Crash-style risk ladder game! Reveal safe tiles to multiply your bet. Cash out before hitting a bomb!");
            game9.setCategory(Game.GameCategory.OTHER);
            game9.setProvider(provider);
            game9.setIframeUrl("http://localhost:8888/fun/chicken-road/index.html");
            game9.setThumbnailUrl("https://via.placeholder.com/300x200?text=Chicken+Road");
            game9.setRtp(new BigDecimal("96.00"));
            game9.setFeatured(true);
            game9.setStatus(Game.GameStatus.ACTIVE);
            game9.setSortOrder(9);
            gameRepository.save(game9);
            log.info("Game data loaded: {}", game9.getName());
        } else {
            log.info("Game already exists: Chicken Road");
        }

        // Create game 10 - European Roulette (if doesn't exist)
        if (!gameRepository.findByGameCode("EUROPEAN_ROULETTE").isPresent()) {
            Game game10 = new Game();
            game10.setGameCode("EUROPEAN_ROULETTE");
            game10.setName("European Roulette");
            game10.setDescription("Classic European Roulette with 37 numbers (0-36). Place your bets on numbers, colors, or combinations. Single zero for better odds!");
            game10.setCategory(Game.GameCategory.TABLE_GAMES);
            game10.setProvider(provider);
            game10.setIframeUrl("http://localhost:8888/table-games/european-roulette/index.html");
            game10.setThumbnailUrl("https://via.placeholder.com/300x200?text=European+Roulette");
            game10.setRtp(new BigDecimal("97.30"));
            game10.setFeatured(true);
            game10.setStatus(Game.GameStatus.ACTIVE);
            game10.setSortOrder(10);
            gameRepository.save(game10);
            log.info("Game data loaded: {}", game10.getName());
        } else {
            log.info("Game already exists: European Roulette");
        }

        // Create game 11 - Baccarat (if doesn't exist)
        if (!gameRepository.findByGameCode("BACCARAT").isPresent()) {
            Game game11 = new Game();
            game11.setGameCode("BACCARAT");
            game11.setName("Baccarat - Punto Banco");
            game11.setDescription("Classic Baccarat! Bet on Player, Banker, or Tie. Closest to 9 wins. One of the most popular casino games worldwide!");
            game11.setCategory(Game.GameCategory.TABLE_GAMES);
            game11.setProvider(provider);
            game11.setIframeUrl("http://localhost:8888/table-games/baccarat/index.html");
            game11.setThumbnailUrl("https://via.placeholder.com/300x200?text=Baccarat");
            game11.setRtp(new BigDecimal("98.94"));
            game11.setFeatured(true);
            game11.setStatus(Game.GameStatus.ACTIVE);
            game11.setSortOrder(11);
            gameRepository.save(game11);
            log.info("Game data loaded: {}", game11.getName());
        } else {
            log.info("Game already exists: Baccarat - Punto Banco");
        }

        // Create game 12 - Dragon Tiger (if doesn't exist)
        if (!gameRepository.findByGameCode("DRAGON_TIGER").isPresent()) {
            Game game12 = new Game();
            game12.setGameCode("DRAGON_TIGER");
            game12.setName("Dragon Tiger");
            game12.setDescription("The fastest card game in Asia! One card each for Dragon and Tiger. Highest card wins. Simple and thrilling!");
            game12.setCategory(Game.GameCategory.TABLE_GAMES);
            game12.setProvider(provider);
            game12.setIframeUrl("http://localhost:8888/table-games/dragon-tiger/index.html");
            game12.setThumbnailUrl("https://via.placeholder.com/300x200?text=Dragon+Tiger");
            game12.setRtp(new BigDecimal("96.27"));
            game12.setFeatured(true);
            game12.setStatus(Game.GameStatus.ACTIVE);
            game12.setSortOrder(12);
            gameRepository.save(game12);
            log.info("Game data loaded: {}", game12.getName());
        } else {
            log.info("Game already exists: Dragon Tiger");
        }

        // Create game 13 - Craps (if doesn't exist)
        if (!gameRepository.findByGameCode("CRAPS").isPresent()) {
            Game game13 = new Game();
            game13.setGameCode("CRAPS");
            game13.setName("Craps");
            game13.setDescription("The most exciting dice game! Bet on Pass Line, Don't Pass, Field, and more. Roll the dice and win big!");
            game13.setCategory(Game.GameCategory.TABLE_GAMES);
            game13.setProvider(provider);
            game13.setIframeUrl("http://localhost:8888/table-games/craps/index.html");
            game13.setThumbnailUrl("https://via.placeholder.com/300x200?text=Craps");
            game13.setRtp(new BigDecimal("98.59"));
            game13.setFeatured(true);
            game13.setStatus(Game.GameStatus.ACTIVE);
            game13.setSortOrder(13);
            gameRepository.save(game13);
            log.info("Game data loaded: {}", game13.getName());
        } else {
            log.info("Game already exists: Craps");
        }

        // Create game 14 - Sic Bo (if doesn't exist)
        if (!gameRepository.findByGameCode("SIC_BO").isPresent()) {
            Game game14 = new Game();
            game14.setGameCode("SIC_BO");
            game14.setName("Sic Bo");
            game14.setDescription("Ancient Chinese dice game! Bet on Small, Big, specific totals, and more. Three dice, endless excitement!");
            game14.setCategory(Game.GameCategory.TABLE_GAMES);
            game14.setProvider(provider);
            game14.setIframeUrl("http://localhost:8888/table-games/sic-bo/index.html");
            game14.setThumbnailUrl("https://via.placeholder.com/300x200?text=Sic+Bo");
            game14.setRtp(new BigDecimal("97.22"));
            game14.setFeatured(true);
            game14.setStatus(Game.GameStatus.ACTIVE);
            game14.setSortOrder(14);
            gameRepository.save(game14);
            log.info("Game data loaded: {}", game14.getName());
        } else {
            log.info("Game already exists: Sic Bo");
        }

        // Create game 15 - Keno (if doesn't exist)
        if (!gameRepository.findByGameCode("KENO").isPresent()) {
            Game game15 = new Game();
            game15.setGameCode("KENO");
            game15.setName("Keno");
            game15.setDescription("Classic lottery-style game! Pick up to 10 numbers from 1-80 and watch the draw. Huge payouts for big hits!");
            game15.setCategory(Game.GameCategory.OTHER);
            game15.setProvider(provider);
            game15.setIframeUrl("http://localhost:8888/other/keno/index.html");
            game15.setThumbnailUrl("https://via.placeholder.com/300x200?text=Keno");
            game15.setRtp(new BigDecimal("92.00"));
            game15.setFeatured(true);
            game15.setStatus(Game.GameStatus.ACTIVE);
            game15.setSortOrder(15);
            gameRepository.save(game15);
            log.info("Game data loaded: {}", game15.getName());
        } else {
            log.info("Game already exists: Keno");
        }

        // Create game 16 - Money Wheel (if doesn't exist)
        if (!gameRepository.findByGameCode("MONEY_WHEEL").isPresent()) {
            Game game16 = new Game();
            game16.setGameCode("MONEY_WHEEL");
            game16.setName("Money Wheel");
            game16.setDescription("Spin the giant Wheel of Fortune! Bet on numbers 1, 2, 5, 10, 20, or hit the Jackpot at 40:1. The most iconic casino game!");
            game16.setCategory(Game.GameCategory.OTHER);
            game16.setProvider(provider);
            game16.setIframeUrl("http://localhost:8888/other/money-wheel/index.html");
            game16.setThumbnailUrl("https://via.placeholder.com/300x200?text=Money+Wheel");
            game16.setRtp(new BigDecimal("94.50"));
            game16.setFeatured(true);
            game16.setStatus(Game.GameStatus.ACTIVE);
            game16.setSortOrder(16);
            gameRepository.save(game16);
            log.info("Game data loaded: {}", game16.getName());
        } else {
            log.info("Game already exists: Money Wheel");
        }

        // Create game 17 - 90-Ball Bingo (if doesn't exist)
        if (!gameRepository.findByGameCode("BINGO_90").isPresent()) {
            Game game17 = new Game();
            game17.setGameCode("BINGO_90");
            game17.setName("90-Ball Bingo");
            game17.setDescription("Classic British Bingo! 6 cards, auto-daub feature. Win prizes for 1 Line, 2 Lines, and Full House! One of the most popular games in Europe!");
            game17.setCategory(Game.GameCategory.OTHER);
            game17.setProvider(provider);
            game17.setIframeUrl("http://localhost:8888/other/bingo/index.html");
            game17.setThumbnailUrl("https://via.placeholder.com/300x200?text=90-Ball+Bingo");
            game17.setRtp(new BigDecimal("95.00"));
            game17.setFeatured(true);
            game17.setStatus(Game.GameStatus.ACTIVE);
            game17.setSortOrder(17);
            gameRepository.save(game17);
            log.info("Game data loaded: {}", game17.getName());
        } else {
            log.info("Game already exists: 90-Ball Bingo");
        }

        // Create game 18 - American Roulette (if doesn't exist)
        if (!gameRepository.findByGameCode("AMERICAN_ROULETTE").isPresent()) {
            Game game18 = new Game();
            game18.setGameCode("AMERICAN_ROULETTE");
            game18.setName("American Roulette");
            game18.setDescription("Classic American Roulette with 38 numbers (0, 00, 1-36). The iconic double-zero wheel! Place your bets on numbers, colors, or combinations. The most popular roulette variant in America!");
            game18.setCategory(Game.GameCategory.TABLE_GAMES);
            game18.setProvider(provider);
            game18.setIframeUrl("http://localhost:8888/table-games/american-roulette/index.html");
            game18.setThumbnailUrl("https://via.placeholder.com/300x200?text=American+Roulette");
            game18.setRtp(new BigDecimal("94.74"));
            game18.setFeatured(true);
            game18.setStatus(Game.GameStatus.ACTIVE);
            game18.setSortOrder(18);
            gameRepository.save(game18);
            log.info("Game data loaded: {}", game18.getName());
        } else {
            log.info("Game already exists: American Roulette");
        }

        // Create game 19 - Lucky 7 (if doesn't exist)
        if (!gameRepository.findByGameCode("LUCKY_7").isPresent()) {
            Game game19 = new Game();
            game19.setGameCode("LUCKY_7");
            game19.setName("Lucky 7");
            game19.setDescription("Lucky 7 is an exciting 3x3 slot machine featuring classic symbols! Hit three 7s for a massive 100x JACKPOT! Multiple paylines including horizontal, vertical, and diagonal combinations. Choose your bet from 1₾ to 100₾ and spin for your fortune!");
            game19.setCategory(Game.GameCategory.SLOTS);
            game19.setProvider(provider);
            game19.setIframeUrl("http://localhost:8888/slots/lucky-7/index.html");
            game19.setThumbnailUrl("http://localhost:8888/slots/lucky-7/photo.jpeg");
            game19.setRtp(new BigDecimal("96.50"));
            game19.setFeatured(true);
            game19.setStatus(Game.GameStatus.ACTIVE);
            game19.setSortOrder(19);
            gameRepository.save(game19);
            log.info("Game data loaded: {}", game19.getName());
        } else {
            log.info("Game already exists: Lucky 7");
        }

        // Create game 20 - Triple Red Hot 777 (if doesn't exist)
        if (!gameRepository.findByGameCode("TRIPLE_RED_HOT_777").isPresent()) {
            Game game20 = new Game();
            game20.setGameCode("TRIPLE_RED_HOT_777");
            game20.setName("Triple Red Hot 777");
            game20.setDescription("Feel the heat with Triple Red Hot 777! A sizzling 3x3 slot with fiery symbols! Hit three 7s for a 200x JACKPOT! Bet from 1₾ to 100₾ and win big!");
            game20.setCategory(Game.GameCategory.SLOTS);
            game20.setProvider(provider);
            game20.setIframeUrl("http://localhost:8888/slots/triple-red-hot-777/index.html");
            game20.setThumbnailUrl("http://localhost:8888/slots/triple-red-hot-777/photo.jpeg");
            game20.setRtp(new BigDecimal("96.80"));
            game20.setFeatured(true);
            game20.setStatus(Game.GameStatus.ACTIVE);
            game20.setSortOrder(20);
            gameRepository.save(game20);
            log.info("Game data loaded: {}", game20.getName());
        } else {
            log.info("Game already exists: Triple Red Hot 777");
        }

        // Create game 21 - Double 7s (if doesn't exist)
        if (!gameRepository.findByGameCode("DOUBLE_7S").isPresent()) {
            Game game21 = new Game();
            game21.setGameCode("DOUBLE_7S");
            game21.setName("Double 7s");
            game21.setDescription("Premium 3x3 slot with colored 7s! Hit three Gold 7s for a huge 250x JACKPOT! Features Red 7s, Blue 7s, and classic symbols. Bet 1-100₾ and win big!");
            game21.setCategory(Game.GameCategory.SLOTS);
            game21.setProvider(provider);
            game21.setIframeUrl("http://localhost:8888/slots/double-7s/index.html");
            game21.setThumbnailUrl("http://localhost:8888/slots/double-7s/photo.jpeg");
            game21.setRtp(new BigDecimal("97.00"));
            game21.setFeatured(true);
            game21.setStatus(Game.GameStatus.ACTIVE);
            game21.setSortOrder(21);
            gameRepository.save(game21);
            log.info("Game data loaded: {}", game21.getName());
        } else {
            log.info("Game already exists: Double 7s");
        }

        // Create game 22 - Fruit Cocktail (if doesn't exist)
        if (!gameRepository.findByGameCode("FRUIT_COCKTAIL").isPresent()) {
            Game game22 = new Game();
            game22.setGameCode("FRUIT_COCKTAIL");
            game22.setName("Fruit Cocktail");
            game22.setDescription("Mix fruits and win big! 3x3 slot with juicy symbols! Hit three Cocktails for 150x JACKPOT! Features strawberries, watermelons, grapes and more!");
            game22.setCategory(Game.GameCategory.SLOTS);
            game22.setProvider(provider);
            game22.setIframeUrl("http://localhost:8888/slots/fruit-cocktail/index.html");
            game22.setThumbnailUrl("http://localhost:8888/slots/fruit-cocktail/photo.jpeg");
            game22.setRtp(new BigDecimal("96.50"));
            game22.setFeatured(true);
            game22.setStatus(Game.GameStatus.ACTIVE);
            game22.setSortOrder(22);
            gameRepository.save(game22);
            log.info("Game data loaded: {}", game22.getName());
        } else {
            log.info("Game already exists: Fruit Cocktail");
        }

        // Create game 23 - Fruit Shop (if doesn't exist)
        if (!gameRepository.findByGameCode("FRUIT_SHOP").isPresent()) {
            Game game23 = new Game();
            game23.setGameCode("FRUIT_SHOP");
            game23.setName("Fruit Shop");
            game23.setDescription("Fresh fruits, big wins! 5-reel slot with delicious symbols! Hit five Watermelons for massive 500x JACKPOT! Features kiwi, strawberry, grapes and more!");
            game23.setCategory(Game.GameCategory.SLOTS);
            game23.setProvider(provider);
            game23.setIframeUrl("http://localhost:8888/slots/fruit-shop/index.html");
            game23.setThumbnailUrl("http://localhost:8888/slots/fruit-shop/photo.jpeg");
            game23.setRtp(new BigDecimal("96.70"));
            game23.setFeatured(true);
            game23.setStatus(Game.GameStatus.ACTIVE);
            game23.setSortOrder(23);
            gameRepository.save(game23);
            log.info("Game data loaded: {}", game23.getName());
        } else {
            log.info("Game already exists: Fruit Shop");
        }

        // Create game 24 - Snake Coin Collector (if doesn't exist)
        if (!gameRepository.findByGameCode("SNAKE_ARCADE").isPresent()) {
            Game game24 = new Game();
            game24.setGameCode("SNAKE_ARCADE");
            game24.setName("Snake Coin Collector");
            game24.setDescription("Classic snake game with a twist! Collect coins worth ₾0.10 each. Start with ₾5 bet, cash out anytime! The game speeds up with each coin collected!");
            game24.setCategory(Game.GameCategory.ARCADE);
            game24.setProvider(provider);
            game24.setIframeUrl("http://localhost:8888/arcade/snake/index.html");
            game24.setThumbnailUrl("http://localhost:8888/arcade/snake/thumbnail.svg");
            game24.setRtp(new BigDecimal("97.00"));
            game24.setFeatured(true);
            game24.setStatus(Game.GameStatus.ACTIVE);
            game24.setSortOrder(24);
            gameRepository.save(game24);
            log.info("Game data loaded: {}", game24.getName());
        } else {
            log.info("Game already exists: Snake Coin Collector");
        }

        // Create game 25 - 40 Super Hot (if doesn't exist)
        if (!gameRepository.findByGameCode("40_SUPER_HOT").isPresent()) {
            Game game25 = new Game();
            game25.setGameCode("40_SUPER_HOT");
            game25.setName("40 Super Hot");
            game25.setDescription("Classic 5x4 fruit slot with 40 paylines! Features blazing sevens, cherries, watermelons and more. Big wins with traditional symbols!");
            game25.setCategory(Game.GameCategory.SLOTS);
            game25.setProvider(provider);
            game25.setIframeUrl("http://localhost:8888/slots/40-super-hot/index.html");
            game25.setThumbnailUrl("http://localhost:8888/slots/40-super-hot/photo.jpeg");
            game25.setRtp(new BigDecimal("95.80"));
            game25.setFeatured(false);
            game25.setStatus(Game.GameStatus.ACTIVE);
            game25.setSortOrder(25);
            gameRepository.save(game25);
            log.info("Game data loaded: {}", game25.getName());
        } else {
            log.info("Game already exists: 40 Super Hot");
        }

        // Create game 26 - 777 Deluxe (if doesn't exist)
        if (!gameRepository.findByGameCode("777_DELUXE").isPresent()) {
            Game game26 = new Game();
            game26.setGameCode("777_DELUXE");
            game26.setName("777 Deluxe");
            game26.setDescription("Premium 3x3 slot with lucky sevens! Hit three 777 symbols for massive JACKPOT! Classic casino experience!");
            game26.setCategory(Game.GameCategory.SLOTS);
            game26.setProvider(provider);
            game26.setIframeUrl("http://localhost:8888/slots/777-deluxe/index.html");
            game26.setThumbnailUrl("http://localhost:8888/slots/777-deluxe/photo.jpeg");
            game26.setRtp(new BigDecimal("96.20"));
            game26.setFeatured(false);
            game26.setStatus(Game.GameStatus.ACTIVE);
            game26.setSortOrder(26);
            gameRepository.save(game26);
            log.info("Game data loaded: {}", game26.getName());
        } else {
            log.info("Game already exists: 777 Deluxe");
        }

        // Create game 27 - Blazing 7s (if doesn't exist)
        if (!gameRepository.findByGameCode("BLAZING_7S").isPresent()) {
            Game game27 = new Game();
            game27.setGameCode("BLAZING_7S");
            game27.setName("Blazing 7s");
            game27.setDescription("Hot 3x3 slot with blazing sevens! Triple red sevens for 200x JACKPOT! Features fiery symbols and big payouts!");
            game27.setCategory(Game.GameCategory.SLOTS);
            game27.setProvider(provider);
            game27.setIframeUrl("http://localhost:8888/slots/blazing-7s/index.html");
            game27.setThumbnailUrl("http://localhost:8888/slots/blazing-7s/photo.jpeg");
            game27.setRtp(new BigDecimal("96.50"));
            game27.setFeatured(false);
            game27.setStatus(Game.GameStatus.ACTIVE);
            game27.setSortOrder(27);
            gameRepository.save(game27);
            log.info("Game data loaded: {}", game27.getName());
        } else {
            log.info("Game already exists: Blazing 7s");
        }

        // Create game 28 - Burning Hot (if doesn't exist)
        if (!gameRepository.findByGameCode("BURNING_HOT").isPresent()) {
            Game game28 = new Game();
            game28.setGameCode("BURNING_HOT");
            game28.setName("Burning Hot");
            game28.setDescription("Sizzling 5-reel slot with hot fruits! Burning symbols and fiery wins! Get three Flames for explosive payouts!");
            game28.setCategory(Game.GameCategory.SLOTS);
            game28.setProvider(provider);
            game28.setIframeUrl("http://localhost:8888/slots/burning-hot/index.html");
            game28.setThumbnailUrl("http://localhost:8888/slots/burning-hot/photo.jpeg");
            game28.setRtp(new BigDecimal("96.45"));
            game28.setFeatured(false);
            game28.setStatus(Game.GameStatus.ACTIVE);
            game28.setSortOrder(28);
            gameRepository.save(game28);
            log.info("Game data loaded: {}", game28.getName());
        } else {
            log.info("Game already exists: Burning Hot");
        }

        // Create game 29 - Fire 7s (if doesn't exist)
        if (!gameRepository.findByGameCode("FIRE_7S").isPresent()) {
            Game game29 = new Game();
            game29.setGameCode("FIRE_7S");
            game29.setName("Fire 7s");
            game29.setDescription("Flaming hot 3x3 slot! Red hot sevens bring fire wins! Triple Fire 7s for sizzling 250x JACKPOT!");
            game29.setCategory(Game.GameCategory.SLOTS);
            game29.setProvider(provider);
            game29.setIframeUrl("http://localhost:8888/slots/fire-7s/index.html");
            game29.setThumbnailUrl("http://localhost:8888/slots/fire-7s/photo.jpeg");
            game29.setRtp(new BigDecimal("96.55"));
            game29.setFeatured(false);
            game29.setStatus(Game.GameStatus.ACTIVE);
            game29.setSortOrder(29);
            gameRepository.save(game29);
            log.info("Game data loaded: {}", game29.getName());
        } else {
            log.info("Game already exists: Fire 7s");
        }

        // Create game 30 - Fruit Mania (if doesn't exist)
        if (!gameRepository.findByGameCode("FRUIT_MANIA").isPresent()) {
            Game game30 = new Game();
            game30.setGameCode("FRUIT_MANIA");
            game30.setName("Fruit Mania");
            game30.setDescription("Go crazy with fruits! 5-reel slot packed with juicy symbols! Watermelons, cherries, lemons and huge wins!");
            game30.setCategory(Game.GameCategory.SLOTS);
            game30.setProvider(provider);
            game30.setIframeUrl("http://localhost:8888/slots/fruit-mania/index.html");
            game30.setThumbnailUrl("http://localhost:8888/slots/fruit-mania/photo.jpeg");
            game30.setRtp(new BigDecimal("96.25"));
            game30.setFeatured(false);
            game30.setStatus(Game.GameStatus.ACTIVE);
            game30.setSortOrder(30);
            gameRepository.save(game30);
            log.info("Game data loaded: {}", game30.getName());
        } else {
            log.info("Game already exists: Fruit Mania");
        }

        // Create game 31 - Fruit Party (if doesn't exist)
        if (!gameRepository.findByGameCode("FRUIT_PARTY").isPresent()) {
            Game game31 = new Game();
            game31.setGameCode("FRUIT_PARTY");
            game31.setName("Fruit Party");
            game31.setDescription("Party with fruits! Colorful 5-reel slot with cascading wins! Strawberries, grapes, oranges bring festive payouts!");
            game31.setCategory(Game.GameCategory.SLOTS);
            game31.setProvider(provider);
            game31.setIframeUrl("http://localhost:8888/slots/fruit-party/index.html");
            game31.setThumbnailUrl("http://localhost:8888/slots/fruit-party/photo.jpeg");
            game31.setRtp(new BigDecimal("96.50"));
            game31.setFeatured(false);
            game31.setStatus(Game.GameStatus.ACTIVE);
            game31.setSortOrder(31);
            gameRepository.save(game31);
            log.info("Game data loaded: {}", game31.getName());
        } else {
            log.info("Game already exists: Fruit Party");
        }

        // Create game 32 - Hot Fruits 100 (if doesn't exist)
        if (!gameRepository.findByGameCode("HOT_FRUITS_100").isPresent()) {
            Game game32 = new Game();
            game32.setGameCode("HOT_FRUITS_100");
            game32.setName("Hot Fruits 100");
            game32.setDescription("100 lines of hot fruit action! Massive 5x4 slot with flaming fruits! Hit five Fire symbols for volcanic 500x JACKPOT!");
            game32.setCategory(Game.GameCategory.SLOTS);
            game32.setProvider(provider);
            game32.setIframeUrl("http://localhost:8888/slots/hot-fruits-100/index.html");
            game32.setThumbnailUrl("http://localhost:8888/slots/hot-fruits-100/photo.jpeg");
            game32.setRtp(new BigDecimal("96.75"));
            game32.setFeatured(true);
            game32.setStatus(Game.GameStatus.ACTIVE);
            game32.setSortOrder(32);
            gameRepository.save(game32);
            log.info("Game data loaded: {}", game32.getName());
        } else {
            log.info("Game already exists: Hot Fruits 100");
        }

        // Create game 33 - Hot Fruits 20 (if doesn't exist)
        if (!gameRepository.findByGameCode("HOT_FRUITS_20").isPresent()) {
            Game game33 = new Game();
            game33.setGameCode("HOT_FRUITS_20");
            game33.setName("Hot Fruits 20");
            game33.setDescription("20 sizzling paylines! Classic 5x3 slot with hot fruits! Fire symbols trigger burning wins!");
            game33.setCategory(Game.GameCategory.SLOTS);
            game33.setProvider(provider);
            game33.setIframeUrl("http://localhost:8888/slots/hot-fruits-20/index.html");
            game33.setThumbnailUrl("http://localhost:8888/slots/hot-fruits-20/photo.jpeg");
            game33.setRtp(new BigDecimal("96.30"));
            game33.setFeatured(false);
            game33.setStatus(Game.GameStatus.ACTIVE);
            game33.setSortOrder(33);
            gameRepository.save(game33);
            log.info("Game data loaded: {}", game33.getName());
        } else {
            log.info("Game already exists: Hot Fruits 20");
        }

        // Create game 34 - Hot Fruits 40 (if doesn't exist)
        if (!gameRepository.findByGameCode("HOT_FRUITS_40").isPresent()) {
            Game game34 = new Game();
            game34.setGameCode("HOT_FRUITS_40");
            game34.setName("Hot Fruits 40");
            game34.setDescription("40 lines of fruity heat! Expanded 5x4 grid with scorching symbols! Five flames for 400x inferno!");
            game34.setCategory(Game.GameCategory.SLOTS);
            game34.setProvider(provider);
            game34.setIframeUrl("http://localhost:8888/slots/hot-fruits-40/index.html");
            game34.setThumbnailUrl("http://localhost:8888/slots/hot-fruits-40/photo.jpeg");
            game34.setRtp(new BigDecimal("96.60"));
            game34.setFeatured(false);
            game34.setStatus(Game.GameStatus.ACTIVE);
            game34.setSortOrder(34);
            gameRepository.save(game34);
            log.info("Game data loaded: {}", game34.getName());
        } else {
            log.info("Game already exists: Hot Fruits 40");
        }

        // Create game 35 - Juicy Fruits (if doesn't exist)
        if (!gameRepository.findByGameCode("JUICY_FRUITS").isPresent()) {
            Game game35 = new Game();
            game35.setGameCode("JUICY_FRUITS");
            game35.setName("Juicy Fruits");
            game35.setDescription("Squeeze out juicy wins! 5-reel slot bursting with fresh fruits! Watermelons, oranges, cherries drip with rewards!");
            game35.setCategory(Game.GameCategory.SLOTS);
            game35.setProvider(provider);
            game35.setIframeUrl("http://localhost:8888/slots/juicy-fruits/index.html");
            game35.setThumbnailUrl("http://localhost:8888/slots/juicy-fruits/photo.jpeg");
            game35.setRtp(new BigDecimal("96.40"));
            game35.setFeatured(false);
            game35.setStatus(Game.GameStatus.ACTIVE);
            game35.setSortOrder(35);
            gameRepository.save(game35);
            log.info("Game data loaded: {}", game35.getName());
        } else {
            log.info("Game already exists: Juicy Fruits");
        }

        // Create game 36 - Mega 7s (if doesn't exist)
        if (!gameRepository.findByGameCode("MEGA_7S").isPresent()) {
            Game game36 = new Game();
            game36.setGameCode("MEGA_7S");
            game36.setName("Mega 7s");
            game36.setDescription("Mega lucky sevens! 3x3 slot with giant payouts! Triple Mega 7s for colossal 300x JACKPOT!");
            game36.setCategory(Game.GameCategory.SLOTS);
            game36.setProvider(provider);
            game36.setIframeUrl("http://localhost:8888/slots/mega-7s/index.html");
            game36.setThumbnailUrl("http://localhost:8888/slots/mega-7s/photo.jpeg");
            game36.setRtp(new BigDecimal("96.65"));
            game36.setFeatured(false);
            game36.setStatus(Game.GameStatus.ACTIVE);
            game36.setSortOrder(36);
            gameRepository.save(game36);
            log.info("Game data loaded: {}", game36.getName());
        } else {
            log.info("Game already exists: Mega 7s");
        }

        // Create game 37 - Sizzling Hot (if doesn't exist)
        if (!gameRepository.findByGameCode("SIZZLING_HOT").isPresent()) {
            Game game37 = new Game();
            game37.setGameCode("SIZZLING_HOT");
            game37.setName("Sizzling Hot");
            game37.setDescription("Classic sizzling fruits! 5-reel slot with scorching payouts! Red hot sevens and fiery fruits bring the heat!");
            game37.setCategory(Game.GameCategory.SLOTS);
            game37.setProvider(provider);
            game37.setIframeUrl("http://localhost:8888/slots/sizzling-hot/index.html");
            game37.setThumbnailUrl("http://localhost:8888/slots/sizzling-hot/photo.jpeg");
            game37.setRtp(new BigDecimal("95.66"));
            game37.setFeatured(true);
            game37.setStatus(Game.GameStatus.ACTIVE);
            game37.setSortOrder(37);
            gameRepository.save(game37);
            log.info("Game data loaded: {}", game37.getName());
        } else {
            log.info("Game already exists: Sizzling Hot");
        }

        // Create game 38 - Sweet Bonanza (if doesn't exist)
        if (!gameRepository.findByGameCode("SWEET_BONANZA").isPresent()) {
            Game game38 = new Game();
            game38.setGameCode("SWEET_BONANZA");
            game38.setName("Sweet Bonanza");
            game38.setDescription("Sweet cascade slot! Tumbling reels with candies and fruits! Multipliers and free spins bring sugar rush wins!");
            game38.setCategory(Game.GameCategory.SLOTS);
            game38.setProvider(provider);
            game38.setIframeUrl("http://localhost:8888/slots/sweet-bonanza/index.html");
            game38.setThumbnailUrl("http://localhost:8888/slots/sweet-bonanza/photo.jpeg");
            game38.setRtp(new BigDecimal("96.48"));
            game38.setFeatured(true);
            game38.setStatus(Game.GameStatus.ACTIVE);
            game38.setSortOrder(38);
            gameRepository.save(game38);
            log.info("Game data loaded: {}", game38.getName());
        } else {
            log.info("Game already exists: Sweet Bonanza");
        }

        // Create game 39 - Triple Bar (if doesn't exist)
        if (!gameRepository.findByGameCode("TRIPLE_BAR").isPresent()) {
            Game game39 = new Game();
            game39.setGameCode("TRIPLE_BAR");
            game39.setName("Triple Bar");
            game39.setDescription("Classic bar symbols! 3x3 traditional slot! Triple BAR symbols for nostalgic 200x JACKPOT!");
            game39.setCategory(Game.GameCategory.SLOTS);
            game39.setProvider(provider);
            game39.setIframeUrl("http://localhost:8888/slots/triple-bar/index.html");
            game39.setThumbnailUrl("http://localhost:8888/slots/triple-bar/photo.jpeg");
            game39.setRtp(new BigDecimal("96.15"));
            game39.setFeatured(false);
            game39.setStatus(Game.GameStatus.ACTIVE);
            game39.setSortOrder(39);
            gameRepository.save(game39);
            log.info("Game data loaded: {}", game39.getName());
        } else {
            log.info("Game already exists: Triple Bar");
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
