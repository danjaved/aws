package com.learn.aws.controllers;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.learn.aws.utils.Utils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
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
    public ResponseEntity downloadObject(@PathVariable String bucketName, @PathVariable String key){
        S3Object object= s3.getObject(bucketName,key);
        S3ObjectInputStream s3ObjectInputStream= object.getObjectContent();
        FileOutputStream fileOutputStream;
        File file=new File(key);
        Path path = Paths.get(".\\Downloaded\\"+file.getName());
        try {
            fileOutputStream =new FileOutputStream(file);
            byte[] read_buff= new byte[1024];
            int read_len=0;
            while ((read_len=s3ObjectInputStream.read(read_buff))>0){
                fileOutputStream.write(read_buff);
            }
            fileOutputStream.close();
            InputStreamResource inputStreamResource= new InputStreamResource(new FileInputStream(file));
            HttpHeaders header = new HttpHeaders();
            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + key);
            header.add("Cache-Control", "no-cache, no-store, must-revalidate");
            header.add("Pragma", "no-cache");
            header.add("Expires", "0");

            return ResponseEntity.ok()
                    .headers(header)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(inputStreamResource);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @GetMapping("getImage/{bucketName}/{key}")
    public ResponseEntity getObject(@PathVariable String bucketName, @PathVariable String key){
        S3Object object= s3.getObject(bucketName,key);
        S3ObjectInputStream s3ObjectInputStream= object.getObjectContent();
        FileOutputStream fileOutputStream;
        File file=new File(key);
        Path path = Paths.get(".\\Downloaded\\"+file.getName());
        try {
            S3Object o = s3.getObject(bucketName, key);
            S3ObjectInputStream s3is = o.getObjectContent();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }
            s3is.close();
            fos.close();
            return ResponseEntity.ok(new InputStreamResource(new FileInputStream(file)));
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            return null;
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            return null;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}
