package innercircle.commerce.review;

import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewController {

    @GetMapping
    public String test() {
        return "hello world2";
    }
}
