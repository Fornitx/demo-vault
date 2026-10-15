В драйвере r2dbc-postgresql поддержка динамической ротации учетных данных (credential rotation) реализована нативно. Вместо передачи статической строки пароля конфигуратор PostgresqlConnectionConfiguration позволяет передавать функцию Supplier<String> (для синхронного получения пароля) или Publisher<String> (для реактивного получения, например из Project Reactor). [1]
Ниже приведены практические примеры реализации ротации для чистого Java/R2DBC приложения и для интеграции в Spring Boot 3.
------------------------------
## Вариант 1: Использование Supplier или Publisher в конфигурации
Этот подход универсален и работает независимо от фреймворка. Драйвер будет вызывать метод get() у Supplier или подписываться на Publisher каждый раз, когда пулу соединений требуется создать новое физическое подключение к PostgreSQL.
```java
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Mono;
import java.util.function.Supplier;
public class R2dbcConfig {

    public ConnectionFactory connectionFactory() {
        // Пример 1: Синхронный поставщик (Supplier)
        Supplier<String> passwordSupplier = () -> {
            // Здесь логика получения пароля (из Vault, AWS Secrets Manager, K8s ConfigMap и т.д.)
            return fetchLatestPasswordFromSecretManager();
        };

        // Пример 2: Реактивный поставщик (Publisher/Mono)
        Mono<String> passwordPublisher = Mono.fromSupplier(() -> fetchLatestPasswordFromSecretManager())
                .cache(); // Опционально кешируйте, если не хотите спамить хранилище секретов на каждый запрос

        return new PostgresqlConnectionFactory(
            PostgresqlConnectionConfiguration.builder()
                .host("localhost")
                .port(5432)
                .database("mydb")
                .username("db_user")
                // Драйвер принимает Supplier<String> или Publisher<String> напрямую в метод password()
                .password(passwordSupplier) 
                .build()
        );
    }

    private String fetchLatestPasswordFromSecretManager() {
        // Реальный код вызова внешнего API хранилища секретов
        return "dynamic-rotated-password";
    }
}
```
------------------------------
## Вариант 2: Интеграция с Spring Boot и Spring Data R2DBC
Если вы используете Spring Boot, автоматическая конфигурация ожидает фиксированную строку. Чтобы заставить пул (R2dbcPool) перевыпускать соединения с новым паролем без перезапуска приложения, нужно переопределить создание бина ConnectionFactory.
## 1. Создаем кастомный ConnectionFactory с реактивной ротацией
Компонент, отвечающий за поставку учетных данных (например, интегрированный с Vault или обновляемый по расписанию):
```java
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@Configurationpublic class DatabaseConfiguration {

    // Эмулируем кэш для пароля, который обновляется раз в N минут
    private final AtomicReference<String> cachedPassword = new AtomicReference<>("initial_password");

    @Bean
    public ConnectionFactory connectionFactory() {
        // Реактивный источник пароля. Драйвер будет запрашивать его для каждого НОВОГО коннекта
        Mono<String> passwordPublisher = Mono.defer(() -> Mono.just(cachedPassword.get()));

        // Настраиваем базовый фабричный метод PostgreSQL
        PostgresqlConnectionFactory connectionFactory = new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host("localhost")
                        .port(5432)
                        .database("my_database")
                        .username("my_user")
                        .password(passwordPublisher) // Передаем реактивный Mono
                        .build()
        );

        // Оборачиваем в пул соединений R2DBC Pool
        ConnectionPoolConfiguration poolConfiguration = ConnectionPoolConfiguration.builder(connectionFactory)
                .maxSize(20)
                // Важно: старые соединения со старым паролем должны со временем вытесняться (eviction)
                .maxIdleTime(Duration.ofMinutes(5)) 
                .maxLifeTime(Duration.ofMinutes(30))
                .build();

        return new ConnectionPool(poolConfiguration);
    }
    
    // Метод, который будет вызываться вашим планировщиком или слушателем событий (например, Vault/AWS)
    public void updatePassword(String newPassword) {
        this.cachedPassword.set(newPassword);
    }
}
```
## 2. Как заставить пул мгновенно сбросить старые соединения?
Если ротация произошла на стороне СУБД, старые соединения в пуле (которые все еще используют старый пароль) начнут выдавать ошибки аутентификации при проверке (validation) или выполнении запросов.
Чтобы избежать этого, после вызова updatePassword(newPassword) рекомендуется принудительно очистить пул:
```java
import io.r2dbc.pool.ConnectionPool;import io.r2dbc.spi.ConnectionFactory;import org.springframework.stereotype.Service;

@Servicepublic class CredentialRotationService {

    private final ConnectionFactory connectionFactory;
    private final DatabaseConfiguration databaseConfiguration;

    public CredentialRotationService(ConnectionFactory connectionFactory, DatabaseConfiguration databaseConfiguration) {
        this.connectionFactory = connectionFactory;
        this.databaseConfiguration = databaseConfiguration;
    }

    public void rotateCredentials(String newPassword) {
        // 1. Обновляем пароль в провайдере
        databaseConfiguration.updatePassword(newPassword);

        // 2. Сбрасываем пул (закрываем idle соединения), чтобы новые создавались с новым паролем
        if (connectionFactory instanceof ConnectionPool connectionPool) {
            // close() уничтожает текущий пул. 
            // При следующем запросе к базе данных пул создаст новые соединения, используя обновленный Supplier/Publisher.
            connectionPool.close().subscribe(); 
        }
    }
}
```
## Важные нюансы при работе с пулами:

1. Жизненный цикл соединений (maxLifeTime): Обязательно настраивайте параметры вытеснения в пуле. Соединения, созданные со старым паролем, остаются валидными в PostgreSQL до тех пор, пока они открыты. Ротация влияет только на новые физические сессии.
2. Параметр проверки (validationQuery): Включайте валидацию соединений при выделении из пула. Если СУБД жестко оборвала сессии при смене пароля, пул сразу поймет, что соединение «мертво», утилизирует его и запросит у драйвера новое (сработает ваш Supplier с новым актуальным паролем).

Уточните, откуда именно вы планируете забирать обновленные пароли: из встроенного механизма Spring Cloud Vault, AWS Secrets Manager, по событию из Kubernetes ConfigMap или с помощью IAM/OAuth токенов? Я могу адаптировать код под конкретный облачный или инфраструктурный провайдер.

[1] [https://github.com](https://github.com/pgjdbc/r2dbc-postgresql/blob/main/README.md)
