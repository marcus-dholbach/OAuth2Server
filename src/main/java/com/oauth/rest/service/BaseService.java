package com.oauth.rest.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

public abstract class BaseService<T, ID, R extends JpaRepository<T, ID>> {

    protected final R repository;

    protected BaseService(R repository) {
        this.repository = repository;
    }

    @Transactional
    public @NonNull T save(@NonNull T entity) {
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public @NonNull Optional<T> findById(@NonNull ID id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public @NonNull List<T> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public @NonNull Page<T> findAll(@NonNull Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional
    public @NonNull T update(@NonNull T entity) {
        return repository.save(entity);
    }

    @Transactional
    public void delete(@NonNull T entity) {
        repository.delete(entity);
    }

    @Transactional
    public void deleteById(@NonNull ID id) {
        repository.deleteById(id);
    }
}