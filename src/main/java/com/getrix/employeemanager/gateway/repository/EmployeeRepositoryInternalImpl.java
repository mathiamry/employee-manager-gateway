package com.getrix.employeemanager.gateway.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import com.getrix.employeemanager.gateway.domain.Employee;
import com.getrix.employeemanager.gateway.repository.rowmapper.DepartmentRowMapper;
import com.getrix.employeemanager.gateway.repository.rowmapper.EmployeeRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive custom repository implementation for the Employee entity.
 */
@SuppressWarnings("unused")
class EmployeeRepositoryInternalImpl extends SimpleR2dbcRepository<Employee, Long> implements EmployeeRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final DepartmentRowMapper departmentMapper;
    private final EmployeeRowMapper employeeMapper;

    private static final Table entityTable = Table.aliased("employee", EntityManager.ENTITY_ALIAS);
    private static final Table departmentTable = Table.aliased("department", "department");

    public EmployeeRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        DepartmentRowMapper departmentMapper,
        EmployeeRowMapper employeeMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Employee.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.departmentMapper = departmentMapper;
        this.employeeMapper = employeeMapper;
    }

    @Override
    public Flux<Employee> findAllBy(Pageable pageable) {
        return findAllBy(pageable, null);
    }

    @Override
    public Flux<Employee> findAllBy(Pageable pageable, Criteria criteria) {
        return createQuery(pageable, criteria).all();
    }

    RowsFetchSpec<Employee> createQuery(Pageable pageable, Criteria criteria) {
        List<Expression> columns = EmployeeSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(DepartmentSqlHelper.getColumns(departmentTable, "department"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(departmentTable)
            .on(Column.create("department_id", entityTable))
            .equals(Column.create("id", departmentTable));

        String select = entityManager.createSelect(selectFrom, Employee.class, pageable, criteria);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Employee> findAll() {
        return findAllBy(null, null);
    }

    @Override
    public Mono<Employee> findById(Long id) {
        return createQuery(null, where(EntityManager.ENTITY_ALIAS + ".id").is(id)).one();
    }

    private Employee process(Row row, RowMetadata metadata) {
        Employee entity = employeeMapper.apply(row, "e");
        entity.setDepartment(departmentMapper.apply(row, "department"));
        return entity;
    }

    @Override
    public <S extends Employee> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
