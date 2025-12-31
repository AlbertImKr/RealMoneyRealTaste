package com.albert.realmoneyrealtaste.adapter.infrastructure.oci

import com.oracle.bmc.Region
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider
import com.oracle.bmc.objectstorage.ObjectStorageClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.io.ByteArrayInputStream

@Profile("prod")
@Configuration
@ConfigurationProperties(prefix = "oci.objectstorage")
class OciObjectStorageConfig {
    lateinit var tenantId: String
    lateinit var userId: String
    lateinit var fingerprint: String
    lateinit var privateKey: String
    lateinit var region: String
    lateinit var namespace: String
    lateinit var bucketName: String

    @Bean
    fun objectStorageClient(): ObjectStorageClient {
        val provider = SimpleAuthenticationDetailsProvider.builder()
            .tenantId(tenantId)
            .userId(userId)
            .fingerprint(fingerprint)
            .privateKeySupplier { ByteArrayInputStream(privateKey.replace("\\n", "\n").toByteArray()) }
            .build()

        return ObjectStorageClient.builder()
            .region(Region.fromRegionId(region))
            .build(provider)
    }
}
