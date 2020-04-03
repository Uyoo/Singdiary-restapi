package com.singdiary.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.singdiary.common.AwsCloudProperties;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@Service
@NoArgsConstructor
public class S3Service {

    private AmazonS3 s3Client;

    @Autowired
    AwsCloudProperties awsCloudProperties;

    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(awsCloudProperties.getAccessKey(), awsCloudProperties.getSecretKey());

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(awsCloudProperties.getRegion())
                .build();
    }

    public String upload(String currentFilePath, MultipartFile file) throws IOException {
        //String fileName = file.getOriginalFilename();

        // 고유한 key 값을 갖기위해 현재 시간을 postfix로 붙여줌
        SimpleDateFormat date = new SimpleDateFormat("yyyymmddHHmmss");
        String fileName = file.getOriginalFilename() + "-" + date.format(new Date());

        // key가 존재하면 기존 파일은 삭제
        if (!currentFilePath.equals("") && currentFilePath != null) {
            boolean isExistObject = s3Client.doesObjectExist(awsCloudProperties.getBucket(), currentFilePath);

            if (isExistObject == true) {
                s3Client.deleteObject(awsCloudProperties.getBucket(), currentFilePath);
            }
        }

        s3Client.putObject(new PutObjectRequest(awsCloudProperties.getBucket(), fileName, file.getInputStream(), null)
                .withCannedAcl(CannedAccessControlList.PublicRead));

//        String imgUrl = s3Client.getUrl(awsCloudProperties.getBucket(), fileName).toString();
//
//        HashMap<String, String> hashMap = new HashMap<>();
//        hashMap.put("fileName", fileName);
//        hashMap.put("imgUrl", imgUrl);

        return fileName;
    }
}
