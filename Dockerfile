# ============================================================
# Dockerfile - scaffold-calculator
# ------------------------------------------------------------
# Dviejų-etapų (multi-stage) build'as:
#   1. "builder" image'e su Maven'u — surenkame .jar failą
#   2. mažesnis "runtime" image'as — tik JRE + mūsų .jar
# Rezultatas: mažesnis final image (~200MB vs ~500MB).
# ============================================================

# --- Stage 1: build ---
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# Pirma kopijuojam tik pom.xml — Docker cache'uos dependency'es,
# jei pom.xml nepasikeitė (greitesnis perbūdavimas)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Tada kopijuojam šaltinius ir buildinam
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Stage 2: runtime ---
FROM eclipse-temurin:17-jre
WORKDIR /app

# Kopijuojam pabūdintą .jar iš builder etapo
COPY --from=builder /build/target/scaffold-calculator-0.0.1-SNAPSHOT.jar app.jar

# Render'as pateiks PORT env var; Spring Boot jį nuskaitys
EXPOSE 8080

# Paleidžiam aplikaciją
ENTRYPOINT ["java", "-jar", "app.jar"]
