# 해커톤 4팀 — Backend

백엔드 레포입니다. 프론트엔드는 별도 레포에 있습니다.

처음 보시는 분은 **[GitHub 접근 권한](#0-github-접근-권한)** 부터 순서대로 실행해주세요. 막히면 맨 아래 [트러블슈팅](#트러블슈팅)에 대부분 답이 있습니다.

## 기술 스택

| 항목 | 버전 / 값 |
|---|---|
| Spring Boot | 3.5.16 |
| Java | 17 |
| 빌드 도구 | Gradle (Groovy) |
| DB | MySQL |
| ORM | Spring Data JPA (Hibernate) |
| 기본 포트 | 8080 |

> ⚠️ **Spring Boot 4.x가 아닙니다.** 호환성을 위해 3.5.x를 사용합니다. `build.gradle`의 버전을 임의로 올리지 마세요. (4.x에서는 `spring-boot-starter-web`이 `webmvc`로 바뀌는 등 변경이 많아 코드가 깨집니다.)

> 💡 **Mac / Windows 명령어가 다릅니다.** 아래에서 본인 OS에 맞는 쪽만 참고하세요.
 
---

## 0. GitHub 접근 권한

이 레포는 **private** 이라 초대받지 않으면 클론이 안 됩니다.

1. 팀장에게 본인 **GitHub 아이디**를 알려주고 collaborator 초대를 요청하세요.
2. GitHub 알림 또는 가입 이메일로 온 초대를 **수락**하세요.
### 인증 토큰 발급

GitHub는 계정 비밀번호로는 클론/푸시를 허용하지 않습니다. **토큰**을 발급받아야 합니다.

1. github.com → 우측 상단 프로필 → **Settings**
2. 왼쪽 메뉴 맨 아래 **Developer settings**
3. **Personal access tokens** → **Tokens (classic)**
4. **Generate new token** → **Generate new token (classic)**
5. 설정값
   - Note: `hackathon`
   - Expiration: `90 days`
   - Select scopes: **`repo`** 체크 (이것만)
6. 맨 아래 **Generate token** → 나온 문자열 **복사**
> ⚠️ 토큰은 이 화면을 벗어나면 다시 볼 수 없습니다. 바로 복사해서 안전한 곳에 보관하세요.
> 토큰은 비밀번호와 같습니다. **레포에 커밋하거나 팀 채널에 올리지 마세요.**

클론과 push를 할 때마다 Username / Password를 물어봅니다. Username에는 **GitHub 아이디**, Password에는 **토큰**을 붙여넣으세요. 터미널에 붙여넣어도 화면에 아무것도 안 보이는 것이 정상입니다. 그냥 Enter를 누르면 됩니다.
 
---

## 사전 준비

### 1. JDK 17

```bash
java -version
```

`17.x`가 나오면 통과입니다. 아니면 설치하세요.

**Mac**

```bash
brew install --cask temurin@17
```

**Windows**

adoptium.net 에서 Temurin **17 (LTS)** 설치 파일을 받아 실행합니다. 설치 중 "Set JAVA_HOME variable" 옵션을 반드시 체크하세요.

### 2. MySQL

```bash
mysql --version
```

**Mac**

```bash
brew install mysql
brew services start mysql
```

**Windows**

dev.mysql.com/downloads/installer 에서 MySQL Installer를 받아 실행합니다.

- Setup Type은 **Developer Default** 선택
- 설치 중 **root 비밀번호**를 정하게 됩니다. 반드시 기억하세요. 나중에 설정 파일에 입력합니다.
### 3. IntelliJ IDEA

jetbrains.com/idea 에서 **Community Edition**(무료)을 받으면 충분합니다.

학교 이메일이 있으면 jetbrains.com/community/education 에서 Ultimate를 무료로 쓸 수 있습니다.
 
---

## 초기 세팅 (클론 후 1회)

### 1. 클론

```bash
cd ~
git clone https://github.com/14th-likelion-Hackathon-4team/BE.git
cd BE
```

Username / Password를 물으면 위에서 발급받은 **아이디 + 토큰**을 입력합니다.

### 2. DB 생성

MySQL에 접속합니다.

```bash
mysql -u root -p
```

비밀번호를 입력하면 `mysql>` 프롬프트가 뜹니다. 여기에 아래를 입력하세요.

```sql
CREATE DATABASE team4 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SHOW DATABASES;
exit;
```

목록에 `team4`가 보이면 성공입니다.

> `utf8mb4`는 필수입니다. 빼면 한글과 이모지가 깨집니다.

### 3. 설정 파일 만들기

DB 비밀번호는 사람마다 다르므로 git에 올리지 않습니다. 견본을 복사해서 본인 것을 만드세요.

```bash
# Mac
cp src/main/resources/application-local.yaml.example src/main/resources/application-local.yaml
 
# Windows (PowerShell)
copy src\main\resources\application-local.yaml.example src\main\resources\application-local.yaml
```

복사한 `application-local.yaml`을 열어 `password`에 **본인 MySQL 비밀번호**를 입력합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/team4?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: root
    password: "본인비밀번호"
    driver-class-name: com.mysql.cj.jdbc.Driver
```

> 비밀번호는 반드시 **큰따옴표**로 감싸세요. 숫자만 있을 경우 따옴표가 없으면 인식이 안 될 수 있습니다.
>
> `application-local.yaml`은 `.gitignore`에 등록되어 있습니다. **절대 커밋하지 마세요.**
> `application-local.yaml.example`은 견본이므로 실제 비밀번호를 넣지 마세요.

### 4. IntelliJ로 열기

IntelliJ 실행 → **Open** → `BE` 폴더 선택 (`build.gradle`이 아니라 **폴더**를 선택).

열면 우측 하단에 Gradle 동기화 진행 표시가 뜹니다. **1~3분 걸립니다.** 이게 끝나기 전에는 코드에 빨간 줄이 잔뜩 보여도 정상이니 기다리세요.

**Lombok 설정 (필수)**

Settings(`Cmd + ,` / `Ctrl + Alt + S`) → Build, Execution, Deployment → Compiler → **Annotation Processors** → `Enable annotation processing` 체크.

이걸 안 하면 `@Getter`, `@RequiredArgsConstructor` 같은 게 동작하지 않아 컴파일 에러가 납니다.

### 5. 실행

```bash
# Mac
./gradlew bootRun
 
# Windows
gradlew.bat bootRun
```

IntelliJ 우측 상단의 초록색 ▶ 버튼을 눌러도 됩니다.

아래 두 줄이 보이면 성공입니다.

```
HikariPool-1 - Start completed.          ← DB 연결 성공
Tomcat started on port 8080 (http)       ← 서버 기동 성공
```

브라우저에서 `http://localhost:8080` 접속 시 **Whitelabel Error Page**가 나오는 것은 정상입니다. 아직 API가 없어서 404가 뜨는 것이며, 서버가 응답하고 있다는 뜻입니다.

종료는 `Ctrl + C`.
 
---

## 프로젝트 구조

```
src/main/java/com/likelion/team4/
├── Team4Application.java
├── domain/                  ← 기능별 패키지
│   └── user/                  (예시)
│       ├── controller/          API 엔드포인트
│       ├── service/             비즈니스 로직
│       ├── repository/          DB 접근
│       ├── entity/              테이블 매핑
│       └── dto/                 요청/응답 객체
└── global/                  ← 공통 설정
    ├── config/                  CORS 등 설정 클래스
    └── exception/               공통 예외 처리
```

**기능 단위로 `domain/` 아래에 패키지를 만드세요.** `controller/` 안에 모든 컨트롤러를 모으는 방식이 아닙니다. 담당 기능이 서로 다른 폴더에 있으면 충돌이 거의 나지 않습니다.
 
---

## 개발 규칙

팀끼리 맞춰야 하는 것만 적었습니다.

- **개발 순서**: Entity → Repository → DTO → Service → Controller
- **API 주소는 `/api/` 로 시작**하도록 통일합니다. (예: `/api/users`, `/api/posts`)
- **Entity를 직접 응답으로 반환하지 않고 DTO를 사용**합니다.
- `ddl-auto: update`로 설정되어 있어 Entity를 만들고 서버를 재시작하면 테이블이 자동 생성됩니다. 컬럼 삭제나 타입 변경은 자동 반영되지 않으니 그때는 직접 DB를 수정하세요.
> ⚠️ **검색으로 찾은 자료가 Spring Boot 3 기준인지 확인하세요.** `javax.persistence`로 되어 있으면 구버전 자료입니다. Spring Boot 3부터는 **`jakarta.persistence`** 를 사용하며, `javax`는 컴파일되지 않습니다.

### 공통 응답 형식 — 백엔드 팀원들이 상의해서 결정해주세요

**초기 세팅에는 포함되어 있지 않습니다.** 개발 시작 전에 백엔드 팀원끼리 상의해서 형식을 통일하고, 결정한 내용을 팀 채널에 공유해주세요.

사람마다 응답 구조가 다르면 프론트에서 API마다 다르게 처리해야 해서 연동 단계에서 시간을 크게 잃습니다. 통일 여부보다 **합의된 기준이 있다는 것 자체가 중요합니다.**

합의할 항목:

- 성공 응답을 그대로 반환할지, `{ "data": ..., "message": ... }` 처럼 감쌀지
- 에러 응답의 형식 (필드 이름, 에러 코드 사용 여부)
- HTTP 상태 코드 사용 규칙 (생성 시 201 vs 200 등)
  정한 형식은 `global/` 아래에 공통 클래스로 만들어두고 전원이 그것을 사용하면 어긋날 일이 없습니다. `global/exception/`도 비어 있으니 예외 처리 방식(`@RestControllerAdvice` 등)을 함께 정하면 좋습니다.

---

## 협업 규칙

### 브랜치

`main`에 직접 커밋하지 않습니다. 기능별 브랜치를 만들고 PR로 머지합니다.

```bash
git checkout main
git pull
git checkout -b feat/user-api
 
# 작업 후
git add .
git commit -m "feat: 회원가입 API 추가"
git push -u origin feat/user-api
```

push하면 터미널에 PR 링크가 출력됩니다. 그 링크를 열어 **Create pull request** 를 누르고, 리뷰 후 머지하세요.

머지가 끝나면 다음 작업 전에 반드시 최신 코드를 받아옵니다.

```bash
git checkout main
git pull
```

브랜치 이름 규칙: `feat/기능명`, `fix/버그명`, `refactor/대상`

### 커밋 메시지

| 접두어 | 용도 |
|---|---|
| `feat:` | 새 기능 |
| `fix:` | 버그 수정 |
| `refactor:` | 기능 변화 없는 코드 정리 |
| `chore:` | 설정, 빌드, 의존성 |
| `docs:` | 문서 |

### 충돌 방지

- 작업 시작 전 항상 `git pull`
- 담당 기능이 정해지면 그 패키지 밖의 파일은 건드리지 않기
- `build.gradle`을 수정할 일이 있으면 팀 채널에 먼저 공유 (전원에게 영향)
- `application.yaml`(공통 설정)을 수정할 때도 마찬가지
- `application-local.yaml`은 각자 다르므로 git에 올라가지 않습니다. 커밋 목록에 보이면 잘못된 것이니 팀에 알리세요.
---

## 트러블슈팅

### 클론 / 푸시

| 증상 | 원인과 해결 |
|---|---|
| `Support for password authentication was removed` | 계정 비밀번호를 입력함 → 토큰을 입력해야 함 (위 0번 참고) |
| `Repository not found` | 인증은 됐지만 접근 권한 없음 → 팀장에게 collaborator 초대 요청 |
| `Please tell me who you are` | git 사용자 정보 미설정 → `git config --global user.name "아이디"`, `git config --global user.email "이메일"` |
| push할 때마다 아이디/토큰을 물어봄 | 정상 동작입니다. 매번 입력하는 게 번거로우면 credential helper 설정을 검색해보세요 |

### 실행

| 증상 | 원인과 해결 |
|---|---|
| `Communications link failure` | MySQL이 꺼져 있음 → Mac: `brew services start mysql` / Windows: services.msc에서 MySQL 시작 |
| `Access denied for user 'root'` | 비밀번호 불일치 → `application-local.yaml` 확인 |
| `Unknown database 'team4'` | DB 미생성 → 초기 세팅 2번 다시 |
| `Failed to configure a DataSource` | `application-local.yaml`을 안 만들었음 → 초기 세팅 3번 |
| `Public Key Retrieval is not allowed` | url 끝에 `&allowPublicKeyRetrieval=true` 추가 |
| `Port 8080 was already in use` | 이전 서버가 살아 있음 → Mac: `lsof -i :8080` 후 `kill -9 PID` / Windows: `netstat -ano` 로 8080 확인 후 `taskkill /PID 번호 /F` |
| `permission denied: ./gradlew` | 실행 권한 없음 → `chmod +x gradlew` |

### 코드

| 증상 | 원인과 해결 |
|---|---|
| `@Getter`가 인식 안 됨 | IntelliJ → Settings → Build → Compiler → Annotation Processors → Enable 체크 |
| `cannot find symbol: javax.persistence` | Spring Boot 3은 `jakarta.persistence` 사용 → import 수정 |
| API 호출 시 404 | `@RestController` 누락, 또는 URL 오타 확인 |
| 테이블이 생성되지 않음 | `@Entity` 누락, 또는 서버 재시작 안 함 |
| 프론트에서 CORS 에러 | 아래 CORS 항목 참고 |
 
---

## CORS

프론트엔드가 별도 레포/포트에서 동작하므로 `global/config/WebConfig.java`에 CORS가 설정되어 있습니다.

허용된 origin은 `http://localhost:3000`, `http://localhost:5173`입니다. 프론트 개발 서버 포트가 다르거나 배포 주소가 정해지면 이 파일에 추가하세요.

> `allowCredentials(true)`와 `allowedOrigins("*")`는 함께 쓸 수 없습니다. 서버가 기동되지 않습니다.
 
---

## 배포 전 확인 (나중에)

- `ddl-auto`를 `update`에서 `validate`로 변경 (운영 DB 데이터 보호)
- 운영용 설정은 `application-prod.yaml`로 분리
- 배포된 프론트 주소를 CORS 허용 목록에 추가
 