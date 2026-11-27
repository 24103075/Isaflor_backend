
package cl.isaflor.isaflor.service;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImgBBService {

    @Value("${imgbb.api.url}")
    private String apiUrl;

    @Value("${imgbb.api.key}")
    private String apiKey;

    private final RestClient restClient;

    public ImgBBService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public String subirImagen(MultipartFile archivo) {

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar una imagen");
        }

        if (archivo.getContentType() == null ||
                !archivo.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }

        if (archivo.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("La imagen no puede superar los 5 MB");
        }

        try {
            String imagenBase64 = Base64.getEncoder()
                    .encodeToString(archivo.getBytes());

            MultiValueMap<String, String> parametros = new LinkedMultiValueMap<>();
            parametros.add("key", apiKey);
            parametros.add("image", imagenBase64);

            if (archivo.getOriginalFilename() != null) {
                parametros.add("name", archivo.getOriginalFilename());
            }

            ImgBBRespuesta respuesta = restClient.post()
                    .uri(apiUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(parametros)
                    .retrieve()
                    .body(ImgBBRespuesta.class);

            if (respuesta == null || respuesta.data() == null ||
                    respuesta.data().url() == null) {
                throw new RuntimeException("ImgBB no devolvio la URL de la imagen");
            }

            return respuesta.data().url();

        } catch (IOException e) {
            throw new RuntimeException("Error al leer la imagen", e);
        }
    }

    public record ImgBBRespuesta(
            boolean success,
            ImgBBData data) {
    }

    public record ImgBBData(
            String id,
            String url,
            String display_url,
            String delete_url) {
    }
}
