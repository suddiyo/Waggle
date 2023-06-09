package com.example.waggle.domain.team;


import com.example.waggle.domain.member.Member;
import com.example.waggle.dto.member.ScheduleDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ScheduleMember {

    @Id
    @GeneratedValue
    @Column(name = "schedule_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 연관관계 편의 메서드
    public void addScheduleMember(Schedule schedule, Member member) {
        this.schedule = schedule;
        this.member = member;
        schedule.getScheduleMembers().add(this);
    }

    public void removeTeam() {
        if (schedule != null) {
            schedule.getScheduleMembers().remove(this);
            schedule = null;
            member = null;
        }
    }
}
