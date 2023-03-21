package solid;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class PodTest {

    @Test
    void test() {
        String containerName = "personal-data";
        Assertions.assertEquals("personal-data/", "%s/".formatted(containerName));
    }

    @Test
    void testCreateContainer() {
        String containerName = "personal-data";
        String requestBody = "@prefix ldp: <http://www.w3.org/ns/ldp#>.\n" +
                "@prefix dcterms: <http://purl.org/dc/terms/>.\n" +
                "<> a ldp:Container, ldp:BasicContainer, ldp:Resource;\n" +
                "dcterms:title \"" + containerName + "\";\n" +
                "dcterms:description \"This holds stuff\".";

        final var s = """
                @prefix ldp: <http://www.w3.org/ns/ldp#>.
                @prefix dcterms: <http://purl.org/dc/terms/>.
                <> a ldp:Container, ldp:BasicContainer, ldp:Resource;
                dcterms:title "%s";
                dcterms:description "This holds stuff".""".formatted(containerName);

        Assertions.assertEquals(requestBody, s);

//        HttpClient client = HttpClient.newHttpClient();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("https://solid.interactions.ics.unisg.ch/lukas/"))
//                .header("Content-Type", "text/turtle")
//                .header("Link", "<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"")
//                .header("Slug", "%s/".formatted(containerName))
//                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
//                .build();
//
//        try {
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            System.out.println(response.statusCode());
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}