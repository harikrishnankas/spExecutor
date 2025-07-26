package com.sp.spexecutor.service;
 
import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.sp.spexecutor.controller.JspProcedureController.Param;
 
@Service
public class StoredProcedureExecutorService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
 
    /**
     * Executes a stored procedure by its name, auto-detecting its signature.
     * Fills IN parameters with null/defaults, returns OUT values as a String.
     * @param procedureName Name of the stored procedure (optionally schema-qualified)
     * @return true if executed successfully, false otherwise
     */
    public boolean executeProcedureByName(String procedureName) {
        return jdbcTemplate.execute((Connection conn) -> {
            try {
                // Query Oracle's ALL_ARGUMENTS to get parameter info
                String[] parts = procedureName.split("\\.");
                String owner = null, objectName;
                if (parts.length == 2) {
                    owner = parts[0].toUpperCase();
                    objectName = parts[1].toUpperCase();
                } else {
                    objectName = procedureName.toUpperCase();
                }
 
                String sql = "SELECT ARGUMENT_NAME, POSITION, IN_OUT, DATA_TYPE FROM ALL_ARGUMENTS WHERE OBJECT_NAME = ? " +
                        (owner != null ? "AND OWNER = ? " : "") +
                        "ORDER BY POSITION";
                final String finalOwner = owner;
 
                java.util.List<Argument> args = jdbcTemplate.query(
                        sql,
                        ps -> {
                            ps.setString(1, objectName);
                            if (finalOwner != null) ps.setString(2, finalOwner);
                        },
                        (rs, rowNum) -> new Argument(
                                rs.getString("ARGUMENT_NAME"),
                                rs.getInt("POSITION"),
                                rs.getString("IN_OUT"),
                                rs.getString("DATA_TYPE")
                        )
                );
 
                // Build call string
                StringBuilder call = new StringBuilder();
                call.append("{call ").append(procedureName).append("(");
                for (int i = 0; i < args.size(); i++) {
                    if (i > 0) call.append(",");
                    call.append("?");
                }
                call.append(")}");
 
                try (CallableStatement cs = conn.prepareCall(call.toString())) {
                    // Set parameters to null/defaults, register OUT
                    for (int i = 0; i < args.size(); i++) {
                        Argument arg = args.get(i);
                        int paramIdx = i + 1;
                        if (arg.inOut.contains("OUT")) {
                            cs.registerOutParameter(paramIdx, java.sql.Types.VARCHAR); // generic, for demo
                        }
                        if (arg.inOut.contains("IN")) {
                            cs.setObject(paramIdx, null); // or provide values
                        }
                    }
                    cs.execute();
                    // Could fetch OUT params here
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }
 
    // Fetch IN parameters for a given procedure
    public List<Param> getInParameters(String procedureName) {
        String[] parts = procedureName.split("\\.");
        String owner = null, objectName;
        if (parts.length == 2) {
            owner = parts[0].toUpperCase();
            objectName = parts[1].toUpperCase();
        } else {
            objectName = procedureName.toUpperCase();
        }
        String sql = "SELECT ARGUMENT_NAME, DATA_TYPE FROM ALL_ARGUMENTS WHERE OBJECT_NAME = ? " +
                (owner != null ? "AND OWNER = ? " : "") +
                "AND IN_OUT LIKE '%IN%' AND ARGUMENT_NAME IS NOT NULL ORDER BY POSITION";
        final String finalOwner = owner;
        return jdbcTemplate.query(
                sql,
                ps -> {
                    ps.setString(1, objectName);
                    if (finalOwner != null) ps.setString(2, finalOwner);
                },
                (rs, rowNum) -> new Param(
                        rs.getString("ARGUMENT_NAME"),
                        rs.getString("DATA_TYPE")
                )
        );
    }
 
    // Execute procedure with user-provided params
    public boolean executeProcedureByNameWithParams(String procedureName, java.util.Map<String, String> paramValues) {
        return jdbcTemplate.execute((Connection conn) -> {
            try {
                String[] parts = procedureName.split("\\.");
                String owner = null, objectName;
                if (parts.length == 2) {
                    owner = parts[0].toUpperCase();
                    objectName = parts[1].toUpperCase();
                } else {
                    objectName = procedureName.toUpperCase();
                }
                String sql = "SELECT ARGUMENT_NAME, POSITION, IN_OUT, DATA_TYPE FROM ALL_ARGUMENTS WHERE OBJECT_NAME = ? " +
                        (owner != null ? "AND OWNER = ? " : "") +
                        "AND ARGUMENT_NAME IS NOT NULL ORDER BY POSITION";
                final String finalOwner = owner;
                java.util.List<Argument> args = jdbcTemplate.query(
                        sql,
                        ps -> {
                            ps.setString(1, objectName);
                            if (finalOwner != null) ps.setString(2, finalOwner);
                        },
                        (rs, rowNum) -> new Argument(
                                rs.getString("ARGUMENT_NAME"),
                                rs.getInt("POSITION"),
                                rs.getString("IN_OUT"),
                                rs.getString("DATA_TYPE")
                        )
                );
                StringBuilder call = new StringBuilder();
                call.append("{call ").append(procedureName).append("(");
                for (int i = 0; i < args.size(); i++) {
                    if (i > 0) call.append(",");
                    call.append("?");
                }
                call.append(")}");
                try (CallableStatement cs = conn.prepareCall(call.toString())) {
                    for (int i = 0; i < args.size(); i++) {
                        Argument arg = args.get(i);
                        int paramIdx = i + 1;
                        if (arg.inOut.contains("OUT")) {
                            cs.registerOutParameter(paramIdx, java.sql.Types.VARCHAR); // generic for demo
                        }
                        if (arg.inOut.contains("IN")) {
                            String val = paramValues.get(arg.name);
                            cs.setObject(paramIdx, val);
                        }
                    }
                    cs.execute();
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }
 
    // Helper class to hold argument metadata
    private static class Argument {
        String name;
        int position;
        String inOut;
        String dataType;
        Argument(String name, int position, String inOut, String dataType) {
            this.name = name;
            this.position = position;
            this.inOut = inOut;
            this.dataType = dataType;
        }
    }
}