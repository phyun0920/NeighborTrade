package com.study.neighbortrade.domain.location;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "neighborhood")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Neighborhood {

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String sido;

    @Column(nullable = false, length = 30)
    private String sigungu;

    @Column(nullable = false, length = 50)
    private String emdName;

    @Column(nullable = false, unique = true, length = 120)
    private String displayName;

    @Column(nullable = false)
    private double centerLatitude;

    @Column(nullable = false)
    private double centerLongitude;

    @Column(nullable = false)
    private int verifyRadiusMeters;
}
