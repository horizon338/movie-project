package com.example.movie.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * 场次座位（每场次独立生成，杜绝重复售座）
 */
@Getter
@Setter
@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(name = "uk_seat_showtime_row_col",
                columnNames = {"showtime_id", "row_no", "col_no"})
})
public class Seat {

    /** 可售 */
    public static final int STATUS_AVAILABLE = 0;
    /** 已售 */
    public static final int STATUS_SOLD = 1;
    /** 锁定（下单未支付） */
    public static final int STATUS_LOCKED = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @Column(name = "row_no", nullable = false)
    private Integer rowNo;

    @Column(name = "col_no", nullable = false)
    private Integer colNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SeatType seatType = SeatType.NORMAL;

    @Column(nullable = false)
    private Integer status = STATUS_AVAILABLE;
}
