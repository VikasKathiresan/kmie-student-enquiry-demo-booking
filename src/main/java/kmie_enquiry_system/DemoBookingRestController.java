
package kmie_enquiry_system;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
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
@RequestMapping("/api/bookings")
public class DemoBookingRestController {

    private final DemoBookingRepository bookingRepository;
    private final StudentEnquiryRepository studentRepository;

    private static final List<LocalTime> ALLOWED_SLOTS =
            List.of(
                LocalTime.of(13, 0),
                LocalTime.of(15, 0),
                LocalTime.of(17, 0),
                LocalTime.of(19, 0)
            );

    public DemoBookingRestController(
            DemoBookingRepository bookingRepository,
            StudentEnquiryRepository studentRepository) {

        this.bookingRepository = bookingRepository;
        this.studentRepository = studentRepository;
    }

    // GET: List all demo bookings
    @GetMapping
    public List<BookingResponse> listBookings() {

        return bookingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // POST: Create a demo booking
    @PostMapping
    public ResponseEntity<?> createBooking(
            @RequestBody BookingRequest request) {

        if (request == null
                || request.phone() == null
                || request.demoDate() == null
                || request.demoTime() == null) {

            return ResponseEntity.badRequest().body(
                Map.of("error", "All booking fields are required.")
            );
        }

        String phone = request.phone().trim();
        LocalDate demoDate = request.demoDate();
        LocalTime demoTime = request.demoTime();

        if (!phone.matches("[0-9]{10}")) {

            return ResponseEntity.badRequest().body(
                Map.of("error", "Invalid mobile number.")
            );
        }

        if (demoDate.isBefore(LocalDate.now())) {

            return ResponseEntity.badRequest().body(
                Map.of("error", "Past demo dates are not allowed.")
            );
        }

        if (!ALLOWED_SLOTS.contains(demoTime)) {

            return ResponseEntity.badRequest().body(
                Map.of("error", "Invalid demo time slot.")
            );
        }

        if (demoDate.equals(LocalDate.now())
                && !demoTime.isAfter(LocalTime.now())) {

            return ResponseEntity.badRequest().body(
                Map.of("error", "Selected demo time has already passed.")
            );
        }

        // Find registered student
        List<StudentEnquiry> students =
                studentRepository.findAll()
                    .stream()
                    .filter(s -> phone.equals(s.getPhone()))
                    .toList();

        if (students.isEmpty()) {

            return ResponseEntity.badRequest().body(
                Map.of("error", "Student is not registered.")
            );
        }

        if (students.size() > 1) {

            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                    "error",
                    "Multiple student records use this mobile number."
                )
            );
        }

        StudentEnquiry student = students.get(0);

        // Prevent duplicate date and slot
        boolean duplicate =
                bookingRepository
                    .existsByStudentEnquiryIdAndDemoDateAndDemoTime(
                        student.getId(),
                        demoDate,
                        demoTime
                    );

        if (duplicate) {

            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                    "error",
                    "You have already booked this date and time slot."
                )
            );
        }

        DemoBooking booking = new DemoBooking();

        booking.setStudentEnquiry(student);
        booking.setDemoDate(demoDate);
        booking.setDemoTime(demoTime);
        booking.setStatus("PENDING");

        try {

            DemoBooking saved =
                    bookingRepository.saveAndFlush(booking);

            return ResponseEntity
                    .created(URI.create(
                        "/api/bookings/" + saved.getId()
                    ))
                    .body(toResponse(saved));

        } catch (DataIntegrityViolationException ex) {

            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                    "error",
                    "Booking could not be saved. "
                    + "Please check for duplicate booking."
                )
            );
        }
    }

    private BookingResponse toResponse(DemoBooking booking) {

        StudentEnquiry student = booking.getStudentEnquiry();

        return new BookingResponse(
                booking.getId(),
                student.getId(),
                student.getFullName(),
                student.getCourse(),
                booking.getDemoDate(),
                booking.getDemoTime(),
                booking.getStatus()
        );
    }

    public record BookingRequest(
            String phone,
            LocalDate demoDate,
            LocalTime demoTime
    ) {}

    public record BookingResponse(
            Long id,
            Long studentId,
            String studentName,
            String course,
            LocalDate demoDate,
            LocalTime demoTime,
            String status
    ) {}
}
