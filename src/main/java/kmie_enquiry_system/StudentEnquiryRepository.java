
package kmie_enquiry_system;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentEnquiryRepository
        extends JpaRepository<StudentEnquiry, Long> {

    // Existing admin search
    @Query("""
        SELECT s FROM StudentEnquiry s
        WHERE LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(s.course) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    List<StudentEnquiry> searchEnquiries(
            @Param("keyword") String keyword
    );

    // New: Check whether mobile number already exists
    boolean existsByPhone(String phone);
}
