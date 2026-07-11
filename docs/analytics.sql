-- ============================================
-- OrbitaMarket Analytics Queries
-- Статистика: кто сколько купил геокредитов
-- ============================================

-- 1. Основная статистика по пользователям
SELECT
    user_id,
    COUNT(*) AS paid_orders_count,
    SUM(price) AS total_spent_geocredits,
    AVG(price) AS avg_order_price,
    MIN(price) AS min_order_price,
    MAX(price) AS max_order_price
FROM orders
WHERE status = 'PAID'
GROUP BY user_id
ORDER BY total_spent_geocredits DESC;

-- 2. Детальная статистика по типам продуктов
SELECT
    user_id,
    product_type,
    COUNT(*) AS orders_count,
    SUM(price) AS total_amount,
    AVG(price) AS avg_order_price
FROM orders
WHERE status = 'PAID'
GROUP BY user_id, product_type
ORDER BY user_id, total_amount DESC;

-- 3. Активность пользователей за последние 30 дней
SELECT
    user_id,
    COUNT(*) AS total_orders,
    SUM(CASE WHEN status = 'PAID' THEN 1 ELSE 0 END) AS paid_orders,
    SUM(CASE WHEN status = 'PAYMENT_FAILED' THEN 1 ELSE 0 END) AS failed_orders,
    SUM(price) FILTER (WHERE status = 'PAID') AS total_spent
FROM orders
WHERE created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY user_id
ORDER BY total_spent DESC NULLS LAST;

-- 4. Статистика по статусам заказов
SELECT
    status,
    COUNT(*) AS count,
    SUM(price) AS total_amount,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) AS percentage
FROM orders
GROUP BY status
ORDER BY count DESC;

-- 5. Топ-10 пользователей по сумме покупок
SELECT
    user_id,
    COUNT(*) AS orders_count,
    SUM(price) AS total_spent,
    RANK() OVER (ORDER BY SUM(price) DESC) AS rank
FROM orders
WHERE status = 'PAID'
GROUP BY user_id
ORDER BY total_spent DESC
LIMIT 10;

-- 6. Ежемесячная статистика продаж
SELECT
    DATE_TRUNC('month', created_at) AS month,
    COUNT(*) AS total_orders,
    SUM(CASE WHEN status = 'PAID' THEN price ELSE 0 END) AS revenue,
    COUNT(DISTINCT user_id) AS unique_customers
FROM orders
GROUP BY DATE_TRUNC('month', created_at)
ORDER BY month DESC;

-- 7. Конверсия из созданных в оплаченные заказы
SELECT
    product_type,
    COUNT(*) AS created,
    SUM(CASE WHEN status = 'PAID' THEN 1 ELSE 0 END) AS paid,
    ROUND(
        SUM(CASE WHEN status = 'PAID' THEN 1 ELSE 0 END) * 100.0 / COUNT(*),
        2
    ) AS conversion_rate
FROM orders
GROUP BY product_type
ORDER BY conversion_rate DESC;

-- 8. Пользователи с наибольшим количеством отказов в оплате
SELECT
    user_id,
    COUNT(*) FILTER (WHERE status = 'PAYMENT_FAILED') AS failed_count,
    SUM(price) FILTER (WHERE status = 'PAYMENT_FAILED') AS failed_amount,
    COUNT(*) AS total_orders,
    ROUND(
        COUNT(*) FILTER (WHERE status = 'PAYMENT_FAILED') * 100.0 / COUNT(*),
        2
    ) AS failure_rate
FROM orders
GROUP BY user_id
HAVING COUNT(*) FILTER (WHERE status = 'PAYMENT_FAILED') > 0
ORDER BY failure_rate DESC;