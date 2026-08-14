package com.xtensus.hrmanagementapi.workflow;
import com.xtensus.hrmanagementapi.security.user.CustomUserDetails;import org.springframework.http.HttpStatus;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/pipelines-validation/workflow-etapes") public class WorkflowAdministrationController{
 private final CongeWorkflowService service;public WorkflowAdministrationController(CongeWorkflowService s){service=s;}
 public record Reaffectation(Long nouveauDecideurId){}
 @GetMapping("/anomalies") public java.util.List<CongeWorkflowService.Anomalie> anomalies(){return service.anomalies();}
 @PostMapping("/{id}/reaffecter") @ResponseStatus(HttpStatus.NO_CONTENT) public void reaffecter(@PathVariable Long id,@RequestBody Reaffectation r,@AuthenticationPrincipal CustomUserDetails admin){service.reaffecter(id,r.nouveauDecideurId(),admin.getId());}
}
