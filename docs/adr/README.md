# Architecture Decision Records

Список ключевых архитектурных решений проекта. Каждый ADR фиксирует
одно решение: контекст, само решение, последствия и отклонённые
альтернативы.

| №                                     | Решение | Статус |
|---------------------------------------|---|---|
| [0001](0001-multimodule-structure.md) | Разбиение на Maven-модули | Accepted |
| [0002](0002-jpa-as-domain.md)         | JPA-сущности как доменные модели | Accepted |
| [0003](0003-jwt-and-sessions.md)      | Две цепочки Security: session + JWT | Accepted |
| [0004](0004-caching-strategy.md)      | Redis для кэша купленных билетов | Accepted |
| [0005](0005-web-impl-coupling.md)     | Отклонение от правила web → impl | Accepted |

## Формат

Каждый ADR использует классическую структуру (Nygard):
**Context → Decision → Consequences → Alternatives**.

Новые ADR не редактируют старые. Если решение изменилось —
пишется новый ADR, а старый помечается `Superseded by NNNN`.