# Dictionary Service — Medicology

Dịch vụ từ điển y khoa cho Medicology: cung cấp thuật ngữ, mã hoá (ví dụ ICD), và dữ liệu tham chiếu khác để chuẩn hoá nội dung, không lưu trữ PHI.

## Công nghệ
- Spring Boot 3.3, Java 17
- Spring Data JPA (RDBMS)
- REST API qua API Gateway

## Tính năng chính
- Tra cứu/đề xuất thuật ngữ, mã bệnh, thuốc (tuỳ bộ dữ liệu sẵn có).
- Quản lý phiên bản tập dữ liệu từ điển; cơ chế cập nhật/đồng bộ (tuỳ triển khai).
- Cung cấp API chỉ đọc (read-mostly) phục vụ các service khác (Assessment, Learning, Website,...).

## Yêu cầu
- Java 17, Maven 3.9+
- CSDL quan hệ (cấu hình qua `spring.datasource.*`)

## Cấu hình môi trường (ví dụ)
- `SPRING_PROFILES_ACTIVE=dev`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/medicology_dict`
- `SPRING_DATASOURCE_USERNAME=...`
- `SPRING_DATASOURCE_PASSWORD=...`
- Tuỳ chọn: `SERVER_PORT=8082`

## Chạy local
```bash
mvn spring-boot:run
```
Hoặc build JAR:
```bash
mvn clean package -DskipTests
java -jar target/*.jar
```

## Kiểm thử
```bash
mvn verify -q
```

## Bảo mật & tuân thủ
- Không lưu trữ hoặc trả về PHI; chỉ dữ liệu tham chiếu.
- Thông điệp lỗi không để lộ chi tiết nội bộ; theo dõi Correlation-Id qua Gateway.

## Tài liệu API
- Khi bật OpenAPI (springdoc), có thể truy cập Swagger UI và spec OpenAPI. Đường dẫn tùy cấu hình.
