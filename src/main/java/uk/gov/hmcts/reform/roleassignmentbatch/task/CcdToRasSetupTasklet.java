package uk.gov.hmcts.reform.roleassignmentbatch.task;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;

public class CcdToRasSetupTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        RepeatStatus stepStatus;

        File downloadedFile = new File("src/main/resources/book2.csv");

        if(!downloadedFile.exists()) {
            try {
                String connectStr = "";
                BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectStr).buildClient();

                BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("amccdras");

                BlobClient blobClient = containerClient.getBlobClient("book2.csv");


                System.out.println("\nDownloading blob to\n\t " + downloadedFile.getAbsolutePath());

                blobClient.downloadToFile(downloadedFile.getAbsolutePath(), true);
                System.out.println("Download Completed");
            } catch (Exception e) {
                System.out.println("FAILED");
            }
        }

        return RepeatStatus.FINISHED;
    }
}
