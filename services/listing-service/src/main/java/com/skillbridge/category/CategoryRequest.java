package com.skillbridge.category;

public record CategoryRequest(
    String name,
    String description,
    String iconCode,
    Long parentId) {

}
