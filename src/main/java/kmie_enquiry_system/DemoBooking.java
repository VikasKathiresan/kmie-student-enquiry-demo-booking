
package kmie_enquiry_system;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "demo_bookings",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_student_demo_slot",
        columnNames = {
            "student_enquiry_id",
            "demo_date",
            "demo_time"
        }
    )
)
public class DemoBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link booking with existing student
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "student_enquiry_id",
        nullable = false
    )
    private StudentEnquiry studentEnquiry;

    private LocalDate demoDate;

    private LocalTime demoTime;

    private String status = "PENDING";

    public DemoBooking() {
    }

    public Long getId() {
        return id;
    }

    public StudentEnquiry getStudentEnquiry() {
        return studentEnquiry;
    }

    public void setStudentEnquiry(
            StudentEnquiry studentEnquiry) {
        this.studentEnquiry = studentEnquiry;
    }

    public LocalDate getDemoDate() {
        return demoDate;
    }

    public void setDemoDate(LocalDate demoDate) {
        this.demoDate = demoDate;
    }

    public LocalTime getDemoTime() {
        return demoTime;
    }

    public void setDemoTime(LocalTime demoTime) {
        this.demoTime = demoTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
