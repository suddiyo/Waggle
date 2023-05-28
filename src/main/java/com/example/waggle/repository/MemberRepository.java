package com.example.waggle.repository;

import com.example.waggle.domain.member.Member;
import com.example.waggle.domain.team.Team;
import com.example.waggle.domain.team.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
    Optional<Member> findByTeamMembers(TeamMember teamMember);

}