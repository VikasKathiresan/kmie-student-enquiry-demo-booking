
package kmie_enquiry_system;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface DemoBookingRepository
        extends JpaRepository<DemoBooking, Long> {

    // Fetch bookings along with student details
    // Fix for possible Hibernate lazy-loading error
    @Override
    @EntityGraph(attributePaths = {"studentEnquiry"})
    List<DemoBooking> findAll();

    // Prevent duplicate booking for same student,
    // same date and same time
    boolean existsByStudentEnquiryIdAndDemoDateAndDemoTime(
            Long studentId,
            LocalDate demoDate,
            LocalTime demoTime
    );

    // Retrieve all bookings for a particular student
    List<DemoBooking> findByStudentEnquiryId(
            Long studentId
    );
}
