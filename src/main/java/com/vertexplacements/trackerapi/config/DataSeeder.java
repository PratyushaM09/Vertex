package com.vertexplacements.trackerapi.config;

import com.vertexplacements.trackerapi.entity.Application;
import com.vertexplacements.trackerapi.entity.ApplicationStatus;
import com.vertexplacements.trackerapi.entity.Company;
import com.vertexplacements.trackerapi.entity.User;
import com.vertexplacements.trackerapi.repository.ApplicationRepository;
import com.vertexplacements.trackerapi.repository.CompanyRepository;
import com.vertexplacements.trackerapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEMO_EMAIL = "admin@vertexplacements.com";
    private static final String DEMO_PASSWORD = "password123";

    @Override
    public void run(String... args) {
        seedDemoUser();
        seedCompaniesAndApplications();
    }

    private void seedDemoUser() {
        // Check for this specific account, not just "any user exists" —
        // other accounts existing shouldn't stop the demo account from being created.
        if (userRepository.existsByEmail(DEMO_EMAIL)) {
            return;
        }
        userRepository.save(User.builder()
                .fullName("Demo Admin")
                .email(DEMO_EMAIL)
                .password(passwordEncoder.encode(DEMO_PASSWORD))
                .build());

        System.out.println("=================================================================");
        System.out.println(" VertexPlacements demo login created:");
        System.out.println("   Email:    " + DEMO_EMAIL);
        System.out.println("   Password: " + DEMO_PASSWORD);
        System.out.println("=================================================================");
    }

    private void seedCompaniesAndApplications() {
        if (companyRepository.count() > 0) {
            return;
        }

        // Defensive: never let optional demo data crash real startup.
        // If the demo user genuinely isn't there for some reason, just skip seeding.
        Optional<User> demoUserOpt = userRepository.findByEmail(DEMO_EMAIL);
        if (demoUserOpt.isEmpty()) {
            System.out.println("Skipping demo company/application seeding — demo user not found.");
            return;
        }
        User demoUser = demoUserOpt.get();

        Company google = companyRepository.save(Company.builder()
                .name("Google").ctc(42.0).eligibilityCriteria("CGPA > 8.0, No backlogs")
                .visitDate(LocalDate.of(2026, 3, 14)).owner(demoUser).build());

        Company microsoft = companyRepository.save(Company.builder()
                .name("Microsoft").ctc(38.0).eligibilityCriteria("CGPA > 7.5, No backlogs")
                .visitDate(LocalDate.of(2026, 2, 20)).owner(demoUser).build());

        Company deloitte = companyRepository.save(Company.builder()
                .name("Deloitte").ctc(12.0).eligibilityCriteria("CGPA > 6.5")
                .visitDate(LocalDate.of(2026, 1, 10)).owner(demoUser).build());

        Company infosys = companyRepository.save(Company.builder()
                .name("Infosys").ctc(7.5).eligibilityCriteria("CGPA > 6.0")
                .visitDate(LocalDate.of(2026, 1, 25)).owner(demoUser).build());

        applicationRepository.save(Application.builder()
                .studentName("Riya Sharma").studentRoll("21CS1042")
                .status(ApplicationStatus.SHORTLISTED).applyDate(LocalDate.now())
                .company(google).owner(demoUser).build());

        applicationRepository.save(Application.builder()
                .studentName("Aman Verma").studentRoll("21EC1078")
                .status(ApplicationStatus.SELECTED).applyDate(LocalDate.now())
                .company(deloitte).owner(demoUser).build());

        applicationRepository.save(Application.builder()
                .studentName("Kabir Singh").studentRoll("21ME1005")
                .status(ApplicationStatus.APPLIED).applyDate(LocalDate.now())
                .company(microsoft).owner(demoUser).build());

        applicationRepository.save(Application.builder()
                .studentName("Sneha Rao").studentRoll("21CS1099")
                .status(ApplicationStatus.REJECTED).applyDate(LocalDate.now())
                .company(infosys).owner(demoUser).build());
    }
}