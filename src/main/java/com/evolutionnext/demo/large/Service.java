package com.evolutionnext.demo.large;

import java.util.concurrent.StructuredTaskScope;

public class Service {

    private final Repository repository;

    public Service(Repository repository) {
        this.repository = repository;
    }

    public String run() {
        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.<String>allSuccessfulOrThrow())) {
            var id = scope.fork(repository::persist);
            var employee = scope.fork(repository::find);
            scope.join();
            return String.format("Found id of %s and employee %s", id, employee);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
