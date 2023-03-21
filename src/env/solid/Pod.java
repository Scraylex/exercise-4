package solid;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * A CArtAgO artifact that agent can use to interact with LDP containers in a Solid pod.
 */
public class Pod extends Artifact {

    private String podURL; // the location of the Solid pod 

    /**
     * Method called by CArtAgO to initialize the artifact.
     *
     * @param podURL The location of a Solid pod
     */
    public void init(String podURL) {
        this.podURL = podURL;
        log("Pod artifact initialized for: " + this.podURL);
    }

    /**
     * CArtAgO operation for creating a Linked Data Platform container in the Solid pod
     *
     * @param containerName The name of the container to be created
     */
    @OPERATION
    public void createContainer(String containerName) {
        final var s = """
                @prefix ldp: <http://www.w3.org/ns/ldp#>.
                @prefix dcterms: <http://purl.org/dc/terms/>.
                <> a ldp:Container, ldp:BasicContainer, ldp:Resource;
                dcterms:title "%s";
                dcterms:description "This holds stuff".""".formatted(containerName);
        final var containerUri = URI.create("%s/%s/".formatted(podURL, containerName));
        if (checkResourceAlreadyExists(containerUri)) {
            log("Resource: %s exists".formatted(containerUri.toString()));
            return;
        }
        final var request = HttpRequest.newBuilder(URI.create("%s/".formatted(podURL)))
                .POST(HttpRequest.BodyPublishers.ofString(s))
                .header("Content-Type", "text/turtle")
                .header("Slug", "%s/".formatted(containerName))
                .header("Link", "<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"")
                .build();
        final var httpClient = HttpClient.newHttpClient();
        try {
            final var httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            log(String.valueOf(httpResponse.statusCode()));
            log(httpResponse.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * CArtAgO operation for publishing data within a .txt file in a Linked Data Platform container of the Solid pod
     *
     * @param containerName The name of the container where the .txt file resource will be created
     * @param fileName      The name of the .txt file resource to be created in the container
     * @param data          An array of Object data that will be stored in the .txt file
     */
    @OPERATION
    public void publishData(String containerName, String fileName, Object[] data) {
        final var s = createStringFromArray(data);
        final var resourceUri = URI.create("%s/%s/%s".formatted(podURL, containerName, fileName));
        final var httpClient = HttpClient.newHttpClient();
        try {
            if (checkResourceAlreadyExists(resourceUri)) {
                final var request = HttpRequest.newBuilder()
                        .uri(resourceUri)
                        .header("Content-Type", "text/plain")
                        .PUT(HttpRequest.BodyPublishers.ofString(s))
                        .build();
                final var httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                log(String.valueOf(httpResponse.statusCode()));
                log("Putting: %s on resource %s".formatted(s, resourceUri));
            } else {
                final var containerUri = URI.create("%s/%s/".formatted(podURL, containerName));
                final var request = HttpRequest.newBuilder(containerUri)
                        .header("Slug", fileName)
                        .POST(HttpRequest.BodyPublishers.ofString(s))
                        .header("Content-Type", "text/plain")
                        .build();
                final var httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
                log(String.valueOf(httpResponse.statusCode()));
                log("Posting: %s on resource %s".formatted(s, containerUri));
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * CArtAgO operation for reading data of a .txt file in a Linked Data Platform container of the Solid pod
     *
     * @param containerName The name of the container where the .txt file resource is located
     * @param fileName      The name of the .txt file resource that holds the data to be read
     * @param data          An array whose elements are the data read from the .txt file
     */
    @OPERATION
    public void readData(String containerName, String fileName, OpFeedbackParam<Object[]> data) {
        final var resourceUri = URI.create("%s/%s/%s".formatted(podURL, containerName, fileName));
        final var request = HttpRequest.newBuilder(resourceUri)
                .GET()
                .header("Accept", "text/plain")
                .build();
        try {
            final var httpClient = HttpClient.newHttpClient();
            final var httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            log(String.valueOf(httpResponse.statusCode()));
            log("Read: %s from Resource: %s".formatted(httpResponse.body(), resourceUri));
            Object[] objects = createArrayFromString(httpResponse.body());
            data.set(objects);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method for reading data of a .txt file in a Linked Data Platform container of the Solid pod
     *
     * @param containerName The name of the container where the .txt file resource is located
     * @param fileName      The name of the .txt file resource that holds the data to be read
     * @return An array whose elements are the data read from the .txt file
     */
    public Object[] readData(String containerName, String fileName) {
        final var data = new OpFeedbackParam<Object[]>();
        readData(containerName, fileName, data);
        return data.get();
    }

    /**
     * Method that converts an array of Object instances to a string,
     * e.g. the array ["one", 2, true] is converted to the string "one\n2\ntrue\n"
     *
     * @param array The array to be converted to a string
     * @return A string consisting of the string values of the array elements separated by "\n"
     */
    public static String createStringFromArray(Object[] array) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : array) {
            sb.append(obj.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Method that converts a string to an array of Object instances computed by splitting the given string with delimiter "\n"
     * e.g. the string "one\n2\ntrue\n" is converted to the array ["one", "2", "true"]
     *
     * @param str The string to be converted to an array
     * @return An array consisting of string values that occur by splitting the string around "\n"
     */
    public static Object[] createArrayFromString(String str) {
        return str.split("\n");
    }

    /**
     * CArtAgO operation for updating data of a .txt file in a Linked Data Platform container of the Solid pod
     * The method reads the data currently stored in the .txt file and publishes in the file the old data along with new data
     *
     * @param containerName The name of the container where the .txt file resource is located
     * @param fileName      The name of the .txt file resource that holds the data to be updated
     * @param data          An array whose elements are the new data to be added in the .txt file
     */
    @OPERATION
    public void updateData(String containerName, String fileName, Object[] data) {
        Object[] oldData = readData(containerName, fileName);
        Object[] allData = new Object[oldData.length + data.length];
        System.arraycopy(oldData, 0, allData, 0, oldData.length);
        System.arraycopy(data, 0, allData, oldData.length, data.length);
        publishData(containerName, fileName, allData);
    }

    private Boolean checkResourceAlreadyExists(URI uri) {
        final var httpClient = HttpClient.newHttpClient();
        final var httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        try {
            final var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}
