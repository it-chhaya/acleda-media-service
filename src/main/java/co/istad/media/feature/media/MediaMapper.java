package co.istad.media.feature.media;

import co.istad.media.domain.Media;
import co.istad.media.feature.media.dto.MediaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Value;

@Mapper(componentModel = "spring")
public abstract class MediaMapper {

    @Value("${storage.base-uri}")
    private String baseUri;

    @Mapping(source = "name", target = "uri", qualifiedByName = "mapUri")
    abstract MediaResponse toMediaResponse(Media media);

    @Named("mapUri")
    String mapUri(String name) {
        return baseUri + name;
    }

}
