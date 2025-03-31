package com.Oracle.FileService.service;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest;
import com.oracle.bmc.objectstorage.requests.ListBucketsRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;
import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse;
import com.oracle.bmc.objectstorage.responses.ListBucketsResponse;
import com.oracle.bmc.objectstorage.responses.PutObjectResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

@Service
public class ObjectStorageService {

    private final ObjectStorage objectStorage;
    private final String namespace;
    private final String bucketName;
    private final String regionId;
    private final String tenancyId;
    private final String compartmentId;

    public ObjectStorageService(
            @Value("${oci.tenancy-id}") String tenancyId,
            @Value("${oci.user-id}") String userId,
            @Value("${oci.fingerprint}") String fingerprint,
            @Value("${oci.private-key-path}") String privateKeyPath,
            @Value("${oci.region}") String region,
            @Value("${oci.namespace}") String namespace,
            @Value("${oci.bucket-name}") String bucketName
    ) {
        try {

            Region regionobj = Region.fromRegionCode(region);

            SimpleAuthenticationDetailsProvider provider = SimpleAuthenticationDetailsProvider.builder()
                    .tenantId(tenancyId)
                    .userId(userId)
                    .fingerprint(fingerprint)
                    .privateKeySupplier(() -> {
                        try {
                            return new FileInputStream(privateKeyPath);
                        } catch (Exception e) {
                            throw new RuntimeException("Error reading private key", e);
                        }
                    })
                    .region(Region.fromRegionCodeOrId(region))
                    .build();
            this.objectStorage = ObjectStorageClient.builder().build(provider);
            this.namespace = namespace;
            this.bucketName = bucketName;
            this.regionId = region;
            this.tenancyId = tenancyId;
            this.compartmentId = tenancyId;

        } catch (Exception e) {
            throw new RuntimeException("Error creating Object Storage client" + e.getMessage(), e);
        }
    }

    public void checkBucketExists() {
        try {
            // Get a list of buckets in the namespace
            ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder()
                    .namespaceName(namespace)
                    .compartmentId(compartmentId)  // Ensure you pass the correct compartment ID
                    .build();

            ListBucketsResponse listBucketsResponse = objectStorage.listBuckets(listBucketsRequest);

            // Check if the bucket exists
            System.out.println("Buckets in the namespace " + namespace + ":");
            listBucketsResponse.getItems().forEach(bucket -> System.out.println(bucket.getName()));
            boolean bucketExists = listBucketsResponse.getItems().stream().anyMatch(bucket -> bucket.getName().equals(bucketName));

            if (!bucketExists) {
                throw new RuntimeException("Bucket " + bucketName + " does not exist in the namespace " + namespace);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error checking bucket existence: " + e.getMessage(), e);
        }
    }


    public String testConnection(){
        try{
            GetNamespaceRequest request = GetNamespaceRequest.builder().build();
            GetNamespaceResponse response = objectStorage.getNamespace(request);
            checkBucketExists();

            return "Successfully connected to Object Storage: " + response.getValue();

        }catch(Exception e){
            throw new RuntimeException("Error connecting to Object Storage" + e.getMessage(),e);
        }
    }

    public String uploadFile(InputStream fileInputStream, String fileName, long fileSize) throws Exception {

        String contentType = java.nio.file.Files.probeContentType(new File(fileName).toPath());
        if(contentType == null){
            contentType = "application/octet-stream";
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .objectName(fileName)
                .contentType(contentType)
                .contentDisposition("inline")
                .putObjectBody(fileInputStream)
                .contentLength(fileSize)
                .build();

        PutObjectResponse response = objectStorage.putObject(request);

        return createPreuathenticatedRequest(fileName);

    }

    public String createPreuathenticatedRequest(String fileName){

        Date expirationTime = new Date(System.currentTimeMillis() + 365L * 2460 * 60 * 1000);

        CreatePreauthenticatedRequestDetails parDetails = CreatePreauthenticatedRequestDetails.builder()
                .name("Preauthenticated Request for " + fileName)
                .objectName(fileName)
                .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectRead)
                .timeExpires(expirationTime)
                .build();

        CreatePreauthenticatedRequestRequest parRequest = CreatePreauthenticatedRequestRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .createPreauthenticatedRequestDetails(parDetails)
                .build();

        CreatePreauthenticatedRequestResponse parResponse = objectStorage.createPreauthenticatedRequest(parRequest);

        String parAccessURL = parResponse.getPreauthenticatedRequest().getAccessUri();

        String parUrl = "https://objectstorage." + regionId + ".oraclecloud.com" + parAccessURL;

        return parUrl;

    }

}
