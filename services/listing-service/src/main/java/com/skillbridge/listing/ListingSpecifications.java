package com.skillbridge.listing;

import org.springframework.data.jpa.domain.Specification;

import com.skillbridge.common.Status;

public class ListingSpecifications {

  public static Specification<Listing> hasTitleLike(String title) {
    return (root, query, cb) -> title == null ? null
        : cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
  }

  public static Specification<Listing> hasStatus(Status status) {
    return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
  }

  public static Specification<Listing> belongsToCategory(Long categoryId) {
    return (root, query, cb) -> {
      if (categoryId == null)
        return null;
      // This joins the 'categories' collection in the Listing entity
      return cb.equal(root.join("categories").get("id"), categoryId);
    };
  }
}