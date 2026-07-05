package webflux.message.pojo;

public record ChunkUploadResponse(
        String fileHash,
        long uploadedBytes,
        boolean completed,
        String downloadUrl
) {
}
