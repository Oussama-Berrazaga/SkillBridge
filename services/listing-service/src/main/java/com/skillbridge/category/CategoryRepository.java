package com.skillbridge.category;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
  /**
   * Finds all top-level categories (those with no parent).
   * Spring Data JPA interprets 'IsNull' automatically.
   */
  List<Category> findByParentIsNull();

  /**
   * Finds sub-categories for a specific parent ID.
   */
  List<Category> findByParentId(Long parentId);

  @Query("SELECT DISTINCT c FROM Category c " +
      "LEFT JOIN FETCH c.subCategories " +
      "WHERE c.parent IS NULL")
  List<Category> findAllRootCategoriesWithChildren();
}
