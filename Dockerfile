# Usa uma imagem do Java 17 (se usar o 21, mude aqui)
FROM eclipse-temurin:17-jre-alpine

# Cria uma pasta de trabalho lá dentro
WORKDIR /app

# Copia o seu arquivo .jar compilado para dentro do contêiner
COPY target/*.jar app.jar

# Libera a porta
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]