package uk.gov.hmcts.reform.roleassignmentbatch.task;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class CcdToRasSetupTasklet implements Tasklet {

    File downloadedFile;
    String connectionString;
    BlobServiceClient blobServiceClient;
    BlobContainerClient containerClient;
    BlobClient blobClient;

    @Autowired
    public CcdToRasSetupTasklet (String fileName,
                                 String filePath, String containerName, String accountName, String accountKey) {

        downloadedFile = new File(filePath + fileName);

        connectionString = "DefaultEndpointsProtocol=https;"
                + "AccountName=" + accountName + ";"
                + "AccountKey=" + accountKey + ";"
                + "EndpointSuffix=core.windows.net";

        blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();

        containerClient = blobServiceClient.getBlobContainerClient(containerName);

        blobClient = containerClient.getBlobClient(fileName);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        if(fileDoesNotExistUpToDate(downloadedFile, blobClient)) {
            try {
                blobClient.downloadToFile(downloadedFile.getAbsolutePath(), true);

                if (blobAndLocalFileSizeAreDifferent(blobClient, downloadedFile)) {
                    throw new RuntimeException("Downloaded file does not match the blob file. Try again");
                }

            } catch (Exception e) {
                System.out.println("EXCEPTION! FAILED!");
            }
        }

        return RepeatStatus.FINISHED;
    }

    public boolean fileDoesNotExistUpToDate(File downloadedFile, BlobClient blobClient) {
        boolean notUpToDate = false;
        if (fileDoesNotExistLocal(downloadedFile) || blobAndLocalFileSizeAreDifferent(blobClient, downloadedFile)) {
            notUpToDate = true;
        }
        return notUpToDate;
    }

    public boolean fileDoesNotExistLocal(File file) {
        return !file.exists();
    }

    public boolean blobAndLocalFileSizeAreDifferent(BlobClient blobClient, File downloadedFile) {
        boolean fileSizeMismatch = true;
        if (getBlobFileSize(blobClient) == getLocalFileSize(downloadedFile)) {
            fileSizeMismatch = false;
        }
        return fileSizeMismatch;
    }

    public long getBlobFileSize(BlobClient blobClient) {
        return blobClient.getProperties().getBlobSize();
    }

    public long getLocalFileSize(File downloadedFile) {
        return downloadedFile.length();
    }

}
