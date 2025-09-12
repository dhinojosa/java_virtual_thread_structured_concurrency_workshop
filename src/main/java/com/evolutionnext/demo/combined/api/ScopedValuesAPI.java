package com.evolutionnext.demo.combined.api;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.StructuredTaskScope;

public class ScopedValuesAPI {
    private static final ScopedValue<String> GREETING_KEY = ScopedValue.newInstance();
    private static final ScopedValue<String> FAREWELL_KEY = ScopedValue.newInstance();

    /**
     * The main method that calls various API calls
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        whereAndGet();
        whereAndCall();
        whereAndRun();
        doubleWhereAndCall();
        isBound();
    }

    /**
     * `where` returns a `Carrier`, which holds the mappings
     * for ScopedValue per thread.
     * `get` calls a `Supplier` of results, it in fact just get translated into a
     * `call`.
     */
    private static void whereAndGet() {
        var outerResult = ScopedValue.where(GREETING_KEY, "Hello").get(GREETING_KEY);
        System.out.println(outerResult);
    }

    /**
     * `where` returns a `Carrier`, which holds the mappings
     * for ScopedValue per thread.
     * `call` takes a `Callable`
     */
    private static void whereAndCall() throws Exception {
        String outerResult = ScopedValue.where(GREETING_KEY, "Bon Jour").call(() -> {
            try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.<String>anySuccessfulResultOrThrow())) {
                var result = scope.fork(() -> String.format("%s!", GREETING_KEY.get()));
                scope.join();
                return result.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(outerResult);
    }

    /**
     * `where` returns a `Carrier`, which holds the mappings
     * for ScopedValue per thread.
     * `run` takes a `Runnable`
     */
    private static void whereAndRun() {
        ScopedValue.where(GREETING_KEY, "Здравейте").run(printGreetingKey());
    }

    @NotNull
    private static Runnable printGreetingKey() {
        return () -> {
            try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.<String>allSuccessfulOrThrow())) {
                var result = scope.fork(() -> String.format("%s!", GREETING_KEY.get()));
                scope.join();
                System.out.println(result.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static void doubleWhereAndCall() throws Exception {
        String outerResult = ScopedValue.where(GREETING_KEY, "Γειά σου")
            .where(FAREWELL_KEY, "Antio sas").call(() -> {
            try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.<String>allSuccessfulOrThrow())) {
                var helloSubtask = scope.fork(() -> String.format("%s!", GREETING_KEY.get()));
                var goodbyeSubtask = scope.fork(() -> String.format("%s!", FAREWELL_KEY.get()));
                scope.join();
                return String.format("%s %s", helloSubtask.get(), goodbyeSubtask.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(outerResult);
    }

    /**
     * You can use the `ScopedValue#isBound` to determine if the key is bound
     * at a position in your code
     *
     * @throws Exception when the call cannot complete
     */
    private static void isBound() throws Exception {
        String outerResult = ScopedValue.where(GREETING_KEY, "你好").call(() -> {
            try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.<String>allSuccessfulOrThrow())) {
                var helloSubtask = scope.fork(() -> String.format("%s!", GREETING_KEY.get()));
                scope.join();
                GREETING_KEY.isBound();
                return String.format("%s", helloSubtask.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(outerResult);
    }
}
