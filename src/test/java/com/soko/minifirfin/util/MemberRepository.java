package com.soko.minifirfin.util;

import com.soko.minifirfin.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
