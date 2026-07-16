package com.vertexplacements.trackerapi.config;

import com.vertexplacements.trackerapi.entity.Company;
import com.vertexplacements.trackerapi.entity.User;
import com.vertexplacements.trackerapi.entity.UserRole;
import com.vertexplacements.trackerapi.repository.CompanyRepository;
import com.vertexplacements.trackerapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEMO_EMAIL = "admin@vertexplacements.com";
    private static final String DEMO_PASSWORD = "password123";

    @Override
    public void run(String... args) {
        seedDemoOfficer();
        seedCompanies();
    }

    private void seedDemoOfficer() {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.save(User.builder()
                .fullName("Demo Admin")
                .email(DEMO_EMAIL)
                .password(passwordEncoder.encode(DEMO_PASSWORD))
                .role(UserRole.PLACEMENT_OFFICER)
                .build());

        System.out.println("=================================================================");
        System.out.println(" VertexPlacements demo Placement Officer login created:");
        System.out.println("   Email:    " + DEMO_EMAIL);
        System.out.println("   Password: " + DEMO_PASSWORD);
        System.out.println(" Register a separate account to try the student experience.");
        System.out.println("=================================================================");
    }

    private void seedCompanies() {
        if (companyRepository.count() > 0) {
            return;
        }

        User demoOfficer = userRepository.findByEmail(DEMO_EMAIL)
                .orElseThrow(() -> new IllegalStateException(
                        "Demo officer should already exist by the time companies are seeded"));

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