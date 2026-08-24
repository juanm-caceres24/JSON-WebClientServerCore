package testgame;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class NeuralNetworkClient {
    private final URI predictionUri;
    private final HttpClient httpClient;

    public NeuralNetworkClient(String predictionUrl) {
        this.predictionUri = URI.create(predictionUrl);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    public double[] predict(double[] inputs) throws IOException, InterruptedException {
        JsonArray inputArray = new JsonArray();
        for (double input : inputs) inputArray.add(input);

        JsonObject requestBody = new JsonObject();
        requestBody.add("inputs", inputArray);

        HttpRequest request = HttpRequest.newBuilder(predictionUri)
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Prediction server returned HTTP " + response.statusCode());
        }

        JsonArray outputArray = JsonParser.parseString(response.body())
                .getAsJsonObject().getAsJsonArray("outputs");
        if (outputArray == null || outputArray.size() != 9) {
            throw new IOException("Prediction server returned an invalid output array");
        }

        double[] outputs = new double[outputArray.size()];
        for (int i = 0; i < outputs.length; i++) outputs[i] = outputArray.get(i).getAsDouble();
        return outputs;
    }
}