package com.yourdomain.personalsite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_holding")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_code", nullable = false, length = 20, unique = true)
    private String stockCode;

    @Column(name = "stock_name", nullable = false, length = 100)
    private String stockName;

    @Column(name = "hold_shares", nullable = false, precision = 15, scale = 2)
    private BigDecimal holdShares;

    @Column(name = "cost_price", nullable = false, precision = 10, scale = 4)
    private BigDecimal costPrice;
}
