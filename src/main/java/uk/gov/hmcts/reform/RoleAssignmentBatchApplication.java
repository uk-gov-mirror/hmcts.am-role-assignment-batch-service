package uk.gov.hmcts.reform;

import java.io.File;
import java.io.FileWriter;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableBatchProcessing
@SuppressWarnings("HideUtilityClassConstructor")
@Slf4j
public class RoleAssignmentBatchApplication {

    public static void main(String[] args) throws Exception {

        String connectStr = System.getenv("BLOB_STORAGE_CONN_STRING");
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectStr).buildClient();

        // Create the container and return a container client object
        // BlobContainerClient containerClient = blobServiceClient.createBlobContainer(containerName);
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("amccdras");
        //amccdras
        for (BlobItem blobItem : containerClient.listBlobs()) {
            System.out.println("\t" + blobItem.getName());
        }
        String localPath = "";
        String fileName = "quickstart" + java.util.UUID.randomUUID() + ".txt";
        File localFile = new File(localPath + fileName);


// Write text to the file
        FileWriter writer = new FileWriter(localPath + fileName, true);
        writer.write("Hello, World!");
        writer.close();

// Get a reference to a blob
        BlobClient blobClient = containerClient.getBlobClient(fileName);

        System.out.println("\nUploading to Blob storage as blob:\n\t" + blobClient.getBlobUrl());

// Upload the blob
        blobClient.uploadFromFile(localPath + fileName, true);
        blobClient = containerClient.getBlobClient("book2.csv");
        File downloadedFile = new File("src/main/resources/book2.csv");

        System.out.println("\nDownloading blob to\n\t " + downloadedFile.getAbsolutePath());

        blobClient.downloadToFile(downloadedFile.getAbsolutePath(), true);
        System.out.println("Download Completed");
        ApplicationContext context = SpringApplication.run(RoleAssignmentBatchApplication.class, args);
        //Sleep added to allow app-insights to flush the logs
        Thread.sleep(1000 * 8);
        int exitCode = SpringApplication.exit(context);
        String exitCodeLog = String.format("RoleAssignmentBatchApplication Application exiting with exit code %s",
                                           exitCode);
        log.info(exitCodeLog);
        System.exit(exitCode);
    }
}
