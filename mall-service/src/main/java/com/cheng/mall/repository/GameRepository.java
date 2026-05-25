package com.cheng.mall.repository;

import com.cheng.mall.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    Page<Game> findByIsOnSaleTrue(Pageable pageable);

    List<Game> findByIsFeaturedTrueAndIsOnSaleTrue();

    @Query("SELECT g FROM Game g JOIN GameCategoryMapping gcm ON g.id = gcm.id.gameId WHERE gcm.id.categoryId = :categoryId AND g.isOnSale = true")
    List<Game> findByCategoryId(@Param("categoryId") Integer categoryId);

    @Query("SELECT g FROM Game g WHERE g.title LIKE %:keyword% AND g.isOnSale = true")
    List<Game> searchByTitle(@Param("keyword") String keyword);

    Page<Game> findByIsOnSaleTrueOrderByCurrentPriceAsc(Pageable pageable);

    Page<Game> findByIsOnSaleTrueOrderByCurrentPriceDesc(Pageable pageable);

    Page<Game> findByIsOnSaleTrueOrderByRatingDesc(Pageable pageable);

    Page<Game> findByIsOnSaleTrueOrderByTotalSalesDesc(Pageable pageable);

    @Query("SELECT g FROM Game g WHERE g.discountRate > 0 AND g.discountStart <= CURRENT_TIMESTAMP AND g.discountEnd >= CURRENT_TIMESTAMP AND g.isOnSale = true ORDER BY g.discountRate DESC")
    List<Game> findCurrentDiscountedGames();

    @Query(value = "SELECT * FROM games WHERE discount_rate > 0 AND is_on_sale = 1 AND discount_start >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) ORDER BY discount_rate DESC LIMIT 10", nativeQuery = true)
    List<Game> findThisWeekDiscountedGames();


    @Query("SELECT g FROM Game g WHERE g.discountRate > 0 AND g.isOnSale = true ORDER BY g.discountRate DESC")
    List<Game> findAllDiscountedGames();

    @Query("SELECT g FROM Game g WHERE g.discountRate > 0 AND g.discountEnd >= CURRENT_TIMESTAMP AND g.isOnSale = true ORDER BY g.discountRate DESC")
    List<Game> findTopDiscountedGames(Pageable pageable);
}