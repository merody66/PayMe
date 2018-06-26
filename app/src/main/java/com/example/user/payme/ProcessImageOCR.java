package com.example.user.payme;
// Imports the Google Cloud client library

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.util.ArrayList;
import java.util.List;

public class ProcessImageOCR {
    public static void main(String[] args) {
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
            // The path to the image file to annotate
            String fileName = "https://storage.googleapis.com/curious-song-140803.appspot.com/sample_patbingsoo.jpg";

            // Encode the image.
            Bitmap bm = BitmapFactory.decodeFile(fileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            ByteString imgBytes = ByteString.copyFrom(data);

            // Builds the image annotation request
            List<AnnotateImageRequest> requests = new ArrayList<>();
            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Type.LABEL_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();
            requests.add(request);

            // Performs label detection on the image file
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.printf("Error: %s\n", res.getError().getMessage());
                    return;
                }

                for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                    annotation.getAllFields();
                    System.out.println("annotation"+annotation.getAllFields());
//                            .forEach((k, v) ->
//                            System.out5.printf("%s : %s\n", k, v.toString()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
