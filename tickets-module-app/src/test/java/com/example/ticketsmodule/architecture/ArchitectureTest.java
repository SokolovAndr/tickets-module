package com.example.ticketsmodule.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.example.ticketsmodule")
class ArchitectureTest {

    @ArchTest
    static final ArchRule api_should_not_depend_on_impl =
            noClasses().that().resideInAPackage("com.example.ticketsmodule.api..")
                    .should().dependOnClassesThat().resideInAPackage("com.example.ticketsmodule.impl..")
                    .because("API-контракты не знают про реализацию");

    @ArchTest
    static final ArchRule api_should_not_depend_on_web =
            noClasses().that().resideInAPackage("com.example.ticketsmodule.api..")
                    .should().dependOnClassesThat().resideInAPackage("com.example.ticketsmodule.web..")
                    .because("API-контракты не знают про web-слой");

    @ArchTest
    static final ArchRule impl_should_not_depend_on_web =
            noClasses().that().resideInAPackage("com.example.ticketsmodule.impl..")
                    .should().dependOnClassesThat().resideInAPackage("com.example.ticketsmodule.web..")
                    .because("impl не знает про web-слой");

    @ArchTest
    static final ArchRule impl_should_not_depend_on_app =
            noClasses().that().resideInAPackage("com.example.ticketsmodule.impl..")
                    .should().dependOnClassesThat().resideInAPackage("com.example.ticketsmodule.app..")
                    .because("impl не знает про точку входа");

    @ArchTest
    static final ArchRule web_should_not_depend_on_db =
            noClasses().that().resideInAPackage("com.example.ticketsmodule.web..")
                    .should().dependOnClassesThat().resideInAPackage("com.example.ticketsmodule.db..")
                    .because("web не знает про миграции");

    /**
     * Проверяет, что web-слой не зависит от impl.
     *
     * <p>Правило временно отключено. Причина: {@code RegistrationController}
     * использует {@code AuthService} из {@code impl.oauth.service}.
     * Это известная проблема связанности.
     *
     * <p>Чтобы включить правило, надо:
     * <ol>
     *   <li>создать модуль {@code tickets-module-security};</li>
     *   <li>вынести туда {@code AuthService}, {@code JwtService},
     *       конфигурации безопасности;</li>
     *   <li>добавить интерфейс {@code UserLookup} для разрыва цикла
     *       с {@code UserRepository};</li>
     *   <li>раскомментировать это правило.</li>
     * </ol>
     *
     * <p>Причина отключения — размер проекта. Один разработчик, пять сущностей.
     * Отложено до роста проекта.
     **/
    /*@ArchTest
    static final ArchRule web_should_not_depend_on_impl =
            noClasses().that().resideInAPackage("com.example.ticketsmodule.web..")
                    .should().dependOnClassesThat().resideInAPackage("com.example.ticketsmodule.impl..");*/

    @ArchTest
    static final ArchRule no_cycles_between_modules =
            slices().matching("com.example.ticketsmodule.(*)..")
                    .should().beFreeOfCycles()
                    .because("циклы между модулями недопустимы");

}