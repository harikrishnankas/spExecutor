package com.sp.spexecutor.controller;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sp.spexecutor.service.StoredProcedureExecutorService;
 
@Controller
public class JspProcedureController {
    @Autowired
    private StoredProcedureExecutorService executorService;
 
    @GetMapping("/jsp/execute")
    public String showForm() {
        return "jsp/executeProcedure";
    }
 
    @PostMapping("/jsp/execute")
    public String getProcedureParams(@RequestParam("procName") String procName, Model model) {
        // Fetch IN parameters from Oracle
        java.util.List<Param> params = executorService.getInParameters(procName);
        model.addAttribute("procName", procName);
        model.addAttribute("params", params);
        return "jsp/executeProcedure";
    }
 
    @PostMapping("/jsp/execute/run")
    public String executeProcedureWithParams(@RequestParam("procName") String procName, @RequestParam java.util.Map<String, String> allParams, Model model) {
        // Remove procName from param map
        allParams.remove("procName");
        boolean success = executorService.executeProcedureByNameWithParams(procName, allParams);
        model.addAttribute("result", new Result(success, success ? "Procedure executed successfully." : "Failed to execute procedure."));
        return "jsp/executeProcedure";
    }
 
    public static class Result {
        public boolean success;
        public String message;
        public Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
 
    public static class Param {
        public String name;
        public String type;
        public Param(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }
}
 