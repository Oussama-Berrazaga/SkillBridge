package com.skillbridge;

import com.skillbridge.category.*;
import com.skillbridge.listing.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.flyway.enabled=false")
@DataJpaTest // Only loads JPA components; uses an in-memory DB by default
class CategoryListingIntegrationTest {

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private ListingRepository listingRepository;

  @Test
  @DisplayName("Should correctly persist and retrieve hierarchical categories and listings")
  void shouldSaveAndRetrieveListingWithCategories() {
    // 1. Arrange: Create Parent and Sub-Category
    Category parent = Category.builder()
        .name("Home Maintenance")
        .build();

    Category sub = Category.builder()
        .name("Plumbing")
        .parent(parent)
        .build();

    parent.setSubCategories(new java.util.ArrayList<>(java.util.List.of(sub)));
    categoryRepository.save(parent);

    // 2. Act: Create Listing and link to sub-category
    Listing listing = Listing.builder()
        .title("Leaky Faucet")
        .customerId(1L)
        .status(ListingStatus.ACTIVE)
        .categories(new HashSet<>(Set.of(sub)))
        .build();

    Listing savedListing = listingRepository.save(listing);

    // 3. Assert: Verify relationships
    assertThat(savedListing.getId()).isNotNull();
    assertThat(savedListing.getCategories()).hasSize(1);

    // Check if the linked category has the correct parent
    Category linkedCat = savedListing.getCategories().iterator().next();
    assertThat(linkedCat.getName()).isEqualTo("Plumbing");
    assertThat(linkedCat.getParent().getName()).isEqualTo("Home Maintenance");
  }
}