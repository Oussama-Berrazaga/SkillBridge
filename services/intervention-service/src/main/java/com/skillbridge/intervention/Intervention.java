package com.skillbridge.intervention;

@Entity
@Table(name = "interventions")
public class Intervention {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private LocalDateTime date;

  @Column(nullable = false)
  private String location;

}
