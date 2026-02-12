package com.skillbridge.category;

public record CategoryResponse(
    Long id,
    String name,
    String description,
    String iconCode,
    Long parentId) {

}
