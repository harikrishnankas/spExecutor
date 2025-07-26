package com.sp.spexecutor.controller;
 
import com.sp.spexecutor.service.StoredProcedureExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import com.sp.spexecutor.controller.JspProcedureController.Param;
 
@RestController
@RequestMapping("/api/procedure")
public class StoredProcedureController {
 
    @Autowired
    private StoredProcedureExecutorService executorService;
 
    /**
     * Execute a stored procedure by its name.
     * @param name Name of the stored procedure
     * @return ResponseEntity with status
     */
    @GetMapping("/params")
    public List<Param> getParams(@RequestParam String name) {
        return executorService.getInParameters(name);
    }
 
    @PostMapping("/executeWithParams")
    public Map<String, Object> executeWithParams(@RequestParam String name, @RequestBody Map<String, String> params) {
        boolean success = executorService.executeProcedureByNameWithParams(name, params);
        return Map.of(
            "success", success,
            "message", success ? "Procedure executed successfully." : "Failed to execute procedure."
        );
    }
 
    @PostMapping("/execute/{name}")
    public ResponseEntity<String> executeProcedure(@PathVariable String name) {
        boolean success = executorService.executeProcedureByName(name);
        if (success) {
            return ResponseEntity.ok("Stored procedure '" + name + "' executed successfully.");
        } else {
            return ResponseEntity.status(500).body("Failed to execute stored procedure: " + name);
        }
    }
}