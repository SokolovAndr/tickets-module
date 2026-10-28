package com.example.ticketsmodule.aop.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @Mock
    private Environment env;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;

    private LoggingAspect loggingAspect;

    @BeforeEach
    void setUp() {
        loggingAspect = new LoggingAspect(env);

        // Мокаем имя класса для логгера
        lenient().when(signature.getDeclaringTypeName()).thenReturn("com.example.ticketsmodule.aop.logging.DummyClass");
        lenient().when(signature.getName()).thenReturn("testMethod");
    }

    @Test
    void logAround_ShouldProceedAndReturnResult() throws Throwable {
        // Arrange
        when(proceedingJoinPoint.getSignature()).thenReturn(signature);
        // УБРАЛИ строку с getArgs(), так как она не вызывается без DEBUG логов
        when(proceedingJoinPoint.proceed()).thenReturn("success");

        // Act
        Object result = loggingAspect.logAround(proceedingJoinPoint);

        // Assert
        assertEquals("success", result);
        verify(proceedingJoinPoint).proceed();
    }

    @Test
    void logAround_ShouldThrowIllegalArgumentException() throws Throwable {
        // Arrange
        when(proceedingJoinPoint.getSignature()).thenReturn(signature);
        when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{});
        when(proceedingJoinPoint.proceed()).thenThrow(new IllegalArgumentException("Bad arg"));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> loggingAspect.logAround(proceedingJoinPoint));

        assertEquals("Bad arg", ex.getMessage());
    }

    @Test
    void logAfterThrowing_ShouldLogWithStackTrace_WhenDevProfile() {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(env.acceptsProfiles(any(Profiles.class))).thenReturn(true);
        Throwable ex = new RuntimeException("Test exception", new RuntimeException("Cause"));

        // Act
        loggingAspect.logAfterThrowing(joinPoint, ex);

        // Assert
        verify(env).acceptsProfiles(any(Profiles.class));
    }

    @Test
    void logAfterThrowing_ShouldLogWithoutStackTrace_WhenNotDevProfile() {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(env.acceptsProfiles(any(Profiles.class))).thenReturn(false);
        Throwable ex = new RuntimeException("Test exception");

        // Act
        loggingAspect.logAfterThrowing(joinPoint, ex);

        // Assert
        verify(env).acceptsProfiles(any(Profiles.class));
    }

    @Test
    void pointcuts_ShouldBeCalledForCoverage() {
        loggingAspect.springBeanPointcut();
        loggingAspect.applicationPackagePointcut();
    }
}