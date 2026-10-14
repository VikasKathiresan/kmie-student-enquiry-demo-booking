
package kmie_enquiry_system;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    private final StudentEnquiryRepository repository;
    private final DemoBookingRepository bookingRepository;

    public AdminController(
            StudentEnquiryRepository repository,
            DemoBookingRepository bookingRepository) {
        this.repository = repository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/admin/enquiries")
    public String viewEnquiries(
            @RequestParam(required = false, defaultValue = "")
            String keyword,
            @RequestParam(required = false, defaultValue = "")
            String bookingDate,
            @RequestParam(required = false, defaultValue = "")
            String bookingStatus,
            Model model) {

        String searchKeyword = keyword.trim();

        // Preserve existing enquiry search
        if (searchKeyword.isEmpty()) {
            model.addAttribute("enquiries", repository.findAll());
        } else {
            model.addAttribute("enquiries",
                    repository.searchEnquiries(searchKeyword));
        }

        model.addAttribute("keyword", searchKeyword);

        // Preserve existing dashboard statistics
        List<StudentEnquiry> allEnquiries = repository.findAll();

        long javaCount = allEnquiries.stream()
                .filter(e -> "Java Full Stack"
                        .equalsIgnoreCase(e.getCourse()))
                .count();

        long pythonCount = allEnquiries.stream()
                .filter(e -> "Python Development"
                        .equalsIgnoreCase(e.getCourse()))
                .count();

        long webCount = allEnquiries.stream()
                .filter(e -> "Web Development"
                        .equalsIgnoreCase(e.getCourse()))
                .count();

        long analyticsCount = allEnquiries.stream()
                .filter(e -> "Data Analytics"
                        .equalsIgnoreCase(e.getCourse()))
                .count();

        long cloudCount = allEnquiries.stream()
                .filter(e -> "Cloud Computing"
                        .equalsIgnoreCase(e.getCourse()))
                .count();

        model.addAttribute("totalEnquiries", allEnquiries.size());
        model.addAttribute("javaCount", javaCount);
        model.addAttribute("pythonCount", pythonCount);
        model.addAttribute("webCount", webCount);
        model.addAttribute("analyticsCount", analyticsCount);
        model.addAttribute("cloudCount", cloudCount);

        // New: Demo bookings and filters
        List<DemoBooking> allBookings = bookingRepository.findAll();

        String selectedDate = bookingDate.trim();
        String selectedStatus = bookingStatus.trim();

        LocalDate parsedDate = null;
        String bookingFilterError = "";

        if (!selectedDate.isEmpty()) {
            try {
                parsedDate = LocalDate.parse(selectedDate);
            } catch (DateTimeParseException ex) {
                bookingFilterError = "Invalid booking date.";
            }
        }

        Stream<DemoBooking> bookingStream = allBookings.stream();

        if (parsedDate != null) {
            LocalDate filterDate = parsedDate;
            bookingStream = bookingStream.filter(
                    booking -> filterDate.equals(booking.getDemoDate()));
        }

        if (!selectedStatus.isEmpty()) {
            bookingStream = bookingStream.filter(
                    booking -> selectedStatus.equalsIgnoreCase(
                            booking.getStatus()));
        }

        model.addAttribute("bookings", bookingStream.toList());
        model.addAttribute("bookingDate", selectedDate);
        model.addAttribute("bookingStatus", selectedStatus);
        model.addAttribute("bookingFilterError", bookingFilterError);
        model.addAttribute("totalBookings", allBookings.size());

        return "admin-enquiries";
    }
}
