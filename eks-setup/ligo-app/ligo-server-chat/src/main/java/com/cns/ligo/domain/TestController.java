package com.cns.ligo.domain;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
  @GetMapping("/test")
  public ResponseEntity<String> test() {
    return ResponseEntity.ok("Hello World");
  }
}
