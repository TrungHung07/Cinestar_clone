package com.cinestar.config;

import com.cinestar.entity.Booking;
import com.cinestar.entity.BookingSeat;
import com.cinestar.entity.Cinema;
import com.cinestar.entity.Combo;
import com.cinestar.entity.Genre;
import com.cinestar.entity.Movie;
import com.cinestar.entity.Payment;
import com.cinestar.entity.Promotion;
import com.cinestar.entity.Role;
import com.cinestar.entity.Room;
import com.cinestar.entity.Seat;
import com.cinestar.entity.Showtime;
import com.cinestar.entity.User;
import com.cinestar.entity.enums.BookingStatus;
import com.cinestar.entity.enums.MovieStatus;
import com.cinestar.entity.enums.PaymentMethod;
import com.cinestar.entity.enums.PaymentStatus;
import com.cinestar.entity.enums.SeatBookingStatus;
import com.cinestar.entity.enums.SeatType;
import com.cinestar.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Du lieu mau cho demo - chi chay khi database con trong.
 * Phim/rap/khuyen mai duoc CRAWL tu cinestar.com.vn (JSON trong resources/data).
 * Tai khoan demo: admin/admin123 (ADMIN), user1/user123 (USER - "Nguyễn Văn A")
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final ComboRepository comboRepository;
    private final PromotionRepository promotionRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final org.springframework.cache.CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Database da co du lieu - bo qua seed");
            return;
        }
        log.info("SEED: dang tao du lieu mau...");
        seedRoles();
        seedUsers();
        seedMoviesFromCrawl();
        seedCinemasFromCrawl();
        seedShowtimes();
        seedCombos();
        seedPromotionsFromCrawl();
        seedSampleBookings();

        for (String name : List.of("movies", "movie-details", "cinemas", "showtimes", "combos", "promotions")) {
            var cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        }
        log.info("SEED: hoan tat!");
    }

    // ================== Dinh nghiet JSON crawl ==================

    private <T> List<T> loadJson(String file, TypeReference<List<T>> type) {
        try {
            return objectMapper.readValue(new ClassPathResource("data/" + file).getInputStream(), type);
        } catch (Exception e) {
            log.warn("Khong doc duoc {}: {}", file, e.getMessage());
            return List.of();
        }
    }

    private void seedRoles() {
        roleRepository.save(Role.builder().name("ROLE_ADMIN").description("Quản trị hệ thống").build());
        roleRepository.save(Role.builder().name("ROLE_USER").description("Khách hàng").build());
    }

    private void seedUsers() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();

        userRepository.save(User.builder()
                .username("admin")
                .email("admin@cinestar.vn")
                .password(passwordEncoder.encode("admin123"))
                .fullName("Quản Trị Viên")
                .phone("0900111222")
                .enabled(true)
                .roles(java.util.Set.of(adminRole))
                .build());

        userRepository.save(User.builder()
                .username("user1")
                .email("nguyenvana@example.com")
                .password(passwordEncoder.encode("user123"))
                .fullName("Nguyễn Văn A")
                .phone("0900333444")
                .enabled(true)
                .roles(java.util.Set.of(userRole))
                .build());
    }

    // ================== PHIM (crawl) ==================

    private void seedMoviesFromCrawl() {
        List<Map<String, Object>> movies = loadJson("movies.json",
                new TypeReference<List<Map<String, Object>>>() {});
        log.info("SEED: {} phim tu du lieu crawl cinestar.com.vn", movies.size());

        Map<String, Genre> genreCache = new HashMap<>();
        Set<String> usedSlugs = new HashSet<>();

        for (Map<String, Object> m : movies) {
            String title = str(m.get("title"));
            String slug = uniqueSlug(str(m.get("slug")), usedSlugs);

            Movie movie = Movie.builder()
                    .title(title)
                    .slug(slug)
                    .description(str(m.get("description")))
                    .posterUrl(str(m.get("posterUrl")))
                    .backdropUrl(str(m.get("backdropUrl")))
                    .trailerUrl(str(m.get("trailerUrl")))
                    .duration(intOf(m.get("duration"), 100))
                    .country(str(m.get("country")))
                    .releaseDate(parseDate(str(m.get("releaseDate"))))
                    .status("UPCOMING".equals(str(m.get("status"))) ? MovieStatus.UPCOMING : MovieStatus.NOW_SHOWING)
                    .ageRating(str(m.get("ageRating")))
                    .format(str(m.get("format")))
                    .subtitleType(str(m.get("subtitleType")))
                    .director(str(m.get("director")))
                    .actors(str(m.get("actors")))
                    .rating(decimalOf(m.get("rating")))
                    .isFeatured(Boolean.TRUE.equals(m.get("isFeatured")))
                    .build();

            Object genresRaw = m.get("genres");
            if (genresRaw instanceof List<?> names) {
                for (Object g : names) {
                    String genreName = str(g);
                    if (genreName.isBlank()) continue;
                    Genre genre = genreCache.computeIfAbsent(genreName, this::findOrCreateGenre);
                    movie.getGenres().add(genre);
                }
            }
            movieRepository.save(movie);
        }
    }

    private Genre findOrCreateGenre(String name) {
        return genreRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> genreRepository.save(Genre.builder()
                        .name(name)
                        .slug(slugify(name))
                        .build()));
    }

    // ================== RAP (crawl) ==================

    private void seedCinemasFromCrawl() {
        List<Map<String, Object>> cinemas = loadJson("cinemas.json",
                new TypeReference<List<Map<String, Object>>>() {});
        log.info("SEED: {} rap tu du lieu crawl cinestar.com.vn", cinemas.size());

        int idx = 0;
        for (Map<String, Object> c : cinemas) {
            String name = str(c.get("name"));
            String slug = uniqueSlug(str(c.get("slug")), new HashSet<>());
            idx++;

            Cinema cinema = cinemaRepository.save(Cinema.builder()
                    .name(name)
                    .slug(slug)
                    .address(str(c.get("address")))
                    .city(str(c.get("city")))
                    .hotline(str(c.get("hotline")))
                    .imageUrl(str(c.get("imageUrl")))
                    .active(true)
                    .build());

            // Moi rap 2 phong: phong 1 lon (75 ghe), phong 2 nho (44 ghe)
            Room room1 = roomRepository.save(Room.builder()
                    .name("Phòng 1").cinema(cinema).seatRows(8).seatColumns(10).build());
            Room room2 = roomRepository.save(Room.builder()
                    .name("Phòng 2").cinema(cinema).seatRows(6).seatColumns(8).build());

            for (char row = 'A'; row <= 'G'; row++) {
                for (int num = 1; num <= 10; num++) {
                    seatRepository.save(Seat.builder().room(room1)
                            .rowLabel(String.valueOf(row)).seatNumber(num)
                            .seatType(SeatType.STANDARD).active(true).build());
                }
            }
            for (int num = 1; num <= 5; num++) {
                seatRepository.save(Seat.builder().room(room1)
                        .rowLabel("H").seatNumber(num).seatType(SeatType.COUPLE).active(true).build());
            }
            for (char row = 'A'; row <= 'E'; row++) {
                for (int num = 1; num <= 8; num++) {
                    seatRepository.save(Seat.builder().room(room2)
                            .rowLabel(String.valueOf(row)).seatNumber(num)
                            .seatType(SeatType.STANDARD).active(true).build());
                }
            }
            for (int num = 1; num <= 4; num++) {
                seatRepository.save(Seat.builder().room(room2)
                        .rowLabel("F").seatNumber(num).seatType(SeatType.COUPLE).active(true).build());
            }
        }
    }

    // ================== SUAT CHIEU ==================

    private void seedShowtimes() {
        List<Movie> nowShowing = movieRepository
                .findByStatus(MovieStatus.NOW_SHOWING, org.springframework.data.domain.PageRequest.of(0, 30))
                .getContent();
        List<Cinema> cinemas = cinemaRepository.findAllByActiveTrue();

        LocalTime[] times = {LocalTime.of(10, 0), LocalTime.of(14, 30), LocalTime.of(19, 0)};
        int movieIdx = 0;
        for (Cinema cinema : cinemas) {
            List<Room> rooms = roomRepository.findAllByCinemaId(cinema.getId());
            for (int day = 0; day < 3; day++) {
                LocalDate date = LocalDate.now().plusDays(day);
                for (Room room : rooms) {
                    for (LocalTime time : times) {
                        if (nowShowing.isEmpty()) return;
                        Movie movie = nowShowing.get(movieIdx % nowShowing.size());
                        movieIdx++;
                        LocalDateTime start = LocalDateTime.of(date, time);
                        showtimeRepository.save(Showtime.builder()
                                .movie(movie)
                                .room(room)
                                .startTime(start)
                                .endTime(start.plusMinutes(
                                        (movie.getDuration() != null ? movie.getDuration() : 100) + 15))
                                .priceBase(BigDecimal.valueOf(room.getSeatColumns() >= 10 ? 75000 : 90000))
                                .format(movie.getFormat())
                                .subtitleType(movie.getSubtitleType())
                                .build());
                    }
                }
            }
        }
    }

    private void seedCombos() {
        comboRepository.save(Combo.builder()
                .name("Bắp ngọt + Coca").description("1 bắp ngọt size L + 1 Coca 32oz")
                .price(new BigDecimal("65000"))
                .imageUrl(null).active(true).build());
        comboRepository.save(Combo.builder()
                .name("Bắp phô mai + Pepsi").description("1 bắp phô mai size L + 1 Pepsi 32oz")
                .price(new BigDecimal("75000"))
                .imageUrl(null).active(true).build());
        comboRepository.save(Combo.builder()
                .name("Đôi bạn thân").description("2 bắp ngọt + 2 nước bất kỳ")
                .price(new BigDecimal("120000"))
                .imageUrl(null).active(true).build());
    }

    // ================== KHUYEN MAI (crawl) ==================

    private void seedPromotionsFromCrawl() {
        List<Map<String, Object>> promos = loadJson("promotions.json",
                new TypeReference<List<Map<String, Object>>>() {});
        log.info("SEED: {} khuyen mai tu du lieu crawl", promos.size());
        for (Map<String, Object> p : promos) {
            promotionRepository.save(Promotion.builder()
                    .title(str(p.get("title")))
                    .description(str(p.get("description")))
                    .imageUrl(str(p.get("imageUrl")))
                    .active(true)
                    .build());
        }
    }

    // ================== DON VE MAU cho bao cao ==================

    private void seedSampleBookings() {
        User user = userRepository.findByUsernameIgnoreCase("user1").orElseThrow();
        List<Showtime> showtimes = showtimeRepository.findAll();
        if (showtimes.isEmpty()) return;

        int[][] samples = {{0, 2, 1}, {7, 3, 2}, {15, 1, 3}};
        for (int[] sample : samples) {
            Showtime showtime = showtimes.get(sample[0] % showtimes.size());
            List<Seat> seats = seatRepository
                    .findByRoomIdAndActiveTrueOrderByRowLabelAscSeatNumberAsc(showtime.getRoom().getId())
                    .stream()
                    .limit(sample[1])
                    .toList();

            BigDecimal total = BigDecimal.ZERO;
            Booking booking = Booking.builder()
                    .bookingCode("SEED" + System.currentTimeMillis() + sample[0])
                    .user(user)
                    .showtime(showtime)
                    .status(BookingStatus.CONFIRMED)
                    .totalAmount(BigDecimal.ZERO)
                    .expiresAt(LocalDateTime.now().minusDays(sample[2]).plusMinutes(5))
                    .build();
            for (Seat seat : seats) {
                BigDecimal price = com.cinestar.util.SeatPriceCalculator
                        .priceOf(showtime.getPriceBase(), seat.getSeatType());
                total = total.add(price);
                booking.getSeats().add(BookingSeat.builder()
                        .booking(booking).seat(seat).showtime(showtime)
                        .price(price).status(SeatBookingStatus.BOOKED).build());
            }
            booking.setTotalAmount(total);
            booking.setPayment(Payment.builder()
                    .booking(booking)
                    .method(PaymentMethod.VNPAY)
                    .status(PaymentStatus.SUCCESS)
                    .amount(total)
                    .vnpTxnRef(booking.getBookingCode())
                    .vnpResponseCode("00")
                    .vnpBankCode("NCB")
                    .paidAt(LocalDateTime.now().minusDays(sample[2]))
                    .build());
            booking = bookingRepository.save(booking);

            jdbcTemplate.update("UPDATE bookings SET created_at = ? WHERE id = ?",
                    LocalDateTime.now().minusDays(sample[2]).minusHours(2), booking.getId());
        }
    }

    // ================== Helper ==================

    private static String str(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o).trim();
        return (s.isEmpty() || s.equals("null")) ? null : s;
    }

    private static Integer intOf(Object o, Integer fallback) {
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static BigDecimal decimalOf(Object o) {
        try {
            BigDecimal d = new BigDecimal(String.valueOf(o).trim());
            return d.signum() > 0 ? d : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDate parseDate(String iso) {
        try {
            return LocalDate.parse(iso);
        } catch (Exception e) {
            return null;
        }
    }

    private static String slugify(String s) {
        String n = java.text.Normalizer.normalize(s == null ? "" : s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase();
        n = n.replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        return n.length() > 80 ? n.substring(0, 80) : (n.isEmpty() ? "item" : n);
    }

    private static String uniqueSlug(String slug, Set<String> used) {
        String base = slug != null ? slug : "item";
        String candidate = base;
        int i = 2;
        while (used.contains(candidate)) {
            candidate = base + "-" + i++;
        }
        used.add(candidate);
        return candidate;
    }
}
