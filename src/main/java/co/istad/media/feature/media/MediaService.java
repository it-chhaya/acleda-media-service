package co.istad.media.feature.media;

import co.istad.media.feature.media.dto.MediaResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaService {


    Resource downloadMediaByName(String name);


    MediaResponse findMediaByName(String name);


    List<MediaResponse> uploadMultiple(List<MultipartFile> files);

    /**
     * Upload single file
     * @param file submit from client
     * @return MediaResponse
     */
    MediaResponse uploadSingle(MultipartFile file);

}
