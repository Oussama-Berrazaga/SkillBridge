package com.skillbridge.category;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categories")
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String name;

  private String description;

  private String iconCode;

  // The Parent: Many sub-categories belong to one parent category
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  @JsonBackReference // Prevents infinite recursion when serializing to JSON
  private Category parent;

  // The Children: One category can have many sub-categories
  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
  @JsonManagedReference // Tells Jackson this is the part to include in the JSON
  @Builder.Default
  private List<Category> subCategories = new ArrayList<>();
}