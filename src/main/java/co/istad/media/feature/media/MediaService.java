package co.istad.media.feature.media;

import co.istad.media.feature.media.dto.MediaResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaService {

    List<MediaResponse> uploadMultiple(List<MultipartFile> files);

    /**
     * Upload single file
     * @param file submit from client
     * @return MediaResponse
     */
    MediaResponse uploadSingle(MultipartFile file);

}
