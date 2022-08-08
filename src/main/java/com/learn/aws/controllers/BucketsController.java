package com.learn.aws.controllers;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("buckets")
public class BucketsController {
    final AmazonS3 s3;

    BucketsController() {
        s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
    }

    @GetMapping()
    public List<String> getBucketNames() {
        List<Bucket> buckets = s3.listBuckets();
        return buckets.stream().map(bucket -> bucket.getName()).collect(Collectors.toList());
    }

    @PostMapping("{bucketName}")
    public ResponseEntity<String> addNewBucket(@PathVariable String bucketName) {
        if (s3.doesBucketExistV2(bucketName))
            return new ResponseEntity<>("Bucket Already Exists", HttpStatus.CONFLICT);
        else {
            s3.createBucket(bucketName);
            return new ResponseEntity<>("Created Successfully", HttpStatus.CREATED);
        }
    }

    @DeleteMapping("{bucketName}")
    public ResponseEntity<String> deleteBucket(@PathVariable String bucketName){

        ObjectListing objectListing= s3.listObjects(bucketName);

        while(true){

            for (Iterator<S3ObjectSummary> iterator = objectListing.getObjectSummaries().iterator(); iterator.hasNext();){
                S3ObjectSummary summary= iterator.next();
                s3.deleteObject(bucketName,summary.getKey());
            }

            if (objectListing.isTruncated())
                objectListing=s3.listNextBatchOfObjects(objectListing);
            else
                break;

        }
        s3.deleteBucket(bucketName);
        return new ResponseEntity<>("Deleted Successfully", HttpStatus.CREATED);
    }

}
