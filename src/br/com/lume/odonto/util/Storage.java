package br.com.lume.odonto.util;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;


public class Storage {
    private static final String AZURE_STORAGE_ACCOUNT = "stgintelidente";

    private static final String AZURE_STORAGE_ACCESS_KEY = "sKLWXKxHeMIRdwsyvukwh7+0rnU8cmUeVu9kSatySRlYPIRoc+Datf/qnk/vBbp0DSKnpAEh9PI42mHr6OjU1g==";

    private static final String AZURE_CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=" + AZURE_STORAGE_ACCOUNT + ";AccountKey=" + AZURE_STORAGE_ACCESS_KEY ;

    public static final String AZURE_PATH = "https://stgintelidente.blob.core.windows.net";
    
    private static Storage instance;

    private static CloudStorageAccount storageAccount;

    private static CloudBlobClient blobClient;

    public static final String AZURE_PATH_ANEXO_EXAME = "/anexo-exame";

    private Storage() {
        init();
    }

    public static Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }

    public synchronized String gravar(InputStream in, String containerName, String fileName) throws Exception {
        CloudBlockBlob blob = getContainer(containerName).getBlockBlobReference(fileName);
        blob.upload(in, -1);
        return containerName + "/" + fileName;
    }
    
    public synchronized String gravar(byte[] bytes, String containerName, String fileName) throws Exception {
        return gravar(new ByteArrayInputStream(bytes), containerName, fileName);
    }

    public synchronized String download(String containerName, String fileName) throws Exception {
        try {
            CloudBlockBlob blob = getContainer(containerName).getBlockBlobReference(fileName);
            return blob.downloadText();
        }catch (StorageException e) {
        }
        return null;
    }

    public List<CloudBlob> listBlobs(String containerName) {
        try {
            CloudBlobContainer container = getContainer(containerName);
            List<CloudBlob> list = new ArrayList<>();
            container.listBlobs().forEach(l -> list.add((CloudBlob) l));
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private CloudBlobContainer getContainer(String containerName) {
        try {
            return blobClient.getContainerReference(containerName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void init() {
        try {
            storageAccount = CloudStorageAccount.parse(AZURE_CONNECTION_STRING);
            blobClient = storageAccount.createCloudBlobClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}