package com.skillbridge.profile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.skillbridge.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "profiles")
public class Profile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String firstName;
  private String lastName;
  private LocalDate birthDate;
  private String phoneNumber;
  private String bio;

  @Builder.Default
  private Double averageRating = 0.0;

  // Link back to User
  @OneToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Builder.Default
  @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TechnicianSkill> skills = new ArrayList<>();

  public void addSkill(TechnicianSkill skill) {
    if (skills == null) {
      skills = new ArrayList<>();
    }
    skills.add(skill);
    skill.setProfile(this);
  }
}