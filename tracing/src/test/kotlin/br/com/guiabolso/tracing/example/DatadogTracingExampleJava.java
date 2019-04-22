package br.com.guiabolso.tracing.example;

import br.com.guiabolso.tracing.Tracer;
import br.com.guiabolso.tracing.engine.datadog.DatadogTracer;
import br.com.guiabolso.tracing.factory.TracerFactory;
import br.com.guiabolso.tracing.utils.DatadogUtils;
import datadog.trace.api.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;

public class DatadogTracingExampleJava {

    private static Logger logger = LoggerFactory.getLogger(DatadogTracer.class);

    public static void main(String[] args) throws InterruptedException {
        final Tracer tracer = TracerFactory.createTracerWithDatadog();
        final ExecutorService executor = Executors.newFixedThreadPool(2);

        // You don't need to use DatadogUtils.traceAsNewOperation when using servlet, it is automatic.
        DatadogUtils.traceAsNewOperation("simpleOperation", new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        tracer.addProperty("oneTag", "someValue1");
                        someWork();
                        return null;
                    }
                }
        );

        DatadogUtils.traceAsNewOperation("simpleOperationWithError", new Callable<Void>() {
                    @Override
                    public Void call() throws InterruptedException {
                        tracer.addProperty("oneTag", "someValue2");
                        someWorkWithError();
                        return null;
                    }
                }
        );

        DatadogUtils.traceAsNewOperation("simpleOperationWithErrorHandled", new Callable<Void>() {
                    @Override
                    public Void call() {
                        tracer.addProperty("oneTag", "someValue3");
                        try {
                            someWorkWithError();
                        } catch (Exception e) {
                            //Fallback
                            //In this scenario only the child span will be marked as error
                        }
                        return null;
                    }
                }
        );


        DatadogUtils.traceAsNewOperation("someAsyncOperation", new Callable<Void>() {
                    @Override
                    public Void call() throws InterruptedException {
                        tracer.addProperty("oneTag", "someValue4");
                        someWork();

                        tracer.executeAsync(executor, new Callable<Void>() {
                                    @Override
                                    public Void call() throws InterruptedException {
                                        someWork();
                                        return null;
                                    }
                                }
                        );
                        Thread.sleep(10);
                        tracer.executeAsync(executor, new Callable<Void>() {
                                    @Override
                                    public Void call() throws InterruptedException {
                                        someWork();
                                        return null;
                                    }
                                }
                        );
                        Thread.sleep(10);
                        tracer.executeAsync(executor, new Callable<Void>() {
                                    @Override
                                    public Void call() throws InterruptedException {
                                        someWorkWithError();
                                        return null;
                                    }
                                }
                        );
                        someWork();
                        return null;
                    }
                }

        );

        DatadogUtils.traceAsNewOperation("someAsyncOperationWithError", new Callable<Void>() {
                    @Override
                    public Void call() throws InterruptedException {
                        tracer.addProperty("oneTag", "someValue5");
                        someWork();

                        tracer.executeAsync(executor, new Callable<Void>() {
                                    @Override
                                    public Void call() throws InterruptedException {
                                        someWork();
                                        return null;
                                    }
                                }
                        );
                        Thread.sleep(10);
                        tracer.executeAsync(executor, new Callable<Void>() {
                                    @Override
                                    public Void call() throws InterruptedException {
                                        someWork();
                                        return null;
                                    }
                                }
                        );
                        Thread.sleep(10);
                        tracer.executeAsync(executor, new Callable<Void>() {
                                    @Override
                                    public Void call() throws InterruptedException {
                                        someWork();
                                        return null;
                                    }
                                }
                        );
                        someWorkWithError();
                        return null;
                    }
                }

        );


        executor.shutdown();
        executor.awaitTermination(10, SECONDS);
    }

    // If you want to change the name of this method span set 'operationName'.
    // Remember that unlike NewRelic's metrics '/' should not be used.
    @Trace
    private static void someWork() throws InterruptedException {
        logger.info("Starting some work");
        Thread.sleep(random());
        logger.info("Finished some work");
    }

    @Trace
    private static void someWorkWithError() throws InterruptedException {
        logger.info("Starting some work");
        Thread.sleep(random());
        throw new RuntimeException("This is an error.");
    }

    private static long random() {
        return (long) 100 + (long) (200 * Math.random());
    }

}
