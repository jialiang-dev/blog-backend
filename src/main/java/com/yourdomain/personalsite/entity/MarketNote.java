package com.yourdomain.personalsite.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "market_note")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String title;

    @Column(length = 280)
    private String content;

    @Column(name = "asset_code", length = 20)
    private String assetCode;

    @Column(length = 20)
    @Builder.Default
    private String color = "gray";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
