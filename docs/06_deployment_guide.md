# Hướng Dẫn Triển Khai CareTalk Backend

> Tài liệu hướng dẫn triển khai hệ thống CareTalk Backend lên server Linux từ đầu.
> Yêu cầu: Server Linux (Ubuntu 22.04+) đã có kết nối mạng.

---

## Kiến trúc triển khai

```
┌──────────────────────────────────────────────────────┐
│                    LINUX SERVER                      │
│                                                      │
│  ┌──────────┐     ┌──────────────┐     ┌──────────┐ │
│  │  Nginx   │────>│  CareTalk    │────>│PostgreSQL│ │
│  │  :80/443 │     │  Spring Boot │     │  :5432   │ │
│  │  reverse  │     │  :8080       │     │ +pgvector│ │
│  │  proxy   │     └──────────────┘     └──────────┘ │
│  └──────────┘            │                           │
│                          │                           │
│                    ┌─────┴─────┐                     │
│                    │  OpenAI   │                     │
│                    │  API      │                     │
│                    └───────────┘                     │
│                    ┌───────────┐                     │
│                    │ Firebase  │                     │
│                    │ Auth/FCM  │                     │
│                    └───────────┘                     │
└──────────────────────────────────────────────────────┘
```

## Yêu cầu hệ thống

| Thành phần | Phiên bản | Ghi chú |
|-----------|-----------|---------|
| OS | Ubuntu 22.04 LTS trở lên | Hoặc Debian 12 |
| Java | JDK 21 | Spring Boot 4.0.5 yêu cầu |
| PostgreSQL | 16+ | Cần extension pgvector |
| RAM | Tối thiểu 2GB | Khuyến nghị 4GB |
| Disk | Tối thiểu 20GB | Cho DB + logs |
| Port | 8080, 5432, 80, 443 | App, DB, HTTP, HTTPS |

---

## Bước 1: Cập nhật hệ thống

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y curl wget git unzip software-properties-common
```

---

## Bước 2: Cài đặt Java 21

### Cài đặt OpenJDK 21

```bash
sudo apt install -y openjdk-21-jdk
```

Nếu Ubuntu chưa có gói OpenJDK 21, dùng Adoptium:

```bash
# Thêm Adoptium repository
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor -o /usr/share/keyrings/adoptium.gpg
echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/adoptium.list

sudo apt update
sudo apt install -y temurin-21-jdk
```

### Xác nhận cài đặt

```bash
java -version
# openjdk version "21.x.x"

javac -version
# javac 21.x.x
```

### Thiết lập JAVA_HOME

```bash
# Thêm vào /etc/environment
echo 'JAVA_HOME="/usr/lib/jvm/temurin-21-jdk-amd64"' | sudo tee -a /etc/environment
source /etc/environment

# Kiểm tra
echo $JAVA_HOME
```

---

## Bước 3: Cài đặt PostgreSQL 16 + pgvector

### Cài đặt PostgreSQL

```bash
# Thêm PostgreSQL repository
sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -

sudo apt update
sudo apt install -y postgresql-16 postgresql-contrib-16
```

### Kiểm tra dịch vụ

```bash
sudo systemctl status postgresql
sudo systemctl enable postgresql
```

### Cài đặt pgvector

pgvector là extension bắt buộc cho tính năng RAG (semantic search bằng cosine distance).

```bash
# Cài đặt pgvector cho PostgreSQL 16
sudo apt install -y postgresql-16-pgvector
```

Nếu không có gói sẵn, build từ source:

```bash
sudo apt install -y build-essential postgresql-server-dev-16
cd /tmp
git clone --branch v0.8.0 https://github.com/pgvector/pgvector.git
cd pgvector
make
sudo make install
```

### Tạo database và user

```bash
sudo -u postgres psql
```

Trong PostgreSQL shell:

```sql
-- Tạo user cho ứng dụng
CREATE USER caretalk_user WITH PASSWORD 'your_secure_password_here';

-- Tạo database
CREATE DATABASE caretalk_db OWNER caretalk_user;

-- Kết nối vào database
\c caretalk_db

-- Cài extension bắt buộc
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";

-- Cấp quyền
GRANT ALL PRIVILEGES ON DATABASE caretalk_db TO caretalk_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO caretalk_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO caretalk_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO caretalk_user;

-- Thoát
\q
```

### Cấu hình PostgreSQL cho kết nối từ ứng dụng

```bash
# Sửa file pg_hba.conf
sudo nano /etc/postgresql/16/main/pg_hba.conf
```

Thêm dòng (nếu app và DB cùng server):

```
# TYPE  DATABASE        USER            ADDRESS         METHOD
local   caretalk_db     caretalk_user                   md5
host    caretalk_db     caretalk_user   127.0.0.1/32    md5
```

```bash
# Khởi động lại PostgreSQL
sudo systemctl restart postgresql
```

### Khởi tạo schema

```bash
# Chạy file init_schema.sql từ source code
psql -U caretalk_user -d caretalk_db -f init_schema.sql
```

---

## Bước 4: Chuẩn bị Firebase Credentials

Dự án sử dụng Firebase cho xác thực (Auth) và thông báo (FCM).

### Lấy Service Account Key

1. Truy cập [Firebase Console](https://console.firebase.google.com)
2. Chọn dự án CareTalk
3. Vào **Project Settings** -> **Service accounts**
4. Nhấn **Generate new private key**
5. Tải file JSON về

### Đưa file lên server

```bash
# Tạo thư mục cấu hình
sudo mkdir -p /opt/caretalk/config

# Upload file (từ máy local)
scp firebase-service-account.json user@server:/opt/caretalk/config/

# Phân quyền
sudo chmod 600 /opt/caretalk/config/firebase-service-account.json
```

---

## Bước 5: Build ứng dụng

### Clone source code

```bash
sudo mkdir -p /opt/caretalk
cd /opt/caretalk
git clone <repository-url> app
cd app
```

### Build JAR file

```bash
# Cấp quyền thực thi cho Gradle Wrapper
chmod +x gradlew

# Build (bỏ qua test vì chưa có DB kết nối)
./gradlew bootJar -x test
```

File JAR được tạo tại:

```
build/libs/agile-chatbot-backend.jar
```

### Copy JAR ra thư mục triển khai

```bash
cp build/libs/agile-chatbot-backend.jar /opt/caretalk/caretalk-backend.jar
```

---

## Bước 6: Tạo file cấu hình môi trường

```bash
sudo nano /opt/caretalk/config/application-prod.properties
```

Nội dung:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/caretalk_db
spring.datasource.username=caretalk_user
spring.datasource.password=your_secure_password_here
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA - QUAN TRỌNG: dùng validate thay vì create trên production
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# OpenAI
caretalk.embedding.api-key=sk-your-openai-key-here
caretalk.embedding.base-url=https://api.openai.com/v1
caretalk.embedding.model=text-embedding-3-small

caretalk.summary.api-key=sk-your-openai-key-here
caretalk.summary.base-url=https://api.openai.com/v1
caretalk.summary.model=gpt-4o-mini

caretalk.llm.openai.api-key=sk-your-openai-key-here
caretalk.llm.openai.base-url=https://api.openai.com/v1

# Firebase
firebase.credentials-file=/opt/caretalk/config/firebase-service-account.json

# Anonymous Chat
caretalk.public.api-key=your-public-api-key-here
```

### Phân quyền bảo mật

```bash
sudo chmod 600 /opt/caretalk/config/application-prod.properties
```

---

## Bước 7: Tạo Systemd Service

Tạo service để ứng dụng tự khởi động và quản lý bằng systemd.

```bash
sudo nano /etc/systemd/system/caretalk.service
```

Nội dung:

```ini
[Unit]
Description=CareTalk Backend - Spring Boot Application
After=network.target postgresql.service
Requires=postgresql.service

[Service]
Type=simple
User=www-data
Group=www-data

WorkingDirectory=/opt/caretalk

ExecStart=/usr/bin/java \
    -Xms512m \
    -Xmx1024m \
    -jar /opt/caretalk/caretalk-backend.jar \
    --spring.profiles.active=prod \
    --spring.config.additional-location=file:/opt/caretalk/config/

Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=caretalk

# Biến môi trường
Environment=JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
Environment=FIREBASE_CREDENTIALS_FILE=/opt/caretalk/config/firebase-service-account.json

[Install]
WantedBy=multi-user.target
```

### Phân quyền thư mục

```bash
sudo chown -R www-data:www-data /opt/caretalk
```

### Kích hoạt service

```bash
sudo systemctl daemon-reload
sudo systemctl enable caretalk
sudo systemctl start caretalk
```

### Kiểm tra trạng thái

```bash
# Xem trạng thái
sudo systemctl status caretalk

# Xem log realtime
sudo journalctl -u caretalk -f

# Xem 50 dòng log gần nhất
sudo journalctl -u caretalk -n 50
```

---

## Bước 8: Cài đặt Nginx (Reverse Proxy)

### Cài đặt

```bash
sudo apt install -y nginx
```

### Tạo cấu hình

```bash
sudo nano /etc/nginx/sites-available/caretalk
```

Nội dung:

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # Redirect HTTP sang HTTPS (bật sau khi có SSL)
    # return 301 https://$server_name$request_uri;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Hỗ trợ SSE streaming (cho chat response)
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 300s;
        proxy_connect_timeout 75s;

        # WebSocket support (nếu cần)
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # Giới hạn kích thước upload (cho tính năng đính kèm file)
    client_max_body_size 10M;
}
```

### Kích hoạt site

```bash
sudo ln -s /etc/nginx/sites-available/caretalk /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

# Kiểm tra cú pháp
sudo nginx -t

# Khởi động
sudo systemctl restart nginx
sudo systemctl enable nginx
```

### Cài SSL với Certbot (tùy chọn)

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com

# Tự động gia hạn
sudo systemctl enable certbot.timer
```

---

## Bước 9: Cấu hình Firewall

```bash
sudo ufw allow ssh
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
# KHÔNG mở port 8080 và 5432 ra ngoài
sudo ufw enable
sudo ufw status
```

---

## Bước 10: Kiểm tra triển khai

### Kiểm tra ứng dụng

```bash
# Kiểm tra Spring Boot đã chạy
curl -s http://localhost:8080/api/v1/public/health || echo "Chưa có health endpoint"

# Kiểm tra qua Nginx
curl -s http://your-domain.com/api/v1/public/health
```

### Kiểm tra database

```bash
psql -U caretalk_user -d caretalk_db -c "SELECT count(*) FROM users;"
```

### Kiểm tra pgvector

```bash
psql -U caretalk_user -d caretalk_db -c "SELECT extname, extversion FROM pg_extension WHERE extname = 'vector';"
```

---

## Checklist triển khai

| # | Hạng mục | Trạng thái |
|---|---------|-----------|
| 1 | Cập nhật hệ thống | [ ] |
| 2 | Cài đặt Java 21 | [ ] |
| 3 | Cài đặt PostgreSQL 16 + pgvector | [ ] |
| 4 | Tạo database + schema | [ ] |
| 5 | Upload Firebase credentials | [ ] |
| 6 | Build JAR file | [ ] |
| 7 | Tạo file cấu hình production | [ ] |
| 8 | Tạo systemd service | [ ] |
| 9 | Cài đặt Nginx | [ ] |
| 10 | Cấu hình firewall | [ ] |
| 11 | Kiểm tra ứng dụng | [ ] |
| 12 | Cài SSL (tùy chọn) | [ ] |

---

## Các lệnh quản lý thường dùng

```bash
# Khởi động / dừng / khởi động lại ứng dụng
sudo systemctl start caretalk
sudo systemctl stop caretalk
sudo systemctl restart caretalk

# Xem log ứng dụng
sudo journalctl -u caretalk -f
sudo journalctl -u caretalk --since "1 hour ago"

# Khởi động / dừng database
sudo systemctl start postgresql
sudo systemctl stop postgresql

# Khởi động / dừng Nginx
sudo systemctl restart nginx

# Kiểm tra port đang lắng nghe
sudo ss -tlnp | grep -E '8080|5432|80|443'

# Kiểm tra dung lượng đĩa
df -h

# Kiểm tra memory
free -m
```

---

## Triển khai bản cập nhật

Khi có phiên bản mới:

```bash
cd /opt/caretalk/app

# Lấy code mới
git pull origin main

# Build lại
./gradlew bootJar -x test

# Thay thế JAR
cp build/libs/agile-chatbot-backend.jar /opt/caretalk/caretalk-backend.jar

# Khởi động lại
sudo systemctl restart caretalk

# Kiểm tra
sudo journalctl -u caretalk -f
```

---

## Xử lý sự cố

### Ứng dụng không khởi động

```bash
# Xem log chi tiết
sudo journalctl -u caretalk -n 100 --no-pager

# Nguyên nhân thường gặp:
# 1. Sai password database -> kiểm tra application-prod.properties
# 2. PostgreSQL chưa chạy -> sudo systemctl start postgresql
# 3. Port 8080 bị chiếm -> sudo ss -tlnp | grep 8080
# 4. Thiếu Firebase credentials -> kiểm tra đường dẫn file
# 5. Java version sai -> java -version
```

### Database lỗi kết nối

```bash
# Kiểm tra PostgreSQL đang chạy
sudo systemctl status postgresql

# Kiểm tra kết nối
psql -U caretalk_user -d caretalk_db -c "SELECT 1;"

# Kiểm tra pg_hba.conf
sudo cat /etc/postgresql/16/main/pg_hba.conf | grep caretalk
```

### Nginx trả về 502 Bad Gateway

```bash
# Ứng dụng chưa chạy hoặc đang khởi động
sudo systemctl status caretalk

# Kiểm tra port 8080 có đang lắng nghe
sudo ss -tlnp | grep 8080
```

### Hết bộ nhớ

```bash
# Kiểm tra memory
free -m

# Giảm heap size trong caretalk.service
# -Xms256m -Xmx512m (thay vì 512m/1024m)

# Khởi động lại
sudo systemctl daemon-reload
sudo systemctl restart caretalk
```
