
package kmie_enquiry_system;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EnquiryController {

    private final StudentEnquiryRepository repository;

    public EnquiryController(StudentEnquiryRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/enquiry")
    public String showEnquiryForm() {
        return "enquiry";
    }

    @PostMapping("/enquiry")
    public String submitEnquiry(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String studentClass,
            @RequestParam String course,
            @RequestParam(required = false, defaultValue = "")
            String message,
            Model model) {

        fullName = fullName.trim();
        email = email.trim();
        phone = phone.trim();
        studentClass = studentClass.trim();
        course = course.trim();

        // Server-side validation
        if (fullName.isEmpty()
                || email.isEmpty()
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || !phone.matches("[0-9]{10}")
                || !(studentClass.equals("11") || studentClass.equals("12"))
                || !(course.equals("NEET") || course.equals("JEE"))) {

            model.addAttribute("error",
                    "Please enter valid student details.");
            return "enquiry";
        }

        // Duplicate prevention
        if (repository.existsByPhone(phone)) {
            model.addAttribute("error",
                    "This mobile number is already registered. "
                    + "Please use a different mobile number.");
            return "enquiry";
        }

        // Save the new student
        StudentEnquiry enquiry = new StudentEnquiry();

        enquiry.setFullName(fullName);
        enquiry.setEmail(email);
        enquiry.setPhone(phone);
        enquiry.setStudentClass(studentClass);
        enquiry.setCourse(course);
        enquiry.setMessage(message);

        repository.save(enquiry);

        model.addAttribute("studentName", fullName);

        return "enquiry-success";
    }
}
