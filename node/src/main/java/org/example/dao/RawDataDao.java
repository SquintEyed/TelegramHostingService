package org.example.dao;

import org.example.entity.RawData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawDataDao extends JpaRepository<RawData, Long> {
}
