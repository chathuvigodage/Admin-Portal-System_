package com.paymedia.administrations.controller;

import com.paymedia.administrations.entity.DualAuthData;
import com.paymedia.administrations.model.request.RoleRequest;
import com.paymedia.administrations.model.request.UserRequest;
import com.paymedia.administrations.model.response.CommonResponse;
import com.paymedia.administrations.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.paymedia.administrations.service.ReportSchedulerService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private final RoleService roleService;

    @Autowired
    private ReportSchedulerService reportSchedulerService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/create")
    public ResponseEntity<CommonResponse<DualAuthData>> createRole(@RequestBody RoleRequest roleRequest) {
        DualAuthData createdRole = roleService.createRole(roleRequest);
        return ResponseEntity.ok(new CommonResponse<>(true, "Role creation request submitted successfully", createdRole));
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<CommonResponse<String>> approveRole(@PathVariable Integer id) {
        roleService.approveRole(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "Role approved successfully", null));
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<CommonResponse<String>> rejectRole(@PathVariable Integer id) {
        roleService.rejectRole(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "Role rejected successfully", null));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CommonResponse<DualAuthData>> updateRole(@PathVariable Integer id, @RequestBody RoleRequest roleRequest) {
        DualAuthData dualAuthData = roleService.updateRole(id, roleRequest);
        return ResponseEntity.ok(new CommonResponse<>(true, "Role update request submitted", dualAuthData));
    }

    // Endpoint to approve a role update
    @PostMapping("/approve/update/{id}")
    public ResponseEntity<CommonResponse<Void>> approveRoleUpdate(@PathVariable Integer id) {
        roleService.approveRoleUpdate(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "Role update approved", null));
    }

    // Endpoint to delete a role
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<CommonResponse<DualAuthData>> deleteRole(@PathVariable Integer id) {
        DualAuthData dualAuthData = roleService.deleteRole(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "Role deletion request submitted", dualAuthData));
    }

    // Endpoint to approve a role deletion
    @PostMapping("/approve/delete/{id}")
    public ResponseEntity<CommonResponse<Void>> approveRoleDeletion(@PathVariable Integer id) {
        roleService.approveRoleDeletion(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "Role deletion approved", null));
    }

    @PostMapping("/active/{id}")
    public ResponseEntity<CommonResponse<String>> activateRole(@PathVariable Integer id) {
        String result = roleService.activateRole(id);
        return ResponseEntity.ok(new CommonResponse<>(true, result, null));
    }

    @PostMapping("/de-active/{id}")
    public ResponseEntity<CommonResponse<String>> deactivateUser(@PathVariable Integer id) {
        roleService.deactivateRole(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "Deactivation request submitted successfully", null));
    }

    @PostMapping("/approveActivation/{id}")
    public ResponseEntity<CommonResponse<String>> approveActivateRole(@PathVariable Integer id) {
        roleService.approveActivateRole(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "Role approved Activation successfully", null));
    }

    @PostMapping("/approveDeactivation/{id}")
    public ResponseEntity<CommonResponse<String>> approveDeactivateRole(@PathVariable Integer id) {
        roleService.approveDeactivateRole(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "Role approved Deactivation successfully", null));
    }

    @PostMapping("/rejectRequest/{id}")
    public ResponseEntity<CommonResponse<String>> rejectRequest(@PathVariable Integer id) {
        String response = roleService.rejectRequest(id);
        return ResponseEntity.ok(new CommonResponse<>(true, response, null));
    }

    @GetMapping("/report")
    public ResponseEntity<byte[]> generateRoleReport(
            @RequestParam("fromDate") String fromDateStr,
            @RequestParam("toDate") String toDateStr,
            @RequestParam("reportType") String reportType) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime fromDate = LocalDateTime.parse(fromDateStr, formatter);
        LocalDateTime toDate = LocalDateTime.parse(toDateStr, formatter);

        byte[] reportData = roleService.generateRoleReport(fromDate, toDate, reportType);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=role_report." + reportType)
                .body(reportData);
    }

//    @GetMapping("/send-report")
//    public ResponseEntity<String> sendReportEmail() {
//        try {
//            reportSchedulerService.sendDailyReportSummary();
//            return ResponseEntity.ok("Email sent successfully!");
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("Failed to send email: " + e.getMessage());
//        }
//    }

}


