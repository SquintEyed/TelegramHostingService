package org.example.controller;

import org.example.service.UserActivationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ActivationController {

    private final UserActivationService userActivationService;

    public ActivationController(UserActivationService userActivationService) {
        this.userActivationService = userActivationService;
    }


    @GetMapping("/user/activation")
    public ResponseEntity<?> activate(@RequestParam(name = "id") String userId) {

      boolean isActivated = userActivationService.activate(userId);

      if (isActivated) {
          return ResponseEntity.ok().body("Activate is successfully completed");
      }

      return ResponseEntity.internalServerError().body("Server internal error");
    }
}
