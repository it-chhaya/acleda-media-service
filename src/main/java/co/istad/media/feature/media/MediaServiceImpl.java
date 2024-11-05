package co.istad.media.feature.media;

import co.istad.media.domain.Media;
import co.istad.media.feature.media.dto.MediaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    @Value("${storage.server-path}")
    private String serverPath;


    @Override
    public Resource downloadMediaByName(String name) {

        Media media = mediaRepository
                .findByName(name)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Media not found"
                        ));

        String folderName = getFolderName(media.getContentType());

        Path path = Paths.get(serverPath + folderName + "/" + media.getName());
        try {
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Media file not found"
                );
            }

            return resource;
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Media file load failed"
            );
        }
    }


    @Override
    public MediaResponse findMediaByName(String name) {

        Media media = mediaRepository
                .findByName(name)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Media not found"
                        ));

        return mediaMapper.toMediaResponse(media, getFolderName(media.getContentType()));
    }


    @Override
    public List<MediaResponse> uploadMultiple(List<MultipartFile> files) {

        List<MediaResponse> mediaResponseList = new ArrayList<>();

        files.forEach(f -> mediaResponseList.add(uploadSingle(f)));

        return mediaResponseList;
    }


    @Override
    public MediaResponse uploadSingle(MultipartFile file) {

        // Extract extension
        // cloud.png
        // png
        int lastIndexOfDot = file.getOriginalFilename()
                .lastIndexOf(".");
        String extension = file
                .getOriginalFilename()
                .substring(lastIndexOfDot + 1);

        // Generate media file
        String mediaName = UUID.randomUUID() + "." + extension;

        String folderName = getFolderName(Objects.requireNonNull(file.getContentType()));

        // Create path object
        Path path = Path.of(serverPath + folderName + "/" + mediaName);

        try {
            Files.copy(file.getInputStream(), path);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "File copy failed"
            );
        }

        Media media = new Media();
        media.setName(mediaName);
        media.setContentType(file.getContentType());
        media.setSize(file.getSize());
        media.setExtension(extension);
        media.setUploadedAt(LocalDateTime.now());
        media.setUploadedBy("admin");
        media.setIsDeleted(false);

        media = mediaRepository.save(media);

        return mediaMapper.toMediaResponse(media, folderName);
    }


    // Reusable logic for get folder name based on the standard content type
    private String getFolderName(String fullContentType) {
        String contentType = fullContentType.split("/")[0];
        return switch (contentType) {
            case "image" -> "image";
            case "video" -> "video";
            case "application" -> "report";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file format!");
        };
    }

}
