package com.accenture.franchiseapi.infrastructure.adapter.out.dynamodb.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.ArrayList;
import java.util.List;

@DynamoDbBean
public class FranchiseEntity {

    private String id;
    private String name;
    private List<BranchEntity> branches = new ArrayList<>();

    @DynamoDbPartitionKey
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<BranchEntity> getBranches() { return branches; }
    public void setBranches(List<BranchEntity> branches) { this.branches = branches; }
}
