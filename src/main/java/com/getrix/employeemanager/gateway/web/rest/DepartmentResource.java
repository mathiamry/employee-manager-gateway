package com.getrix.employeemanager.gateway.web.rest;

import com.getrix.employeemanager.gateway.domain.Department;
import com.getrix.employeemanager.gateway.repository.DepartmentRepository;
import com.getrix.employeemanager.gateway.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.getrix.employeemanager.gateway.domain.Department}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class DepartmentResource {

    private final Logger log = LoggerFactory.getLogger(DepartmentResource.class);

    private static final String ENTITY_NAME = "department";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DepartmentRepository departmentRepository;

    public DepartmentResource(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    /**
     * {@code POST  /departments} : Create a new department.
     *
     * @param department the department to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new department, or with status {@code 400 (Bad Request)} if the department has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/departments")
    public Mono<ResponseEntity<Department>> createDepartment(@RequestBody Department department) throws URISyntaxException {
        log.debug("REST request to save Department : {}", department);
        if (department.getId() != null) {
            throw new BadRequestAlertException("A new department cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return departmentRepository
            .save(department)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/departments/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /departments/:id} : Updates an existing department.
     *
     * @param id the id of the department to save.
     * @param department the department to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated department,
     * or with status {@code 400 (Bad Request)} if the department is not valid,
     * or with status {@code 500 (Internal Server Error)} if the department couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/departments/{id}")
    public Mono<ResponseEntity<Department>> updateDepartment(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Department department
    ) throws URISyntaxException {
        log.debug("REST request to update Department : {}, {}", id, department);
        if (department.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, department.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return departmentRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return departmentRepository
                    .save(department)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /departments/:id} : Partial updates given fields of an existing department, field will ignore if it is null
     *
     * @param id the id of the department to save.
     * @param department the department to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated department,
     * or with status {@code 400 (Bad Request)} if the department is not valid,
     * or with status {@code 404 (Not Found)} if the department is not found,
     * or with status {@code 500 (Internal Server Error)} if the department couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/departments/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Department>> partialUpdateDepartment(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Department department
    ) throws URISyntaxException {
        log.debug("REST request to partial update Department partially : {}, {}", id, department);
        if (department.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, department.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return departmentRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Department> result = departmentRepository
                    .findById(department.getId())
                    .map(existingDepartment -> {
                        if (department.getName() != null) {
                            existingDepartment.setName(department.getName());
                        }
                        if (department.getAddress() != null) {
                            existingDepartment.setAddress(department.getAddress());
                        }

                        return existingDepartment;
                    })
                    .flatMap(departmentRepository::save);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /departments} : get all the departments.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of departments in body.
     */
    @GetMapping("/departments")
    public Mono<List<Department>> getAllDepartments() {
        log.debug("REST request to get all Departments");
        return departmentRepository.findAll().collectList();
    }

    /**
     * {@code GET  /departments} : get all the departments as a stream.
     * @return the {@link Flux} of departments.
     */
    @GetMapping(value = "/departments", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Department> getAllDepartmentsAsStream() {
        log.debug("REST request to get all Departments as a stream");
        return departmentRepository.findAll();
    }

    /**
     * {@code GET  /departments/:id} : get the "id" department.
     *
     * @param id the id of the department to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the department, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/departments/{id}")
    public Mono<ResponseEntity<Department>> getDepartment(@PathVariable Long id) {
        log.debug("REST request to get Department : {}", id);
        Mono<Department> department = departmentRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(department);
    }

    /**
     * {@code DELETE  /departments/:id} : delete the "id" department.
     *
     * @param id the id of the department to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/departments/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteDepartment(@PathVariable Long id) {
        log.debug("REST request to delete Department : {}", id);
        return departmentRepository
            .deleteById(id)
            .map(result ->
                ResponseEntity
                    .noContent()
                    .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                    .build()
            );
    }
}
