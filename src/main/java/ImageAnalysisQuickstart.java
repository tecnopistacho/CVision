// Import external libraries and Java utilities, so we can use
import com.microsoft.azure.cognitiveservices.vision.computervision.*;
import com.microsoft.azure.cognitiveservices.vision.computervision.implementation.ComputerVisionImpl;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.*;

// import java.io.*;
// import java.nio.file.Files;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImageAnalysisQuickstart {

    // Use environment variables
    static String key = System.getenv("VISION_KEY");
    static String endpoint = System.getenv("VISION_ENDPOINT");

    public static void main(String[] args) {
        
        System.out.println("\nAzure Cognitive Services Computer Vision - Java Quickstart Sample");

        // Create an authenticated Computer Vision client.
        ComputerVisionClient compVisClient = Authenticate(key, endpoint); 

        // Analyze local and remote images
        AnalyzeRemoteImage(compVisClient);
        ReadRemoteImage(compVisClient);

    }

    public static ComputerVisionClient Authenticate(String key, String endpoint){
        return ComputerVisionManager.authenticate(key).withEndpoint(endpoint);
    }


    public static void AnalyzeRemoteImage(ComputerVisionClient compVisClient) {
        /* Analyze an image from a URL:
         Set a string variable equal to the path of a remote image. */
        String pathToRemoteImage = "https://raw.githubusercontent.com/tecnopistacho/CVision/refs/heads/main/Cvision_test2.jpg";
        
        // This list defines the features to be extracted from the image.
        List<VisualFeatureTypes> featuresToExtractFromRemoteImage = new ArrayList<>();
        featuresToExtractFromRemoteImage.add(VisualFeatureTypes.TAGS);

        System.out.println("\n\nAnalyzing an image from a URL ...");

        try {
            // Call the Computer Vision service and tell it to analyze the loaded image.
            ImageAnalysis analysis = compVisClient.computerVision().analyzeImage().withUrl(pathToRemoteImage)
                    .withVisualFeatures(featuresToExtractFromRemoteImage).execute();


            // Display image tags and confidence values.
            System.out.println("\nTags: ");
            for (ImageTag tag : analysis.tags()) {
                System.out.printf("\'%s\' with confidence %f\n", tag.name(), tag.confidence());
            }
        }

        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


    // END - Analyze an image from a URL.

    // Read text from an image from a URL

    public static void ReadRemoteImage(ComputerVisionClient compVisClient) {
    String remoteImageUrl = "https://raw.githubusercontent.com/tecnopistacho/CVision/refs/heads/main/Cvision_test2.jpg";

    System.out.println("\nReading text from remote image...");

    try {
        // Create optional parameters
        ReadOptionalParameter options = new ReadOptionalParameter();
        options.withLanguage(OcrDetectionLanguage.EN);

        // Start read operation using the REST clinet
        ReadHeaders responseHeader = ((ComputerVisionImpl) compVisClient.computerVision())
            .readWithServiceResponseAsync(remoteImageUrl, options)
            .toBlocking()
            .single()
            .headers();


        // Get the operation ID from the result
        String operationLocation = responseHeader.operationLocation();
        String operationId = operationLocation.substring(operationLocation.lastIndexOf("/") + 1);
        UUID operationUUID = UUID.fromString(operationId);

        // Poll for the result
        ReadOperationResult results;
        int maxRetries = 10;
        int retryCount = 0;
        do {
            Thread.sleep(1000);
            results = compVisClient.computerVision().getReadResult(operationUUID);
            retryCount++;
        } while ((results.status() == OperationStatusCodes.RUNNING ||
                  results.status() == OperationStatusCodes.NOT_STARTED) && retryCount < maxRetries);

        // Display the results
        if (results.status() == OperationStatusCodes.SUCCEEDED) {
            for (ReadResult pageResult : results.analyzeResult().readResults()) {
                for (Line line : pageResult.lines()) {
                    System.out.println(line.text());
                }
            }
        } else {
            System.out.println("Text recognition failed with status: " + results.status());
        }

    } catch (Exception e) {
        System.out.println("Error reading image text: " + e.getMessage());
        e.printStackTrace();
    }
}

}