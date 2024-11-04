package co.istad.media.feature.media.dto;

public record MediaResponse(
        String name,
        String contentType,
        Long size,
        String extension,
        String uri // baseUri + folderName + name
) {
}
