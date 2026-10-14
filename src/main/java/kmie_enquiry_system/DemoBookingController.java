
package kmie_enquiry_system;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DemoBookingController {

    private final DemoBookingRepository bookingRepository;
    private final StudentEnquiryRepository studentRepository;

    private static final List<LocalTime> ALLOWED_SLOTS =
            List.of(
                LocalTime.of(13, 0),
                LocalTime.of(15, 0),
                LocalTime.of(17, 0),
                LocalTime.of(19, 0)
            );

    public DemoBookingController(
            DemoBookingRepository bookingRepository,
            StudentEnquiryRepository studentRepository) {

        this.bookingRepository = bookingRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/demo-booking")
    public String showBookingForm(Model model) {

        model.addAttribute("today", LocalDate.now());

        return "demo-booking";
    }

    @PostMapping("/demo-booking")
    public String createBooking(
            @RequestParam String phone,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate demoDate,
            @RequestParam String demoTime,
            Model model) {

        LocalDate today = LocalDate.now();

        model.addAttribute("today", today);

        // Fix: Use a final variable for lambda expression
        final String normalizedPhone = phone.trim();

        // Validate mobile number
        if (!normalizedPhone.matches("[0-9]{10}")) {

            model.addAttribute(
                "error",
                "Please enter a valid 10-digit mobile number."
            );

            return "demo-booking";
        }

        // Validate demo date
        if (demoDate.isBefore(today)) {

            model.addAttribute(
                "error",
                "Please select today or a future date."
            );

            return "demo-booking";
        }

        // Convert selected time into LocalTime
        LocalTime selectedTime;

        try {
            selectedTime = LocalTime.parse(demoTime);
        } catch (Exception ex) {

            model.addAttribute(
                "error",
                "Please select a valid demo time slot."
            );

            return "demo-booking";
        }

        // Allow only the four required slots
        if (!ALLOWED_SLOTS.contains(selectedTime)) {

            model.addAttribute(
                "error",
                "Only 1 PM, 3 PM, 5 PM and 7 PM slots are allowed."
            );

            return "demo-booking";
        }

        // Prevent booking an expired time slot today
        if (demoDate.equals(today)
                && !selectedTime.isAfter(LocalTime.now())) {

            model.addAttribute(
                "error",
                "This time slot has already passed. "
                + "Please select a later slot or another date."
            );

            return "demo-booking";
        }

        // Find student using registered mobile number
        List<StudentEnquiry> students =
                studentRepository.findAll()
                    .stream()
                    .filter(s ->
                        normalizedPhone.equals(s.getPhone())
                    )
                    .toList();

        // Student must be registered
        if (students.isEmpty()) {

            model.addAttribute(
                "error",
                "Mobile number is not registered. "
                + "Please submit a student enquiry first."
            );

            return "demo-booking";
        }

        // Handle existing duplicate student records
        if (students.size() > 1) {

            model.addAttribute(
                "error",
                "Multiple student records use this mobile number. "
                + "Please contact the administrator."
            );

            return "demo-booking";
        }

        StudentEnquiry student = students.get(0);

        // Check duplicate booking
        boolean alreadyBooked =
                bookingRepository
                    .existsByStudentEnquiryIdAndDemoDateAndDemoTime(
                        student.getId(),
                        demoDate,
                        selectedTime
                    );

        if (alreadyBooked) {

            model.addAttribute(
                "error",
                "You have already booked this date and time slot."
            );

            return "demo-booking";
        }

        // Create new booking
        DemoBooking booking = new DemoBooking();

        booking.setStudentEnquiry(student);
        booking.setDemoDate(demoDate);
        booking.setDemoTime(selectedTime);
        booking.setStatus("PENDING");

        // Save booking to MySQL
        try {
            bookingRepository.saveAndFlush(booking);
        } catch (DataIntegrityViolationException ex) {

            model.addAttribute(
                "error",
                "Booking could not be saved. "
                + "Please check whether this slot is already booked."
            );

            return "demo-booking";
        }

        // Pass details to confirmation page
        model.addAttribute(
            "studentName",
            student.getFullName()
        );

        model.addAttribute("demoDate", demoDate);
        model.addAttribute("demoTime", selectedTime);
        model.addAttribute("status", booking.getStatus());

        return "demo-booking-success";
    }
}
