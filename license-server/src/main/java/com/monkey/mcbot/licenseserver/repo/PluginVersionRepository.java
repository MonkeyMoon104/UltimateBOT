package com.monkey.mcbot.licenseserver.repo;

import com.monkey.mcbot.licenseserver.domain.PluginVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PluginVersionRepository extends JpaRepository<PluginVersionEntity, UUID> {

    Optional<PluginVersionEntity> findByProductCodeAndVersionName(String productCode, String versionName);

    List<PluginVersionEntity> findByProductCodeOrderByVersionNameAsc(String productCode);

    Optional<PluginVersionEntity> findFirstByProductCodeAndLatestTrue(String productCode);

    @Modifying
    @Query("update PluginVersionEntity p set p.latest = false where p.productCode = :productCode")
    void clearLatestForProduct(@Param("productCode") String productCode);
}
