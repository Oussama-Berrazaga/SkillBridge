package com.skillbridge.listing;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data // Includes @ToString
@Embeddable
public class Address {

  String street;
  String city;
  String state;
  String zipCode;

  @Override
  public String toString() {
    return String.format("%s, %s, %s", street, city, state);
  }
}
