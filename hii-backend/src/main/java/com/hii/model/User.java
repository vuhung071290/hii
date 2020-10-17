package com.hii.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
  private String userId;
  private String userName;
  private String fullName;
  private String password;
}
