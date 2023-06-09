package com.example.waggle.domain.team;

import com.example.waggle.dto.member.ScheduleDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Schedule {
    @Id @GeneratedValue
    @Column(name = "schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private String title;

    private String description;

    private LocalDateTime scheduleTime;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ScheduleMember> scheduleMembers = new ArrayList<>();


    public void update(ScheduleDto scheduleDto, List<ScheduleMember> scheduleMembers) {
        this.title = scheduleDto.getTitle();
        this.description = scheduleDto.getDescription();
        this.scheduleTime = scheduleDto.getScheduleTime();
        this.scheduleMembers = scheduleMembers;
    }

}
