package six.indices.calculation;

import com.six.indexreview.IndexReviewApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Compatibility entry point retained for the original assignment project
 * name. The implemented application lives under com.six.indexreview.
 */
@SpringBootConfiguration
@Import(IndexReviewApplication.class)
public class CalculationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CalculationApplication.class, args);
    }
}
