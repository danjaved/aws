package com.learn.aws.controllers;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("buckets")
public class BucketsController {
    final AmazonS3 s3;
    BucketsController(){
        s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
    }

    @GetMapping("all")
    public List<String> getBucketNames(){

        List<Bucket> buckets = s3.listBuckets();
        return buckets.stream().map(bucket -> bucket.getName()).collect(Collectors.toList());
    }
    @PostMapping("add/{bucketName}")
    public ResponseEntity<String> addNewBucket(@PathVariable String bucketName){
        Bucket bucket;
        try{
            if(s3.doesBucketExistV2(bucketName))
                return new ResponseEntity<>("Bucket Already Exists", HttpStatus.CONFLICT);
            else
            {
                CreateBucketRequest request = new CreateBucketRequest(bucketName);
                String abc=s3.getRegionName();
                bucket = s3.createBucket(bucketName);
                int a=0;
                a=a+5;
            }

            return new ResponseEntity<>("Success", HttpStatus.CREATED);
        }
        catch(Exception e){
            return new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
