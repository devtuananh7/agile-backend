# CareTalk Backend

Hệ thống backend cho ứng dụng CareTalk — chatbot tư vấn sức khỏe sử dụng AI (OpenAI) với xác thực Firebase.

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 4.0 |
| Language | Java 25 |
| Database | PostgreSQL + pgvector |
| Auth | Firebase Authentication |
| AI/LLM | OpenAI GPT-4o (streaming SSE) |
| Migration | Flyway |

## Architecture

```
Android App ──(Firebase ID Token)──▶ Nginx :443 ──▶ Spring Boot :8080
                                                        │
                                        ┌───────────────┼───────────────┐
                                        ▼               ▼               ▼
                                   PostgreSQL     OpenAI API      Firebase Auth
                                   (local)        (outbound)      (verify token)
```

---

## Yêu cầu hệ thống

- Java 21+ (khuyến nghị JDK 25)
- PostgreSQL 15+ (với extension `pgvector`)
- Nginx (reverse proxy + SSL)
- Firebase project (Authentication enabled)
- OpenAI API key

---

## Quick Start (Local Development)

### 1. Clone & Build

```bash
git clone <repo-url>
cd agile-chatbot-backend

# Build (skip tests nếu chưa có Firebase credentials)
./gradlew bootJar -x test
```

### 2. PostgreSQL Setup

```sql
CREATE DATABASE caretalk_db;
```

### 3. Firebase Credentials

Tải `serviceAccountKey.json` từ Firebase Console:
- Vào [Firebase Console](https://console.firebase.google.com) → Project Settings → Service Accounts
- Click **"Generate new private key"** → download file JSON

### 4. Environment Variables

```bash
export OPENAI_API_KEY=sk-proj-xxxxxxxxxxxxx
export FIREBASE_CREDENTIALS_FILE=/path/to/serviceAccountKey.json
```

### 5. Run

```bash
./gradlew bootRun
```

App chạy tại `http://localhost:8080`

---

## Production Deployment Guide

### Bước 1: Chuẩn bị thư mục trên server

```bash
ssh user@your-server-ip

sudo mkdir -p /var/caretalk/{app,config,logs,scripts}
sudo chown -R $USER:$USER /var/caretalk
chmod 700 /var/caretalk/config
```

Cấu trúc thư mục:

```
/var/caretalk/
├── app/
│   └── caretalk-backend.jar        ← Spring Boot fat JAR
├── config/
│   ├── application-prod.properties ← Production config
│   ├── serviceAccountKey.json      ← Firebase credentials
│   └── .env                        ← API keys (env vars)
├── logs/
│   └── caretalk.log                ← Application logs
└── scripts/
    ├── start.sh                    ← Start script
    └── stop.sh                     ← Stop script
```

### Bước 2: PostgreSQL

```bash
sudo -u postgres psql
```

```sql
CREATE DATABASE caretalk_db;
CREATE USER caretalk_user WITH PASSWORD 'YOUR_STRONG_PASSWORD_HERE';
GRANT ALL PRIVILEGES ON DATABASE caretalk_db TO caretalk_user;

-- PostgreSQL 15+ cần thêm quyền schema
\c caretalk_db
GRANT ALL ON SCHEMA public TO caretalk_user;

-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;
\q
```

> **Bảo mật:** Đảm bảo `pg_hba.conf` chỉ cho phép local connections:
> ```
> local   all   caretalk_user   md5
> host    all   caretalk_user   127.0.0.1/32   md5
> ```

### Bước 3: Đặt file credentials

**3a. Firebase Service Account Key:**

```bash
# Copy từ máy local lên server
scp ~/path/to/serviceAccountKey.json user@your-server-ip:/var/caretalk/config/

# Bảo vệ file
chmod 600 /var/caretalk/config/serviceAccountKey.json
```

**3b. Tạo file production config:**

```bash
cat > /var/caretalk/config/application-prod.properties << 'EOF'
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/caretalk_db
spring.datasource.username=caretalk_user
spring.datasource.password=YOUR_STRONG_DB_PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.hikari.maximum-pool-size=10

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# Firebase
firebase.credentials-file=/var/caretalk/config/serviceAccountKey.json

# OpenAI
caretalk.embedding.api-key=${OPENAI_API_KEY}
caretalk.embedding.base-url=https://api.openai.com/v1
caretalk.embedding.model=text-embedding-3-small

caretalk.summary.api-key=${OPENAI_API_KEY}
caretalk.summary.base-url=https://api.openai.com/v1
caretalk.summary.model=gpt-4o-mini

caretalk.llm.openai.api-key=${OPENAI_API_KEY}
caretalk.llm.openai.base-url=https://api.openai.com/v1

# Logging
logging.file.path=/var/caretalk/logs
logging.level.root=INFO
logging.level.vn.hust.agilechatbotbackend=INFO
EOF

chmod 600 /var/caretalk/config/application-prod.properties
```

**3c. Tạo file environment variables:**

```bash
cat > /var/caretalk/config/.env << 'EOF'
OPENAI_API_KEY=sk-proj-xxxxxxxxxxxxxxxxxxxxxxxxxxxx
EOF

chmod 600 /var/caretalk/config/.env
```

### Bước 4: Build & Upload JAR

Trên máy local:

```bash
cd agile-chatbot-backend
./gradlew bootJar -x test

scp build/libs/agile-chatbot-backend-0.0.1-SNAPSHOT.jar \
    user@your-server-ip:/var/caretalk/app/caretalk-backend.jar
```

### Bước 5: Tạo Startup Scripts

**start.sh:**

```bash
cat > /var/caretalk/scripts/start.sh << 'SCRIPT'
#!/bin/bash
set -e

APP_DIR=/var/caretalk
JAR_FILE=$APP_DIR/app/caretalk-backend.jar
CONFIG_FILE=$APP_DIR/config/application-prod.properties
LOG_FILE=$APP_DIR/logs/caretalk.log
PID_FILE=$APP_DIR/app/caretalk.pid

# Load env vars
if [ -f "$APP_DIR/config/.env" ]; then
    export $(grep -v '^#' "$APP_DIR/config/.env" | xargs)
fi

# Check if already running
if [ -f "$PID_FILE" ] && kill -0 $(cat "$PID_FILE") 2>/dev/null; then
    echo "CareTalk is already running (PID: $(cat $PID_FILE))"
    exit 1
fi

echo "Starting CareTalk Backend..."
nohup java \
    -Xms256m -Xmx512m \
    -Dspring.config.additional-location=file:$CONFIG_FILE \
    -Dspring.profiles.active=prod \
    -jar "$JAR_FILE" \
    >> "$LOG_FILE" 2>&1 &

echo $! > "$PID_FILE"
echo "CareTalk started (PID: $(cat $PID_FILE))"
echo "Logs: tail -f $LOG_FILE"
SCRIPT

chmod +x /var/caretalk/scripts/start.sh
```

**stop.sh:**

```bash
cat > /var/caretalk/scripts/stop.sh << 'SCRIPT'
#!/bin/bash
PID_FILE=/var/caretalk/app/caretalk.pid

if [ ! -f "$PID_FILE" ]; then
    echo "CareTalk is not running"
    exit 0
fi

PID=$(cat "$PID_FILE")
if kill -0 $PID 2>/dev/null; then
    echo "Stopping CareTalk (PID: $PID)..."
    kill $PID
    sleep 5
    if kill -0 $PID 2>/dev/null; then
        kill -9 $PID
    fi
    rm -f "$PID_FILE"
    echo "CareTalk stopped"
else
    echo "CareTalk is not running"
    rm -f "$PID_FILE"
fi
SCRIPT

chmod +x /opt/caretalk/scripts/stop.sh
```

### Bước 6: Systemd Service (Auto-start khi reboot)

```bash
sudo tee /etc/systemd/system/caretalk.service << 'SERVICE'
[Unit]
Description=CareTalk Backend
After=network.target postgresql.service
Requires=postgresql.service

[Service]
Type=forking
User=caretalk
Group=caretalk
WorkingDirectory=/opt/caretalk
EnvironmentFile=/opt/caretalk/config/.env
ExecStart=/opt/caretalk/scripts/start.sh
ExecStop=/opt/caretalk/scripts/stop.sh
PIDFile=/opt/caretalk/app/caretalk.pid
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
SERVICE

sudo systemctl daemon-reload
sudo systemctl enable caretalk
sudo systemctl start caretalk
sudo systemctl status caretalk
```

### Bước 7: Nginx Reverse Proxy (HTTPS)

```bash
sudo apt install nginx certbot python3-certbot-nginx -y
```

```bash
sudo tee /etc/nginx/sites-available/caretalk << 'NGINX'
server {
    listen 80;
    server_name api.caretalk.yourdomain.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.caretalk.yourdomain.com;

    # Security headers
    add_header X-Content-Type-Options nosniff;
    add_header X-Frame-Options DENY;
    add_header X-XSS-Protection "1; mode=block";

    client_max_body_size 10M;

    # Proxy to Spring Boot
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # SSE support (chatbot streaming)
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 120s;
    }
}
NGINX

sudo ln -sf /etc/nginx/sites-available/caretalk /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx

# SSL certificate (cần DNS trỏ domain trước)
sudo certbot --nginx -d api.caretalk.yourdomain.com
```

### Bước 8: Firewall

```bash
sudo ufw allow 22/tcp     # SSH
sudo ufw allow 80/tcp     # HTTP (redirect)
sudo ufw allow 443/tcp    # HTTPS
sudo ufw deny 8080/tcp    # Block direct Java access
sudo ufw deny 5432/tcp    # Block direct PostgreSQL access
sudo ufw enable
```

> **Lưu ý:** Firebase KHÔNG cần gọi ngược vào server. Server verify token bằng outbound call tới Google. Chỉ cần server có outbound internet access.

---

## Tạo Admin Account

Admin phải tạo thủ công. Các role khác (Patient, Doctor) tự động.

### 1. Tạo Firebase account cho admin

Vào [Firebase Console](https://console.firebase.google.com) → Authentication → Users → Add user

### 2. Copy Firebase UID

Sau khi tạo, copy cột **User UID** (dạng `abc123xyz...`)

### 3. INSERT vào PostgreSQL

```sql
INSERT INTO users (id, firebase_uid, email, role, status, auth_provider, created_at)
VALUES (
    gen_random_uuid(),
    'PASTE_FIREBASE_UID_HERE',
    'admin@caretalk.vn',
    'ADMIN',
    'ACTIVE',
    'password',
    NOW()
);
```

> **Quan trọng:** INSERT admin vào DB **TRƯỚC KHI** admin login lần đầu. Nếu admin đã login (bị auto-create thành PATIENT), dùng:
> ```sql
> UPDATE users SET role = 'ADMIN' WHERE firebase_uid = 'PASTE_FIREBASE_UID_HERE';
> ```

---

## API Endpoints

### Authentication
Tất cả endpoints yêu cầu Firebase ID Token trong header:
```
Authorization: Bearer <firebase-id-token>
```

### User
| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | `/api/v1/users/me` | Any | Lấy profile user hiện tại |
| PUT | `/api/v1/users/me` | Any | Cập nhật profile (phone, metadata) |
| POST | `/api/v1/users/register-firebase` | Any | Đăng ký thông tin bổ sung |
| PUT | `/api/v1/users/doctor/profile` | DOCTOR | Doctor cập nhật profile |

### Chatbot
| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `/api/v1/chatbot/chat` | Any | Chat với AI bot (SSE streaming) |

### Conversations
| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | `/api/v1/conversations` | Any | Danh sách cuộc hội thoại |
| GET | `/api/v1/conversations/{id}` | Any | Chi tiết cuộc hội thoại |
| GET | `/api/v1/conversations/{id}/messages` | Any | Lịch sử tin nhắn |
| PUT | `/api/v1/conversations/{id}/escalate` | Any | Chuyển sang bác sĩ |
| PUT | `/api/v1/conversations/{id}/close` | Any | Đóng cuộc hội thoại |

### Admin
| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `/api/v1/admin/users/doctor` | ADMIN | Tạo tài khoản bác sĩ |
| PATCH | `/api/v1/admin/users/{id}/approve` | ADMIN | Phê duyệt bác sĩ |

---

## Verify Deployment

```bash
# 1. App running
sudo systemctl status caretalk

# 2. Logs
tail -f /opt/caretalk/logs/caretalk.log

# 3. Health check (local)
curl http://localhost:8080/actuator/health

# 4. HTTPS (external)
curl https://api.caretalk.yourdomain.com/api/v1/users/me
# Expected: 401 (no token)

# 5. With Firebase token
curl -X GET https://api.caretalk.yourdomain.com/api/v1/users/me \
     -H "Authorization: Bearer YOUR_FIREBASE_ID_TOKEN"
# Expected: 200 with user profile
```

---

## Troubleshooting

| Vấn đề | Nguyên nhân | Giải pháp |
|--------|-------------|-----------|
| `Firebase credentials not configured` | Thiếu serviceAccountKey.json | Kiểm tra path trong `application-prod.properties` |
| `Connection refused :5432` | PostgreSQL chưa chạy | `sudo systemctl start postgresql` |
| `403 Forbidden` trên admin endpoint | User không có role ADMIN | UPDATE role trong DB |
| SSE timeout | Nginx buffer | Đảm bảo `proxy_buffering off` trong nginx config |
| `OPENAI_API_KEY` empty | .env không được load | Kiểm tra `.env` file và `EnvironmentFile` trong systemd |

---

## Security Notes

- **OpenAI API Key** chỉ tồn tại server-side, không bao giờ expose ra client
- **Firebase credentials** JSON phải `chmod 600`, không commit vào git
- **Database password** nằm trong config file `chmod 600`
- **PostgreSQL** chỉ chấp nhận local connections
- **Port 8080** blocked từ external, chỉ truy cập qua Nginx HTTPS
