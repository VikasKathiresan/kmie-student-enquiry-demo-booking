package kmie_enquiry_system;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enquiries")
public class StudentEnquiryRestController {

    private final StudentEnquiryRepository repository;

    public StudentEnquiryRestController(
            StudentEnquiryRepository repository) {
        this.repository = repository;
    }

    // Public read-only alias is available at /api/enquiries/list.
    @GetMapping({"", "/list"})
    public List<StudentEnquiry> listEnquiries() {
        return repository.findAll();
    }

    // Create an enquiry using JSON
    @PostMapping
    public ResponseEntity<?> createEnquiry(
            @RequestBody EnquiryRequest request) {

        if (request == null
                || isBlank(request.fullName())
                || isBlank(request.email())
                || isBlank(request.phone())
                || isBlank(request.studentClass())
                || isBlank(request.course())) {

            return ResponseEntity.badRequest().body(
                Map.of("error", "All required fields must be provided.")
            );
        }

        String fullName = request.fullName().trim();
        String email = request.email().trim();
        String phone = request.phone().trim();
        String studentClass = request.studentClass().trim();
        String course = request.course().trim();

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || !phone.matches("[0-9]{10}")
                || !(studentClass.equals("11")
                    || studentClass.equals("12"))
                || !(course.equals("NEET")
                    || course.equals("JEE"))) {

            return ResponseEntity.badRequest().body(
                Map.of("error", "Invalid student details.")
            );
        }

        if (repository.existsByPhone(phone)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of("error", "Mobile number already registered.")
            );
        }

        StudentEnquiry student = new StudentEnquiry();
        student.setFullName(fullName);
        student.setEmail(email);
        student.setPhone(phone);
        student.setStudentClass(studentClass);
        student.setCourse(course);
        student.setMessage(
            request.message() == null ? "" : request.message()
        );
        student.setEnquiryDate(LocalDate.now());

        try {
            StudentEnquiry saved = repository.saveAndFlush(student);

            return ResponseEntity
                    .created(URI.create(
                        "/api/enquiries/" + saved.getId()
                    ))
                    .body(Map.of(
                        "id", saved.getId(),
                        "message", "Enquiry created successfully"
                    ));

        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of("error", "Enquiry could not be saved.")
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record EnquiryRequest(
            String fullName,
            String email,
            String phone,
            String studentClass,
            String course,
            String message
    ) {}
}