package com.learn.aws.controllers;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.learn.aws.utils.Utils;
import org.apache.commons.logging.impl.AvalonLogger;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("objects")
public class ObjectsController {
    final AmazonS3 s3;

    ObjectsController() {
        s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
    }

    @PostMapping("upload/{bucketName}")
    public ResponseEntity<String> uploadObject(@PathVariable String bucketName, @RequestBody List<MultipartFile> images) throws IOException {

        images.stream().forEach(image->{
            try(InputStream inputStream=image.getInputStream()){
                String name= image.getOriginalFilename();
                s3.putObject(new PutObjectRequest(bucketName,name,inputStream, new ObjectMetadata()));
            }catch (IOException e){}
        });

        return new ResponseEntity<>("Uploaded Successfully", HttpStatus.CREATED);
    }

    @GetMapping("download/{bucketName}/{key}")
    public ResponseEntity getObject(@PathVariable String bucketName, @PathVariable String key) throws IOException {
        S3Object object= s3.getObject(bucketName,key);
        byte[] byteArray= IOUtils.toByteArray(object.getObjectContent());
        return ResponseEntity.ok(byteArray);
    }

    @GetMapping("listObjectNames/{bucketName}")
    public ResponseEntity getObjectNameList(@PathVariable String bucketName){
       List<String> objectNames=new ArrayList<>();
       s3.listObjectsV2(bucketName).getObjectSummaries().forEach(object->objectNames.add(object.getKey()));
       return ResponseEntity.ok(objectNames);
    }

    @GetMapping("getObjectsLink/{bucketName}/{directoryName}")
    public ResponseEntity getLinks(@PathVariable String bucketName, @PathVariable String directoryName){
       return ResponseEntity.ok( s3.getUrl(bucketName,directoryName));
    }

    @GetMapping("getImages/{bucketName}/{directory}")
    public ResponseEntity getImages(@PathVariable String bucketName,@PathVariable String directory){
        List<byte[]> images=new ArrayList<>();
        List<String> imagesNames=new ArrayList<>();
        s3.listObjects(bucketName,directory).getObjectSummaries().forEach(object->imagesNames.add(object.getKey()));
        imagesNames.forEach(imageName->{
            try {
                images.add(IOUtils.toByteArray(s3.getObject(bucketName,imageName).getObjectContent()));
            } catch (IOException e) {

            }
        });
        return ResponseEntity.ok(images);
    }
}
