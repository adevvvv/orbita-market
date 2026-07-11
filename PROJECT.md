# OrbitaMarket Project Plan

## Цель проекта
Разработка высоконагруженной платформы для заказа спутниковых продуктов (архивные снимки, tasking, мониторинг) с биллингом в геокредитах и согласованным списанием при пиковой нагрузке.

## Стейкхолдеры
1. **Операторы ДЗЗ** - заказчики спутниковых снимков для дистанционного зондирования Земли
2. **Аналитические компании** - потребители геопространственных данных для аналитики
3. **Администраторы платформы** - управление системой, мониторинг, решение инцидентов

## Roadmap

### Этап 1: Проектирование и Payments Service
- [x] Проектирование архитектуры и C4 диаграмм
- [x] Базовая структура проекта (Gradle, Spring Boot)
- [x] Создание и пополнение счетов (POST /accounts, /top-up)
- [x] Просмотр баланса (GET /balance)
- [x] Коды ошибок и exception handling

### Этап 2: Orders Service и бизнес-логика
- [x] Создание заказов ARCHIVE, TASKING, MONITORING
- [x] Жизненный цикл: CREATED → PAYMENT_PENDING → PAID/FAILED
- [x] Валидация и коды ошибок (INVALID_PRICE, INVALID_PAYLOAD, ORDER_NOT_FOUND)

### Этап 3: Асинхронное взаимодействие
- [x] Kafka конфигурация и топики
- [x] Transactional Outbox в Orders Service
- [x] Idempotent Inbox в Payments Service
- [x] Exactly-once списание по order_id
- [x] Optimistic Locking (@Version) для конкурентности

### Этап 4: Инфраструктура
- [x] Docker Compose (7 контейнеров)
- [x] API Gateway (Spring Cloud Gateway)
- [x] Раздельные БД PostgreSQL для сервисов
- [x] Конфигурация Kafka + Zookeeper

### Этап 5: Тестирование
- [x] Автотесты на RestAssured (15 тестов)
- [x] Allure отчет (100% пройдено)
- [x] Чек-лист сценариев (47 проверок)
- [x] Отдельный репозиторий автотестов

### Этап 6: Безопасность
- [x] Gitleaks сканирование (0 утечек)
- [x] Semgrep SAST (0 находок, 225 правил)
- [x] Таблица триажа ИБ (7 строк)
- [x] Dockerfile с USER non-root
- [x] CORS ограничения и Rate limiting
- [x] Пароли в .env (не в коде)

### Этап 7: Документация
- [x] README с полной документацией
- [x] C4 диаграммы (C1 System Context, C2 Containers)
- [x] SQL аналитика (8 запросов)
- [x] API примеры (curl)
- [x] Таблица кодов ошибок

### Следующие шаги (post-MVP)
- [ ] Frontend на React/Vue.js
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Метрики и мониторинг (Prometheus + Grafana)
- [ ] Аутентификация и авторизация
- [ ] Production деплой

## Ключевые метрики
- ✅ Тесты: 15/15 PASS (100%)
- ✅ Безопасность: 0 уязвимостей
- ✅ Конкурентность: Optimistic Locking
- ✅ Надежность: Outbox + Inbox + Exactly-once