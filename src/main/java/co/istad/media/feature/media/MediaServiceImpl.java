package co.istad.media.feature.media;

import co.istad.media.domain.Media;
import co.istad.media.feature.media.dto.MediaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    @Value("${storage.server-path}")
    private String serverPath;

    @Value("${storage.base-uri}")
    private String baseUri;

    @Override
    public List<MediaResponse> uploadMultiple(List<MultipartFile> files) {

        List<MediaResponse> mediaResponseList = new ArrayList<>();

        files.forEach(f -> mediaResponseList.add(uploadSingle(f)));

        return mediaResponseList;
    }


    @Override
    public MediaResponse uploadSingle(MultipartFile file) {

        // TODO

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

        String contentType = file.getContentType().split("/")[0];
        System.out.println(contentType);
        String folderName = switch (contentType) {
            case "image" -> "image";
            case "video" -> "video";
            case "application" -> "report";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file format!");
        };

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
        media.setName(folderName + "/" + mediaName);
        media.setContentType(file.getContentType());
        media.setSize(file.getSize());
        media.setExtension(extension);
        media.setUploadedAt(LocalDateTime.now());
        media.setUploadedBy("admin");
        media.setIsDeleted(false);

        media = mediaRepository.save(media);

        return mediaMapper.toMediaResponse(media);
    }

}
