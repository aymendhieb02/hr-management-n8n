package com.xtensus.hrmanagementapi.workflow;
import com.xtensus.hrmanagementapi.security.user.CustomUserDetails; import java.util.List; import org.springframework.http.*; import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/pipelines-validation") public class PipelineValidationController{
 private final PipelineValidationService service; public PipelineValidationController(PipelineValidationService s){service=s;}
 @GetMapping public List<PipelineValidationService.Response> lister(){return service.lister();}
 @PostMapping public ResponseEntity<PipelineValidationService.Response> creer(@RequestBody PipelineValidationService.Request r){return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(r));}
 @PutMapping("/{id}") public PipelineValidationService.Response modifier(@PathVariable Long id,@RequestBody PipelineValidationService.Request r){return service.modifier(id,r);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void supprimer(@PathVariable Long id){service.supprimer(id);}
}
