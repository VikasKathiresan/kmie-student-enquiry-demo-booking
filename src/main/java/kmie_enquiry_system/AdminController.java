package kmie_enquiry_system;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        @GetMapping({"/counsellor/login", "/login"})
        public String counsellorLogin() {
                return "counseller-login";
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

        long neetCount = allEnquiries.stream()
                .filter(e -> "NEET"
                        .equalsIgnoreCase(e.getCourse()))
                .count();

        long jeeCount = allEnquiries.stream()
                .filter(e -> "JEE"
                        .equalsIgnoreCase(e.getCourse()))
                .count();

        model.addAttribute("totalEnquiries", allEnquiries.size());
        model.addAttribute("neetCount", neetCount);
        model.addAttribute("jeeCount", jeeCount);

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

    @PostMapping("/admin/bookings/{id}/approve")
    public String approveBooking(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String bookingDate,
            @RequestParam(required = false, defaultValue = "") String bookingStatus,
            RedirectAttributes redirectAttributes) {

        bookingRepository.findById(id).ifPresentOrElse(booking -> {
            if ("PENDING".equals(booking.getStatus())) {
                booking.setStatus("CONFIRMED");
                bookingRepository.save(booking);
                redirectAttributes.addFlashAttribute(
                        "bookingActionMessage", "Booking approved.");
            } else {
                redirectAttributes.addFlashAttribute(
                        "bookingActionMessage",
                        "Only pending bookings can be approved.");
            }
        }, () -> redirectAttributes.addFlashAttribute(
                "bookingActionMessage", "Booking not found."));

        return redirectToDashboard(
                keyword, bookingDate, bookingStatus, redirectAttributes);
    }

    @PostMapping("/admin/bookings/{id}/cancel")
    public String cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String bookingDate,
            @RequestParam(required = false, defaultValue = "") String bookingStatus,
            RedirectAttributes redirectAttributes) {

        bookingRepository.findById(id).ifPresentOrElse(booking -> {
            String currentStatus = booking.getStatus();
            if ("PENDING".equals(currentStatus)
                    || "CONFIRMED".equals(currentStatus)) {
                booking.setStatus("CANCELLED");
                bookingRepository.save(booking);
                redirectAttributes.addFlashAttribute(
                        "bookingActionMessage", "Booking cancelled.");
            } else {
                redirectAttributes.addFlashAttribute(
                        "bookingActionMessage",
                        "This booking can no longer be cancelled.");
            }
        }, () -> redirectAttributes.addFlashAttribute(
                "bookingActionMessage", "Booking not found."));

        return redirectToDashboard(
                keyword, bookingDate, bookingStatus, redirectAttributes);
    }

    private String redirectToDashboard(
            String keyword,
            String bookingDate,
            String bookingStatus,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("bookingDate", bookingDate);
        redirectAttributes.addAttribute("bookingStatus", bookingStatus);
        return "redirect:/admin/enquiries";
    }
}