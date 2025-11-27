package com.albert.realmoneyrealtaste.domain.image

import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import com.albert.realmoneyrealtaste.domain.image.command.ImageCreateCommand
import com.albert.realmoneyrealtaste.domain.image.value.FileKey
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "images", indexes = [
        Index(name = "idx_uploaded_by", columnList = "uploaded_by"),
        Index(name = "idx_image_type", columnList = "image_type")
    ]
)
class Image protected constructor(
    fileKey: FileKey,
    uploadedBy: Long,
    imageType: ImageType,
    isDeleted: Boolean,
) : BaseEntity() {

    companion object {
        fun create(
            command: ImageCreateCommand,
        ): Image {
            return Image(
                fileKey = command.fileKey,
                uploadedBy = command.uploadedBy,
                imageType = command.imageType,
                isDeleted = false,
            )
        }
    }

    @Embedded
    var fileKey: FileKey = fileKey
        protected set

    @Column(name = "uploaded_by", nullable = false)
    var uploadedBy: Long = uploadedBy
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false)
    var imageType: ImageType = imageType
        protected set

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = isDeleted
        protected set

    fun canAccess(userId: Long): Boolean {
        return uploadedBy == userId
    }

    fun markAsDeleted() {
        isDeleted = true
    }

    fun isProfileImage(): Boolean = imageType == ImageType.PROFILE_IMAGE
    fun isPostImage(): Boolean = imageType == ImageType.POST_IMAGE
    fun isThumbnail(): Boolean = imageType == ImageType.THUMBNAIL
}
