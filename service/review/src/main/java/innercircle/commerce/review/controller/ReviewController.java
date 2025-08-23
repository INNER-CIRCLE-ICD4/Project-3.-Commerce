package innercircle.commerce.review.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewController {

    @GetMapping
    public String test() {
        return "hello world2";
    }
}
