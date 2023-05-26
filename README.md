# Warta Paroki

- Run docker stack local
    ```
    bash run-dev-local.sh
    ````

- Clear/reset database : 
    ```
    mvn flyway:clean
    ```

- Documentation API :
    - [Swagger](http://your_domain/api/manajemen-pengguna/swagger-ui/index.html)
    - [Actuator](http://your_domain/api/manajemen-pengguna/actuator)

- Environtment :
    ```
    SPRING_DATASOURCE_URL=jdbc:postgresql://your_db_svc_domain:your_db_port/wartaparoki
    SPRING_DATASOURCE_USERNAME=userAuth
    SPRING_DATASOURCE_PASSWORD=userAuthPass
    SPRING_JPA_HIBERNATE_DDL_AUTO=update
    SPRING_FLYWAY_ENABLED=false
    SPRING_REDIS_HOST=redisHost
    SPRING_REDIS_PORT=redisPort
    SPRING_REDIS_PASSWORD=redisPassword

    APPLICATION_TOKEN_SECRET=komsos_algoritm
    APPLICATION_REFRESH_TOKEN_COOKIE_NAME=komsos-VuVZ85
    APPLICATION_TOKEN_EXPIRATION_MILISEC=25000000
    APPLICATION_REFRESH_TOKEN_EXPIRATION_MILISEC=86400000
    APPLICATION_ALLOWED_CORS=http://localhost:3000

    SERVER_MAX_HTTP_HEADER_SIZE=40KB
    SERVER_PORT=80
    ```
    