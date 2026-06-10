package com.workorder.controller;

import com.workorder.model.Worker;
import com.workorder.service.WorkerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @PostMapping
    public Worker create(@RequestBody Worker worker) {
        return workerService.create(worker);
    }

    @GetMapping
    public List<Worker> getAll(@RequestParam(required = false) String workstation) {
        if (workstation != null) {
            return workerService.getByWorkstation(workstation);
        }
        return workerService.getAll();
    }

    @GetMapping("/{id}")
    public Worker getById(@PathVariable String id) {
        return workerService.getById(id);
    }

    @PutMapping("/{id}")
    public Worker update(@PathVariable String id, @RequestBody Worker worker) {
        worker.setId(id);
        return workerService.update(worker);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        workerService.delete(id);
    }
}
