package com.polyHub.member.repository;

import com.polyHub.member.entity.FaceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaceIdRepository extends JpaRepository<FaceId, Long> {
}
