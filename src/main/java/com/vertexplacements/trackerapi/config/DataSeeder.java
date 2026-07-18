package com.vertexplacements.trackerapi.config;

import com.vertexplacements.trackerapi.entity.Company;
import com.vertexplacements.trackerapi.entity.User;
import com.vertexplacements.trackerapi.entity.UserRole;
import com.vertexplacements.trackerapi.repository.CompanyRepository;
import com.vertexplacements.trackerapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** Set via the ADMIN_PASSWORD environment variable (see application properties). */
    @Value("${app.admin-password:}")
    private String adminPassword;

    /** Sample companies are only seeded when SEED_DEMO_DATA=true. */
    @Value("${app.seed-demo-data:false}")
    private boolean seedDemoData;

    private static final String DEMO_EMAIL = "admin@vertexplacements.com";

    @Override
    public void run(String... args) {
        seedAdminOfficer();
        if (seedDemoData) {
            seedCompanies();
        }
    }

    private void seedAdminOfficer() {
        // Check for the admin specifically — not user count — so the admin is
        // recreated even when student accounts already exist.
        if (userRepository.findByEmail(DEMO_EMAIL).isPresent()) {
            return;
        }

        String password = (adminPassword != null && !adminPassword.isBlank())
                ? adminPassword
                : UUID.randomUUID().toString();

        userRepository.save(User.builder()
                .fullName("Placement Officer")
                .email(DEMO_EMAIL)
                .password(passwordEncoder.encode(password))
                .role(UserRole.PLACEMENT_OFFICER)
                .build());

        if (adminPassword == null || adminPassword.isBlank()) {
            System.out.println("ADMIN_PASSWORD not set — generated one-time admin password: " + password);
        } else {
            System.out.println("Placement Officer account created for " + DEMO_EMAIL);
        }
    }

    private void seedCompanies() {
        if (companyRepository.count() > 0) {
            return;
        }

        User demoOfficer = userRepository.findByEmail(DEMO_EMAIL)
                .orElseThrow(() -> new IllegalStateException(
                        "Admin officer should already exist by the time companies are seeded"));

        companyRepository.save(Company.builder()
                .name("Google").ctc(42.0).eligibilityCriteria("CGPA > 8.0, No backlogs")
                .visitDate(LocalDate.of(2026, 3, 14)).owner(demoOfficer).build());

        companyRepository.save(Company.builder()
                .name("Microsoft").ctc(38.0).eligibilityCriteria("CGPA > 7.5, No backlogs")
                .visitDate(LocalDate.of(2026, 2, 20)).owner(demoOfficer).build());

        companyRepository.save(Company.builder()
                .name("Deloitte").ctc(12.0).eligibilityCriteria("CGPA > 6.5")
                .visitDate(LocalDate.of(2026, 1, 10)).owner(demoOfficer).build());

        companyRepository.save(Company.builder()
                .name("Infosys").ctc(7.5).eligibilityCriteria("CGPA > 6.0")
                .visitDate(LocalDate.of(2026, 1, 25)).owner(demoOfficer).build());
    }
}