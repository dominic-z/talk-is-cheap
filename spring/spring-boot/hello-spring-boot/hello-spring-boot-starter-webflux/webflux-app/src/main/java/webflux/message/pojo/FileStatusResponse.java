package webflux.message.pojo;

public record FileStatusResponse(
        String fileHash,
        String fileName,
        long size,
        boolean completed,
        long uploadedBytes,
        String downloadUrl
) {
}
