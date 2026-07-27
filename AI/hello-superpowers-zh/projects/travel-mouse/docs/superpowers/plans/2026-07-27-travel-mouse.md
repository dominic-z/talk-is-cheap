# Travel Mouse 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 构建一个基于高德地图的本地旅行计划工具，支持收藏地址、创建多日旅行计划、交互式路线规划。

**架构：** Vue 3 前端通过高德 JS API 2.0 处理所有地图交互（POI 搜索、路线计算），Spring Boot 后端提供纯数据 CRUD REST API，MySQL 持久化。前后端单体仓库，开发时 Vite 代理到后端。

**技术栈：** Vue 3 + Vite + 高德 JS API 2.0 / Spring Boot 3 + Maven + Spring Data JPA / MySQL 8

---

## 文件结构

### 后端 (`backend/`)

| 文件 | 职责 |
|------|------|
| `pom.xml` | Maven 依赖配置 |
| `src/main/java/com/travelmouse/TravelMouseApplication.java` | 启动类 |
| `src/main/java/com/travelmouse/entity/Favorite.java` | 收藏实体 |
| `src/main/java/com/travelmouse/entity/TravelPlan.java` | 旅行计划实体 |
| `src/main/java/com/travelmouse/entity/DailyPlan.java` | 单天计划实体 |
| `src/main/java/com/travelmouse/entity/Destination.java` | 目的地实体 |
| `src/main/java/com/travelmouse/entity/DestinationImage.java` | 目的地图片实体 |
| `src/main/java/com/travelmouse/entity/RouteSegment.java` | 路线段实体 |
| `src/main/java/com/travelmouse/repository/FavoriteRepository.java` | 收藏 DAO |
| `src/main/java/com/travelmouse/repository/TravelPlanRepository.java` | 旅行计划 DAO |
| `src/main/java/com/travelmouse/repository/DailyPlanRepository.java` | 单天计划 DAO |
| `src/main/java/com/travelmouse/repository/DestinationRepository.java` | 目的地 DAO |
| `src/main/java/com/travelmouse/repository/DestinationImageRepository.java` | 图片 DAO |
| `src/main/java/com/travelmouse/repository/RouteSegmentRepository.java` | 路线段 DAO |
| `src/main/java/com/travelmouse/dto/ApiResponse.java` | 统一响应包装 |
| `src/main/java/com/travelmouse/dto/TravelPlanCreateRequest.java` | 创建计划请求 |
| `src/main/java/com/travelmouse/dto/DestinationRequest.java` | 目的地请求 |
| `src/main/java/com/travelmouse/dto/RouteUpdateRequest.java` | 路线更新请求 |
| `src/main/java/com/travelmouse/service/FavoriteService.java` | 收藏业务逻辑 |
| `src/main/java/com/travelmouse/service/TravelPlanService.java` | 旅行计划业务逻辑 |
| `src/main/java/com/travelmouse/service/DailyPlanService.java` | 单天计划业务逻辑 |
| `src/main/java/com/travelmouse/service/DestinationService.java` | 目的地业务逻辑 |
| `src/main/java/com/travelmouse/service/RouteService.java` | 路线业务逻辑 |
| `src/main/java/com/travelmouse/controller/FavoriteController.java` | 收藏 API |
| `src/main/java/com/travelmouse/controller/TravelPlanController.java` | 旅行计划 API |
| `src/main/java/com/travelmouse/controller/DailyPlanController.java` | 单天计划 API |
| `src/main/java/com/travelmouse/controller/DestinationController.java` | 目的地 API |
| `src/main/java/com/travelmouse/controller/RouteController.java` | 路线 API |
| `src/main/java/com/travelmouse/config/WebConfig.java` | CORS + 静态资源配置 |
| `src/main/java/com/travelmouse/config/GlobalExceptionHandler.java` | 全局异常处理 |
| `src/main/resources/application.yml` | 应用配置 |
| `src/main/resources/db/schema.sql` | 建表语句 |
| `src/test/java/com/travelmouse/service/FavoriteServiceTest.java` | 收藏服务测试 |
| `src/test/java/com/travelmouse/service/TravelPlanServiceTest.java` | 计划服务测试 |
| `src/test/java/com/travelmouse/controller/FavoriteControllerTest.java` | 收藏 API 测试 |
| `src/test/java/com/travelmouse/controller/TravelPlanControllerTest.java` | 计划 API 测试 |

### 前端 (`frontend/`)

| 文件 | 职责 |
|------|------|
| `package.json` | 依赖配置 |
| `vite.config.js` | Vite 配置（代理） |
| `.env` | 高德 API Key |
| `index.html` | 入口 HTML |
| `src/main.js` | Vue 应用入口 |
| `src/App.vue` | 根组件 |
| `src/router/index.js` | 路由配置 |
| `src/api/request.js` | axios 实例 + 拦截器 |
| `src/api/favorites.js` | 收藏 API |
| `src/api/plans.js` | 计划 API |
| `src/api/destinations.js` | 目的地 API |
| `src/composables/useAmap.js` | 地图初始化 |
| `src/composables/usePoiSearch.js` | POI 搜索 |
| `src/composables/useRoutePlanning.js` | 路线规划计算 |
| `src/views/HomeView.vue` | 首页（计划列表） |
| `src/views/FavoritesView.vue` | 收藏管理 |
| `src/views/PlanCreateView.vue` | 创建计划 |
| `src/views/PlanDetailView.vue` | 计划详情 |
| `src/views/DayPlanView.vue` | 核心：单天计划制定 |
| `src/components/LeftPanel.vue` | 左侧面板 |
| `src/components/PendingList.vue` | 待定列表 |
| `src/components/DestinationCard.vue` | 目的地卡片 |
| `src/components/AddDestination.vue` | 搜索添加 |
| `src/components/NoteEditor.vue` | 备注编辑 |
| `src/components/MapContainer.vue` | 地图容器 |
| `src/components/RightPanel.vue` | 右侧面板 |
| `src/components/RoutePlan.vue` | 路线列表 |
| `src/components/RouteSegment.vue` | 路线段 |
| `src/components/RouteActions.vue` | 路线操作 |

---

## 任务 1：后端项目初始化

**文件：**
- 创建：`backend/pom.xml`
- 创建：`backend/src/main/java/com/travelmouse/TravelMouseApplication.java`
- 创建：`backend/src/main/resources/application.yml`

- [ ] **步骤 1：创建 Maven 项目结构**

```bash
mkdir -p backend/src/main/java/com/travelmouse
mkdir -p backend/src/main/resources/db
mkdir -p backend/src/test/java/com/travelmouse
mkdir -p backend/uploads
```

- [ ] **步骤 2：编写 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>
    <groupId>com.travelmouse</groupId>
    <artifactId>travel-mouse-backend</artifactId>
    <version>1.0.0</version>
    <name>travel-mouse-backend</name>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **步骤 3：编写启动类**

```java
package com.travelmouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TravelMouseApplication {
    public static void main(String[] args) {
        SpringApplication.run(TravelMouseApplication.class, args);
    }
}
```

- [ ] **步骤 4：编写 application.yml**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/travel_mouse?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 10MB

app:
  upload-dir: ./uploads
```

- [ ] **步骤 5：验证项目可编译**

运行：`cd backend && mvn compile -q`
预期：BUILD SUCCESS

- [ ] **步骤 6：Commit**

```bash
git add backend/
git commit -m "feat: 初始化 Spring Boot 后端项目"
```

---

## 任务 2：数据库建表

**文件：**
- 创建：`backend/src/main/resources/db/schema.sql`

- [ ] **步骤 1：编写建表语句**

```sql
CREATE DATABASE IF NOT EXISTS travel_mouse DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE travel_mouse;

CREATE TABLE favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    category VARCHAR(50),
    note TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE travel_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE daily_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    travel_plan_id BIGINT NOT NULL,
    plan_date DATE NOT NULL,
    sort_order INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    FOREIGN KEY (travel_plan_id) REFERENCES travel_plans(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE destinations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    daily_plan_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    category VARCHAR(50),
    note_text TEXT,
    arrive_time TIME,
    duration_minutes INT,
    leave_time TIME,
    sort_order INT NOT NULL DEFAULT 0,
    in_route BOOLEAN NOT NULL DEFAULT FALSE,
    route_order INT,
    FOREIGN KEY (daily_plan_id) REFERENCES daily_plans(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE destination_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    destination_id BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE route_segments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    daily_plan_id BIGINT NOT NULL,
    from_dest_id BIGINT NOT NULL,
    to_dest_id BIGINT NOT NULL,
    transport_mode VARCHAR(20) NOT NULL,
    duration_minutes INT NOT NULL,
    distance_meters INT NOT NULL,
    route_order INT NOT NULL,
    FOREIGN KEY (daily_plan_id) REFERENCES daily_plans(id) ON DELETE CASCADE,
    FOREIGN KEY (from_dest_id) REFERENCES destinations(id) ON DELETE CASCADE,
    FOREIGN KEY (to_dest_id) REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **步骤 2：告知用户执行建表**

提示用户：
> 请在 MySQL 中执行 `backend/src/main/resources/db/schema.sql`。
> 命令：`mysql -u root -p < backend/src/main/resources/db/schema.sql`
> 如果你的 MySQL 用户名/密码不是 root/root，请告知我修改 application.yml。

- [ ] **步骤 3：Commit**

```bash
git add backend/src/main/resources/db/schema.sql
git commit -m "feat: 添加数据库建表语句"
```

---

## 任务 3：后端实体层

**文件：**
- 创建：`backend/src/main/java/com/travelmouse/entity/Favorite.java`
- 创建：`backend/src/main/java/com/travelmouse/entity/TravelPlan.java`
- 创建：`backend/src/main/java/com/travelmouse/entity/DailyPlan.java`
- 创建：`backend/src/main/java/com/travelmouse/entity/Destination.java`
- 创建：`backend/src/main/java/com/travelmouse/entity/DestinationImage.java`
- 创建：`backend/src/main/java/com/travelmouse/entity/RouteSegment.java`

- [ ] **步骤 1：创建 Favorite 实体**

```java
package com.travelmouse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorites")
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(length = 50)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

- [ ] **步骤 2：创建 TravelPlan 实体**

```java
package com.travelmouse.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "travel_plans")
public class TravelPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "travelPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<DailyPlan> dailyPlans = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<DailyPlan> getDailyPlans() { return dailyPlans; }
    public void setDailyPlans(List<DailyPlan> dailyPlans) { this.dailyPlans = dailyPlans; }
}
```

- [ ] **步骤 3：创建 DailyPlan 实体**

```java
package com.travelmouse.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "daily_plans")
public class DailyPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id", nullable = false)
    private TravelPlan travelPlan;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(nullable = false, length = 20)
    private String status = "draft";

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TravelPlan getTravelPlan() { return travelPlan; }
    public void setTravelPlan(TravelPlan travelPlan) { this.travelPlan = travelPlan; }
    public LocalDate getPlanDate() { return planDate; }
    public void setPlanDate(LocalDate planDate) { this.planDate = planDate; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

- [ ] **步骤 4：创建 Destination 实体**

```java
package com.travelmouse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "destinations")
public class Destination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_plan_id", nullable = false)
    private DailyPlan dailyPlan;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(length = 50)
    private String category;

    @Column(name = "note_text", columnDefinition = "TEXT")
    private String noteText;

    @Column(name = "arrive_time")
    private LocalTime arriveTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "leave_time")
    private LocalTime leaveTime;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "in_route", nullable = false)
    private Boolean inRoute = false;

    @Column(name = "route_order")
    private Integer routeOrder;

    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DestinationImage> images = new ArrayList<>();

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DailyPlan getDailyPlan() { return dailyPlan; }
    public void setDailyPlan(DailyPlan dailyPlan) { this.dailyPlan = dailyPlan; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getNoteText() { return noteText; }
    public void setNoteText(String noteText) { this.noteText = noteText; }
    public LocalTime getArriveTime() { return arriveTime; }
    public void setArriveTime(LocalTime arriveTime) { this.arriveTime = arriveTime; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public LocalTime getLeaveTime() { return leaveTime; }
    public void setLeaveTime(LocalTime leaveTime) { this.leaveTime = leaveTime; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Boolean getInRoute() { return inRoute; }
    public void setInRoute(Boolean inRoute) { this.inRoute = inRoute; }
    public Integer getRouteOrder() { return routeOrder; }
    public void setRouteOrder(Integer routeOrder) { this.routeOrder = routeOrder; }
    public List<DestinationImage> getImages() { return images; }
}
```

- [ ] **步骤 5：创建 DestinationImage 实体**

```java
package com.travelmouse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "destination_images")
public class DestinationImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Destination getDestination() { return destination; }
    public void setDestination(Destination destination) { this.destination = destination; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

- [ ] **步骤 6：创建 RouteSegment 实体**

```java
package com.travelmouse.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "route_segments")
public class RouteSegment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_plan_id", nullable = false)
    private DailyPlan dailyPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_dest_id", nullable = false)
    private Destination fromDestination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_dest_id", nullable = false)
    private Destination toDestination;

    @Column(name = "transport_mode", nullable = false, length = 20)
    private String transportMode;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "distance_meters", nullable = false)
    private Integer distanceMeters;

    @Column(name = "route_order", nullable = false)
    private Integer routeOrder;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DailyPlan getDailyPlan() { return dailyPlan; }
    public void setDailyPlan(DailyPlan dailyPlan) { this.dailyPlan = dailyPlan; }
    public Destination getFromDestination() { return fromDestination; }
    public void setFromDestination(Destination fromDestination) { this.fromDestination = fromDestination; }
    public Destination getToDestination() { return toDestination; }
    public void setToDestination(Destination toDestination) { this.toDestination = toDestination; }
    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String transportMode) { this.transportMode = transportMode; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(Integer distanceMeters) { this.distanceMeters = distanceMeters; }
    public Integer getRouteOrder() { return routeOrder; }
    public void setRouteOrder(Integer routeOrder) { this.routeOrder = routeOrder; }
}
```

- [ ] **步骤 7：验证编译**

运行：`cd backend && mvn compile -q`
预期：BUILD SUCCESS

- [ ] **步骤 8：Commit**

```bash
git add backend/src/main/java/com/travelmouse/entity/
git commit -m "feat: 添加 JPA 实体层"
```

---

## 任务 4：Repository 层 + DTO + 统一响应

**文件：**
- 创建：`backend/src/main/java/com/travelmouse/repository/*.java`（6 个）
- 创建：`backend/src/main/java/com/travelmouse/dto/ApiResponse.java`
- 创建：`backend/src/main/java/com/travelmouse/dto/TravelPlanCreateRequest.java`
- 创建：`backend/src/main/java/com/travelmouse/dto/DestinationRequest.java`
- 创建：`backend/src/main/java/com/travelmouse/dto/RouteUpdateRequest.java`

- [ ] **步骤 1：创建所有 Repository 接口**

```java
// FavoriteRepository.java
package com.travelmouse.repository;

import com.travelmouse.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
}
```

```java
// TravelPlanRepository.java
package com.travelmouse.repository;

import com.travelmouse.entity.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
}
```

```java
// DailyPlanRepository.java
package com.travelmouse.repository;

import com.travelmouse.entity.DailyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DailyPlanRepository extends JpaRepository<DailyPlan, Long> {
    List<DailyPlan> findByTravelPlanIdOrderBySortOrder(Long travelPlanId);
}
```

```java
// DestinationRepository.java
package com.travelmouse.repository;

import com.travelmouse.entity.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DestinationRepository extends JpaRepository<Destination, Long> {
    List<Destination> findByDailyPlanIdOrderBySortOrder(Long dailyPlanId);
    List<Destination> findByDailyPlanIdAndInRouteTrueOrderByRouteOrder(Long dailyPlanId);
}
```

```java
// DestinationImageRepository.java
package com.travelmouse.repository;

import com.travelmouse.entity.DestinationImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DestinationImageRepository extends JpaRepository<DestinationImage, Long> {
    List<DestinationImage> findByDestinationId(Long destinationId);
}
```

```java
// RouteSegmentRepository.java
package com.travelmouse.repository;

import com.travelmouse.entity.RouteSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RouteSegmentRepository extends JpaRepository<RouteSegment, Long> {
    List<RouteSegment> findByDailyPlanIdOrderByRouteOrder(Long dailyPlanId);
    void deleteByDailyPlanId(Long dailyPlanId);
}
```

- [ ] **步骤 2：创建统一响应 DTO**

```java
package com.travelmouse.dto;

public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.code = 200;
        resp.message = "success";
        resp.data = data;
        return resp;
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.code = code;
        resp.message = message;
        resp.data = null;
        return resp;
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
```

- [ ] **步骤 3：创建请求 DTO**

```java
// TravelPlanCreateRequest.java
package com.travelmouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class TravelPlanCreateRequest {
    @NotBlank(message = "计划名称不能为空")
    private String name;

    @NotNull(message = "起始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
```

```java
// DestinationRequest.java
package com.travelmouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalTime;

public class DestinationRequest {
    @NotBlank private String name;
    @NotBlank private String address;
    @NotNull private BigDecimal longitude;
    @NotNull private BigDecimal latitude;
    private String category;
    private String noteText;
    private LocalTime arriveTime;
    private Integer durationMinutes;
    private LocalTime leaveTime;

    // getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getNoteText() { return noteText; }
    public void setNoteText(String noteText) { this.noteText = noteText; }
    public LocalTime getArriveTime() { return arriveTime; }
    public void setArriveTime(LocalTime arriveTime) { this.arriveTime = arriveTime; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public LocalTime getLeaveTime() { return leaveTime; }
    public void setLeaveTime(LocalTime leaveTime) { this.leaveTime = leaveTime; }
}
```

```java
// RouteUpdateRequest.java
package com.travelmouse.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class RouteUpdateRequest {
    @NotNull
    private List<SegmentItem> segments;

    public static class SegmentItem {
        @NotNull private Long fromDestId;
        @NotNull private Long toDestId;
        @NotNull private String transportMode;
        @NotNull private Integer durationMinutes;
        @NotNull private Integer distanceMeters;
        @NotNull private Integer routeOrder;

        public Long getFromDestId() { return fromDestId; }
        public void setFromDestId(Long fromDestId) { this.fromDestId = fromDestId; }
        public Long getToDestId() { return toDestId; }
        public void setToDestId(Long toDestId) { this.toDestId = toDestId; }
        public String getTransportMode() { return transportMode; }
        public void setTransportMode(String transportMode) { this.transportMode = transportMode; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
        public Integer getDistanceMeters() { return distanceMeters; }
        public void setDistanceMeters(Integer distanceMeters) { this.distanceMeters = distanceMeters; }
        public Integer getRouteOrder() { return routeOrder; }
        public void setRouteOrder(Integer routeOrder) { this.routeOrder = routeOrder; }
    }

    public List<SegmentItem> getSegments() { return segments; }
    public void setSegments(List<SegmentItem> segments) { this.segments = segments; }
}
```

- [ ] **步骤 4：验证编译**

运行：`cd backend && mvn compile -q`
预期：BUILD SUCCESS

- [ ] **步骤 5：Commit**

```bash
git add backend/src/main/java/com/travelmouse/repository/ backend/src/main/java/com/travelmouse/dto/
git commit -m "feat: 添加 Repository 层和 DTO"
```

---

## 任务 5：全局配置（CORS + 异常处理 + 静态资源）

**文件：**
- 创建：`backend/src/main/java/com/travelmouse/config/WebConfig.java`
- 创建：`backend/src/main/java/com/travelmouse/config/GlobalExceptionHandler.java`

- [ ] **步骤 1：创建 WebConfig**

```java
package com.travelmouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
```

- [ ] **步骤 2：创建 GlobalExceptionHandler**

```java
package com.travelmouse.config;

import com.travelmouse.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(NoSuchElementException e) {
        return ApiResponse.error(404, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBadRequest(IllegalArgumentException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return ApiResponse.error(400, msg);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMaxUpload(MaxUploadSizeExceededException e) {
        return ApiResponse.error(400, "文件大小超过限制（最大 5MB）");
    }
}
```

- [ ] **步骤 3：验证编译**

运行：`cd backend && mvn compile -q`
预期：BUILD SUCCESS

- [ ] **步骤 4：Commit**

```bash
git add backend/src/main/java/com/travelmouse/config/
git commit -m "feat: 添加 CORS、异常处理、静态资源配置"
```

---

## 任务 6：Service 层（收藏 + 旅行计划）

**文件：**
- 创建：`backend/src/main/java/com/travelmouse/service/FavoriteService.java`
- 创建：`backend/src/main/java/com/travelmouse/service/TravelPlanService.java`
- 测试：`backend/src/test/java/com/travelmouse/service/FavoriteServiceTest.java`
- 测试：`backend/src/test/java/com/travelmouse/service/TravelPlanServiceTest.java`

- [ ] **步骤 1：编写 FavoriteService 失败测试**

```java
package com.travelmouse.service;

import com.travelmouse.entity.Favorite;
import com.travelmouse.repository.FavoriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FavoriteServiceTest {
    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createFavorite_shouldSaveAndReturn() {
        Favorite fav = new Favorite();
        fav.setName("西湖");
        fav.setAddress("杭州市西湖区");
        fav.setLongitude(new BigDecimal("120.1485"));
        fav.setLatitude(new BigDecimal("30.2741"));
        when(favoriteRepository.save(any())).thenAnswer(inv -> {
            Favorite f = inv.getArgument(0);
            f.setId(1L);
            return f;
        });

        Favorite result = favoriteService.create(fav);
        assertEquals(1L, result.getId());
        assertEquals("西湖", result.getName());
    }

    @Test
    void deleteFavorite_notFound_shouldThrow() {
        when(favoriteRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> favoriteService.delete(99L));
    }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：`cd backend && mvn test -pl . -Dtest=FavoriteServiceTest -q`
预期：FAIL，FavoriteService 不存在

- [ ] **步骤 3：实现 FavoriteService**

```java
package com.travelmouse.service;

import com.travelmouse.entity.Favorite;
import com.travelmouse.repository.FavoriteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    public List<Favorite> findAll() {
        return favoriteRepository.findAll();
    }

    public Favorite create(Favorite favorite) {
        return favoriteRepository.save(favorite);
    }

    public Favorite update(Long id, Favorite updated) {
        Favorite fav = favoriteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("收藏不存在: " + id));
        fav.setName(updated.getName());
        fav.setAddress(updated.getAddress());
        fav.setLongitude(updated.getLongitude());
        fav.setLatitude(updated.getLatitude());
        fav.setCategory(updated.getCategory());
        fav.setNote(updated.getNote());
        return favoriteRepository.save(fav);
    }

    public void delete(Long id) {
        Favorite fav = favoriteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("收藏不存在: " + id));
        favoriteRepository.delete(fav);
    }
}
```

- [ ] **步骤 4：运行测试验证通过**

运行：`cd backend && mvn test -Dtest=FavoriteServiceTest -q`
预期：PASS

- [ ] **步骤 5：编写 TravelPlanService 失败测试**

```java
package com.travelmouse.service;

import com.travelmouse.entity.TravelPlan;
import com.travelmouse.repository.TravelPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TravelPlanServiceTest {
    @Mock
    private TravelPlanRepository travelPlanRepository;

    @InjectMocks
    private TravelPlanService travelPlanService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_invalidDateRange_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () ->
            travelPlanService.create("测试", LocalDate.of(2026, 8, 5), LocalDate.of(2026, 8, 1))
        );
    }

    @Test
    void create_validRange_shouldCreateDailyPlans() {
        when(travelPlanRepository.save(any())).thenAnswer(inv -> {
            TravelPlan p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        TravelPlan plan = travelPlanService.create("杭州三日游",
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3));

        assertEquals(3, plan.getDailyPlans().size());
        assertEquals(LocalDate.of(2026, 8, 1), plan.getDailyPlans().get(0).getPlanDate());
        assertEquals(1, plan.getDailyPlans().get(0).getSortOrder());
    }
}
```

- [ ] **步骤 6：运行测试验证失败**

运行：`cd backend && mvn test -Dtest=TravelPlanServiceTest -q`
预期：FAIL，TravelPlanService 不存在

- [ ] **步骤 7：实现 TravelPlanService**

```java
package com.travelmouse.service;

import com.travelmouse.entity.DailyPlan;
import com.travelmouse.entity.TravelPlan;
import com.travelmouse.repository.TravelPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TravelPlanService {
    private final TravelPlanRepository travelPlanRepository;

    public TravelPlanService(TravelPlanRepository travelPlanRepository) {
        this.travelPlanRepository = travelPlanRepository;
    }

    public List<TravelPlan> findAll() {
        return travelPlanRepository.findAll();
    }

    public TravelPlan findById(Long id) {
        return travelPlanRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("旅行计划不存在: " + id));
    }

    @Transactional
    public TravelPlan create(String name, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("结束日期不能早于起始日期");
        }

        TravelPlan plan = new TravelPlan();
        plan.setName(name);
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);

        int order = 1;
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            DailyPlan daily = new DailyPlan();
            daily.setTravelPlan(plan);
            daily.setPlanDate(d);
            daily.setSortOrder(order++);
            daily.setStatus("draft");
            plan.getDailyPlans().add(daily);
        }

        return travelPlanRepository.save(plan);
    }

    @Transactional
    public TravelPlan update(Long id, String name, LocalDate startDate, LocalDate endDate) {
        TravelPlan plan = findById(id);
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("结束日期不能早于起始日期");
        }
        plan.setName(name);
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);
        return travelPlanRepository.save(plan);
    }

    @Transactional
    public void delete(Long id) {
        TravelPlan plan = findById(id);
        travelPlanRepository.delete(plan);
    }
}
```

- [ ] **步骤 8：运行测试验证通过**

运行：`cd backend && mvn test -Dtest=TravelPlanServiceTest -q`
预期：PASS

- [ ] **步骤 9：Commit**

```bash
git add backend/src/main/java/com/travelmouse/service/ backend/src/test/
git commit -m "feat: 添加收藏和旅行计划 Service 层（含测试）"
```

---

## 任务 7：Service 层（单天计划 + 目的地 + 路线）

**文件：**
- 创建：`backend/src/main/java/com/travelmouse/service/DailyPlanService.java`
- 创建：`backend/src/main/java/com/travelmouse/service/DestinationService.java`
- 创建：`backend/src/main/java/com/travelmouse/service/RouteService.java`

- [ ] **步骤 1：实现 DailyPlanService**

```java
package com.travelmouse.service;

import com.travelmouse.entity.DailyPlan;
import com.travelmouse.repository.DailyPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DailyPlanService {
    private final DailyPlanRepository dailyPlanRepository;

    public DailyPlanService(DailyPlanRepository dailyPlanRepository) {
        this.dailyPlanRepository = dailyPlanRepository;
    }

    public DailyPlan findById(Long id) {
        return dailyPlanRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("单天计划不存在: " + id));
    }

    public List<DailyPlan> findByTravelPlanId(Long travelPlanId) {
        return dailyPlanRepository.findByTravelPlanIdOrderBySortOrder(travelPlanId);
    }

    @Transactional
    public DailyPlan saveDay(Long dayId) {
        DailyPlan plan = findById(dayId);
        plan.setStatus("done");
        return dailyPlanRepository.save(plan);
    }
}
```

- [ ] **步骤 2：实现 DestinationService**

```java
package com.travelmouse.service;

import com.travelmouse.entity.DailyPlan;
import com.travelmouse.entity.Destination;
import com.travelmouse.entity.DestinationImage;
import com.travelmouse.repository.DestinationImageRepository;
import com.travelmouse.repository.DestinationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class DestinationService {
    private final DestinationRepository destinationRepository;
    private final DestinationImageRepository imageRepository;
    private final DailyPlanService dailyPlanService;

    @Value("${app.upload-dir}")
    private String uploadDir;

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    public DestinationService(DestinationRepository destinationRepository,
                              DestinationImageRepository imageRepository,
                              DailyPlanService dailyPlanService) {
        this.destinationRepository = destinationRepository;
        this.imageRepository = imageRepository;
        this.dailyPlanService = dailyPlanService;
    }

    public List<Destination> findByDailyPlanId(Long dailyPlanId) {
        return destinationRepository.findByDailyPlanIdOrderBySortOrder(dailyPlanId);
    }

    @Transactional
    public Destination create(Long dailyPlanId, Destination dest) {
        DailyPlan plan = dailyPlanService.findById(dailyPlanId);
        dest.setDailyPlan(plan);
        List<Destination> existing = destinationRepository.findByDailyPlanIdOrderBySortOrder(dailyPlanId);
        dest.setSortOrder(existing.size());
        return destinationRepository.save(dest);
    }

    @Transactional
    public Destination update(Long id, Destination updated) {
        Destination dest = destinationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("目的地不存在: " + id));
        if (updated.getName() != null) dest.setName(updated.getName());
        if (updated.getNoteText() != null) dest.setNoteText(updated.getNoteText());
        if (updated.getArriveTime() != null) dest.setArriveTime(updated.getArriveTime());
        if (updated.getDurationMinutes() != null) dest.setDurationMinutes(updated.getDurationMinutes());
        if (updated.getLeaveTime() != null) dest.setLeaveTime(updated.getLeaveTime());
        if (updated.getInRoute() != null) dest.setInRoute(updated.getInRoute());
        if (updated.getRouteOrder() != null) dest.setRouteOrder(updated.getRouteOrder());
        return destinationRepository.save(dest);
    }

    @Transactional
    public void delete(Long id) {
        Destination dest = destinationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("目的地不存在: " + id));
        destinationRepository.delete(dest);
    }

    @Transactional
    public DestinationImage uploadImage(Long destinationId, MultipartFile file) throws IOException {
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("仅支持 jpg/png/webp 格式");
        }
        Destination dest = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new NoSuchElementException("目的地不存在: " + destinationId));

        String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
        String filename = UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        Path filePath = dir.resolve(filename);
        file.transferTo(filePath.toFile());

        DestinationImage img = new DestinationImage();
        img.setDestination(dest);
        img.setFilePath("/uploads/" + filename);
        return imageRepository.save(img);
    }

    @Transactional
    public void deleteImage(Long imageId) {
        DestinationImage img = imageRepository.findById(imageId)
                .orElseThrow(() -> new NoSuchElementException("图片不存在: " + imageId));
        imageRepository.delete(img);
    }
}
```

- [ ] **步骤 3：实现 RouteService**

```java
package com.travelmouse.service;

import com.travelmouse.dto.RouteUpdateRequest;
import com.travelmouse.entity.DailyPlan;
import com.travelmouse.entity.Destination;
import com.travelmouse.entity.RouteSegment;
import com.travelmouse.repository.DestinationRepository;
import com.travelmouse.repository.RouteSegmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RouteService {
    private final RouteSegmentRepository routeSegmentRepository;
    private final DestinationRepository destinationRepository;
    private final DailyPlanService dailyPlanService;

    public RouteService(RouteSegmentRepository routeSegmentRepository,
                        DestinationRepository destinationRepository,
                        DailyPlanService dailyPlanService) {
        this.routeSegmentRepository = routeSegmentRepository;
        this.destinationRepository = destinationRepository;
        this.dailyPlanService = dailyPlanService;
    }

    public List<RouteSegment> findByDailyPlanId(Long dailyPlanId) {
        return routeSegmentRepository.findByDailyPlanIdOrderByRouteOrder(dailyPlanId);
    }

    @Transactional
    public List<RouteSegment> updateRoute(Long dailyPlanId, RouteUpdateRequest request) {
        DailyPlan plan = dailyPlanService.findById(dailyPlanId);
        routeSegmentRepository.deleteByDailyPlanId(dailyPlanId);

        List<RouteSegment> segments = new ArrayList<>();
        for (RouteUpdateRequest.SegmentItem item : request.getSegments()) {
            Destination from = destinationRepository.findById(item.getFromDestId())
                    .orElseThrow(() -> new NoSuchElementException("起点目的地不存在"));
            Destination to = destinationRepository.findById(item.getToDestId())
                    .orElseThrow(() -> new NoSuchElementException("终点目的地不存在"));

            RouteSegment seg = new RouteSegment();
            seg.setDailyPlan(plan);
            seg.setFromDestination(from);
            seg.setToDestination(to);
            seg.setTransportMode(item.getTransportMode());
            seg.setDurationMinutes(item.getDurationMinutes());
            seg.setDistanceMeters(item.getDistanceMeters());
            seg.setRouteOrder(item.getRouteOrder());
            segments.add(routeSegmentRepository.save(seg));
        }
        return segments;
    }
}
```

- [ ] **步骤 4：验证编译**

运行：`cd backend && mvn compile -q`
预期：BUILD SUCCESS

- [ ] **步骤 5：Commit**

```bash
git add backend/src/main/java/com/travelmouse/service/
git commit -m "feat: 添加单天计划、目的地、路线 Service 层"
```

---

## 任务 8：Controller 层

**文件：**
- 创建：`backend/src/main/java/com/travelmouse/controller/FavoriteController.java`
- 创建：`backend/src/main/java/com/travelmouse/controller/TravelPlanController.java`
- 创建：`backend/src/main/java/com/travelmouse/controller/DailyPlanController.java`
- 创建：`backend/src/main/java/com/travelmouse/controller/DestinationController.java`
- 创建：`backend/src/main/java/com/travelmouse/controller/RouteController.java`
- 测试：`backend/src/test/java/com/travelmouse/controller/FavoriteControllerTest.java`

- [ ] **步骤 1：创建 FavoriteController**

```java
package com.travelmouse.controller;

import com.travelmouse.dto.ApiResponse;
import com.travelmouse.entity.Favorite;
import com.travelmouse.service.FavoriteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ApiResponse<List<Favorite>> list() {
        return ApiResponse.success(favoriteService.findAll());
    }

    @PostMapping
    public ApiResponse<Favorite> create(@RequestBody Favorite favorite) {
        return ApiResponse.success(favoriteService.create(favorite));
    }

    @PutMapping("/{id}")
    public ApiResponse<Favorite> update(@PathVariable Long id, @RequestBody Favorite favorite) {
        return ApiResponse.success(favoriteService.update(id, favorite));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        favoriteService.delete(id);
        return ApiResponse.success(null);
    }
}
```

- [ ] **步骤 2：创建 TravelPlanController**

```java
package com.travelmouse.controller;

import com.travelmouse.dto.ApiResponse;
import com.travelmouse.dto.TravelPlanCreateRequest;
import com.travelmouse.entity.TravelPlan;
import com.travelmouse.service.TravelPlanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class TravelPlanController {
    private final TravelPlanService travelPlanService;

    public TravelPlanController(TravelPlanService travelPlanService) {
        this.travelPlanService = travelPlanService;
    }

    @GetMapping
    public ApiResponse<List<TravelPlan>> list() {
        return ApiResponse.success(travelPlanService.findAll());
    }

    @PostMapping
    public ApiResponse<TravelPlan> create(@Valid @RequestBody TravelPlanCreateRequest req) {
        return ApiResponse.success(travelPlanService.create(req.getName(), req.getStartDate(), req.getEndDate()));
    }

    @GetMapping("/{id}")
    public ApiResponse<TravelPlan> detail(@PathVariable Long id) {
        return ApiResponse.success(travelPlanService.findById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<TravelPlan> update(@PathVariable Long id, @Valid @RequestBody TravelPlanCreateRequest req) {
        return ApiResponse.success(travelPlanService.update(id, req.getName(), req.getStartDate(), req.getEndDate()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        travelPlanService.delete(id);
        return ApiResponse.success(null);
    }
}
```

- [ ] **步骤 3：创建 DailyPlanController**

```java
package com.travelmouse.controller;

import com.travelmouse.dto.ApiResponse;
import com.travelmouse.entity.DailyPlan;
import com.travelmouse.service.DailyPlanService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plans/{planId}/days")
public class DailyPlanController {
    private final DailyPlanService dailyPlanService;

    public DailyPlanController(DailyPlanService dailyPlanService) {
        this.dailyPlanService = dailyPlanService;
    }

    @GetMapping("/{dayId}")
    public ApiResponse<DailyPlan> detail(@PathVariable Long planId, @PathVariable Long dayId) {
        return ApiResponse.success(dailyPlanService.findById(dayId));
    }

    @PutMapping("/{dayId}/save")
    public ApiResponse<DailyPlan> saveDay(@PathVariable Long planId, @PathVariable Long dayId) {
        return ApiResponse.success(dailyPlanService.saveDay(dayId));
    }
}
```

- [ ] **步骤 4：创建 DestinationController**

```java
package com.travelmouse.controller;

import com.travelmouse.dto.ApiResponse;
import com.travelmouse.entity.Destination;
import com.travelmouse.entity.DestinationImage;
import com.travelmouse.service.DestinationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/days/{dayId}/destinations")
public class DestinationController {
    private final DestinationService destinationService;

    public DestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @GetMapping
    public ApiResponse<List<Destination>> list(@PathVariable Long dayId) {
        return ApiResponse.success(destinationService.findByDailyPlanId(dayId));
    }

    @PostMapping
    public ApiResponse<Destination> create(@PathVariable Long dayId, @RequestBody Destination dest) {
        return ApiResponse.success(destinationService.create(dayId, dest));
    }

    @PutMapping("/{id}")
    public ApiResponse<Destination> update(@PathVariable Long dayId, @PathVariable Long id,
                                           @RequestBody Destination dest) {
        return ApiResponse.success(destinationService.update(id, dest));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long dayId, @PathVariable Long id) {
        destinationService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/images")
    public ApiResponse<DestinationImage> uploadImage(@PathVariable Long dayId, @PathVariable Long id,
                                                     @RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.success(destinationService.uploadImage(id, file));
    }

    @DeleteMapping("/{id}/images/{imgId}")
    public ApiResponse<Void> deleteImage(@PathVariable Long dayId, @PathVariable Long id,
                                         @PathVariable Long imgId) {
        destinationService.deleteImage(imgId);
        return ApiResponse.success(null);
    }
}
```

- [ ] **步骤 5：创建 RouteController**

```java
package com.travelmouse.controller;

import com.travelmouse.dto.ApiResponse;
import com.travelmouse.dto.RouteUpdateRequest;
import com.travelmouse.entity.RouteSegment;
import com.travelmouse.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/days/{dayId}/route")
public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public ApiResponse<List<RouteSegment>> getRoute(@PathVariable Long dayId) {
        return ApiResponse.success(routeService.findByDailyPlanId(dayId));
    }

    @PutMapping
    public ApiResponse<List<RouteSegment>> updateRoute(@PathVariable Long dayId,
                                                       @Valid @RequestBody RouteUpdateRequest request) {
        return ApiResponse.success(routeService.updateRoute(dayId, request));
    }
}
```

- [ ] **步骤 6：编写 FavoriteController MockMvc 测试**

```java
package com.travelmouse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelmouse.entity.Favorite;
import com.travelmouse.service.FavoriteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoriteController.class)
class FavoriteControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoriteService favoriteService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listFavorites_shouldReturn200() throws Exception {
        Favorite fav = new Favorite();
        fav.setId(1L);
        fav.setName("西湖");
        fav.setAddress("杭州");
        fav.setLongitude(new BigDecimal("120.1485"));
        fav.setLatitude(new BigDecimal("30.2741"));
        when(favoriteService.findAll()).thenReturn(List.of(fav));

        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("西湖"));
    }

    @Test
    void createFavorite_shouldReturn201() throws Exception {
        Favorite fav = new Favorite();
        fav.setName("灵隐寺");
        fav.setAddress("杭州");
        fav.setLongitude(new BigDecimal("120.10"));
        fav.setLatitude(new BigDecimal("30.24"));
        when(favoriteService.create(any())).thenReturn(fav);

        mockMvc.perform(post("/api/favorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fav)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("灵隐寺"));
    }
}
```

- [ ] **步骤 7：运行所有测试**

运行：`cd backend && mvn test -q`
预期：ALL PASS

- [ ] **步骤 8：Commit**

```bash
git add backend/src/main/java/com/travelmouse/controller/ backend/src/test/
git commit -m "feat: 添加全部 Controller 层（含 MockMvc 测试）"
```

---

## 任务 9：前端项目初始化

**文件：**
- 创建：`frontend/` 整个 Vue 3 项目

- [ ] **步骤 1：使用 Vite 初始化 Vue 3 项目**

```bash
cd /home/dominiczhu/Coding/talk-is-cheap/AI/hello-superpowers-zh/projects/travel-mouse
npm create vite@latest frontend -- --template vue
cd frontend
npm install
npm install vue-router@4 axios
npm install @amap/amap-jsapi-loader
```

- [ ] **步骤 2：配置 vite.config.js（代理）**

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

- [ ] **步骤 3：创建 .env**

```
VITE_AMAP_KEY=你的高德Key
VITE_AMAP_SECURITY_CODE=你的安全密钥
```

- [ ] **步骤 4：配置路由 src/router/index.js**

```javascript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/HomeView.vue') },
  { path: '/favorites', name: 'Favorites', component: () => import('../views/FavoritesView.vue') },
  { path: '/plan/create', name: 'PlanCreate', component: () => import('../views/PlanCreateView.vue') },
  { path: '/plan/:id', name: 'PlanDetail', component: () => import('../views/PlanDetailView.vue') },
  { path: '/plan/:id/day/:dayId', name: 'DayPlan', component: () => import('../views/DayPlanView.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
```

- [ ] **步骤 5：配置 main.js**

```javascript
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

createApp(App).use(router).mount('#app')
```

- [ ] **步骤 6：创建 App.vue（基础布局）**

```vue
<template>
  <div id="app">
    <nav class="app-nav">
      <router-link to="/">首页</router-link>
      <router-link to="/favorites">收藏</router-link>
      <router-link to="/plan/create">新建计划</router-link>
    </nav>
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; }
.app-nav {
  display: flex; gap: 1rem; padding: 0.75rem 1.5rem;
  background: #fff; border-bottom: 1px solid #e5e7eb;
}
.app-nav a { text-decoration: none; color: #374151; font-weight: 500; }
.app-nav a.router-link-active { color: #2563eb; }
.app-main { height: calc(100vh - 49px); }
</style>
```

- [ ] **步骤 7：验证前端可启动**

运行：`cd frontend && npm run dev`
预期：浏览器打开 http://localhost:5173 看到导航栏

- [ ] **步骤 8：Commit**

```bash
git add frontend/
git commit -m "feat: 初始化 Vue 3 前端项目（路由 + 代理 + 布局）"
```

---

## 任务 10：前端 API 层

**文件：**
- 创建：`frontend/src/api/request.js`
- 创建：`frontend/src/api/favorites.js`
- 创建：`frontend/src/api/plans.js`
- 创建：`frontend/src/api/destinations.js`

- [ ] **步骤 1：创建 axios 实例**

```javascript
// src/api/request.js
import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.response.use(
  response => {
    const { code, message, data } = response.data
    if (code !== 200) {
      alert(message || '请求失败')
      return Promise.reject(new Error(message))
    }
    return data
  },
  error => {
    alert(error.response?.data?.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
```

- [ ] **步骤 2：创建 favorites API**

```javascript
// src/api/favorites.js
import request from './request'

export const getFavorites = () => request.get('/favorites')
export const createFavorite = (data) => request.post('/favorites', data)
export const updateFavorite = (id, data) => request.put(`/favorites/${id}`, data)
export const deleteFavorite = (id) => request.delete(`/favorites/${id}`)
```

- [ ] **步骤 3：创建 plans API**

```javascript
// src/api/plans.js
import request from './request'

export const getPlans = () => request.get('/plans')
export const createPlan = (data) => request.post('/plans', data)
export const getPlan = (id) => request.get(`/plans/${id}`)
export const updatePlan = (id, data) => request.put(`/plans/${id}`, data)
export const deletePlan = (id) => request.delete(`/plans/${id}`)
export const saveDay = (planId, dayId) => request.put(`/plans/${planId}/days/${dayId}/save`)
```

- [ ] **步骤 4：创建 destinations API**

```javascript
// src/api/destinations.js
import request from './request'

export const getDestinations = (dayId) => request.get(`/days/${dayId}/destinations`)
export const createDestination = (dayId, data) => request.post(`/days/${dayId}/destinations`, data)
export const updateDestination = (dayId, id, data) => request.put(`/days/${dayId}/destinations/${id}`, data)
export const deleteDestination = (dayId, id) => request.delete(`/days/${dayId}/destinations/${id}`)
export const uploadImage = (dayId, id, file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/days/${dayId}/destinations/${id}/images`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
export const deleteImage = (dayId, id, imgId) => request.delete(`/days/${dayId}/destinations/${id}/images/${imgId}`)
export const getRoute = (dayId) => request.get(`/days/${dayId}/route`)
export const updateRoute = (dayId, data) => request.put(`/days/${dayId}/route`, data)
```

- [ ] **步骤 5：Commit**

```bash
git add frontend/src/api/
git commit -m "feat: 添加前端 API 调用层"
```

---

## 任务 11：前端页面（首页 + 收藏 + 创建/详情）

**文件：**
- 创建：`frontend/src/views/HomeView.vue`
- 创建：`frontend/src/views/FavoritesView.vue`
- 创建：`frontend/src/views/PlanCreateView.vue`
- 创建：`frontend/src/views/PlanDetailView.vue`

- [ ] **步骤 1：创建 HomeView（旅行计划列表）**

```vue
<template>
  <div class="home">
    <h1>我的旅行计划</h1>
    <div class="plan-list">
      <div v-for="plan in plans" :key="plan.id" class="plan-card" @click="$router.push(`/plan/${plan.id}`)">
        <h3>{{ plan.name }}</h3>
        <p>{{ plan.startDate }} ~ {{ plan.endDate }}</p>
        <button @click.stop="removePlan(plan.id)">删除</button>
      </div>
      <p v-if="plans.length === 0">还没有旅行计划，点击右上角「新建计划」开始吧！</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPlans, deletePlan } from '../api/plans'

const plans = ref([])

onMounted(async () => {
  plans.value = await getPlans()
})

const removePlan = async (id) => {
  if (confirm('确定删除该计划？')) {
    await deletePlan(id)
    plans.value = plans.value.filter(p => p.id !== id)
  }
}
</script>

<style scoped>
.home { padding: 2rem; }
.plan-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1rem; margin-top: 1rem; }
.plan-card { border: 1px solid #e5e7eb; border-radius: 8px; padding: 1rem; cursor: pointer; }
.plan-card:hover { border-color: #2563eb; }
</style>
```

- [ ] **步骤 2：创建 FavoritesView**

```vue
<template>
  <div class="favorites">
    <h1>我的收藏</h1>
    <div class="fav-list">
      <div v-for="fav in favorites" :key="fav.id" class="fav-card">
        <h3>{{ fav.name }}</h3>
        <p>{{ fav.address }}</p>
        <p v-if="fav.category" class="tag">{{ fav.category }}</p>
        <button @click="remove(fav.id)">删除</button>
      </div>
      <p v-if="favorites.length === 0">还没有收藏，在地图页面中点击「收藏」按钮添加。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getFavorites, deleteFavorite } from '../api/favorites'

const favorites = ref([])

onMounted(async () => {
  favorites.value = await getFavorites()
})

const remove = async (id) => {
  await deleteFavorite(id)
  favorites.value = favorites.value.filter(f => f.id !== id)
}
</script>

<style scoped>
.favorites { padding: 2rem; }
.fav-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 1rem; margin-top: 1rem; }
.fav-card { border: 1px solid #e5e7eb; border-radius: 8px; padding: 1rem; }
.tag { color: #2563eb; font-size: 0.85rem; }
</style>
```

- [ ] **步骤 3：创建 PlanCreateView**

```vue
<template>
  <div class="plan-create">
    <h1>新建旅行计划</h1>
    <form @submit.prevent="submit">
      <label>计划名称
        <input v-model="form.name" placeholder="如：杭州三日游" required />
      </label>
      <label>起始日期
        <input type="date" v-model="form.startDate" required />
      </label>
      <label>结束日期
        <input type="date" v-model="form.endDate" required />
      </label>
      <button type="submit">创建</button>
    </form>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { createPlan } from '../api/plans'

const router = useRouter()
const form = reactive({ name: '', startDate: '', endDate: '' })

const submit = async () => {
  const plan = await createPlan(form)
  router.push(`/plan/${plan.id}`)
}
</script>

<style scoped>
.plan-create { padding: 2rem; max-width: 400px; }
form { display: flex; flex-direction: column; gap: 1rem; margin-top: 1rem; }
label { display: flex; flex-direction: column; gap: 0.25rem; font-weight: 500; }
input { padding: 0.5rem; border: 1px solid #d1d5db; border-radius: 4px; }
button { padding: 0.5rem 1rem; background: #2563eb; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
</style>
```

- [ ] **步骤 4：创建 PlanDetailView**

```vue
<template>
  <div class="plan-detail" v-if="plan">
    <h1>{{ plan.name }}</h1>
    <p>{{ plan.startDate }} ~ {{ plan.endDate }}</p>
    <div class="day-list">
      <div v-for="day in plan.dailyPlans" :key="day.id" class="day-card"
           @click="$router.push(`/plan/${plan.id}/day/${day.id}`)">
        <h3>第 {{ day.sortOrder }} 天</h3>
        <p>{{ day.planDate }}</p>
        <span :class="['status', day.status]">{{ day.status === 'done' ? '已完成' : '草稿' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPlan } from '../api/plans'

const route = useRoute()
const plan = ref(null)

onMounted(async () => {
  plan.value = await getPlan(route.params.id)
})
</script>

<style scoped>
.plan-detail { padding: 2rem; }
.day-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1rem; margin-top: 1rem; }
.day-card { border: 1px solid #e5e7eb; border-radius: 8px; padding: 1rem; cursor: pointer; }
.day-card:hover { border-color: #2563eb; }
.status { font-size: 0.8rem; padding: 2px 8px; border-radius: 4px; }
.status.done { background: #d1fae5; color: #065f46; }
.status.draft { background: #fef3c7; color: #92400e; }
</style>
```

- [ ] **步骤 5：验证页面可访问**

运行：`cd frontend && npm run dev`
预期：各路由页面可正常渲染（无后端时 API 调用会报错，但页面结构正常）

- [ ] **步骤 6：Commit**

```bash
git add frontend/src/views/ frontend/src/router/
git commit -m "feat: 添加首页、收藏、计划创建/详情页面"
```

---

## 任务 12：前端核心页面 — DayPlanView + 地图组件

**文件：**
- 创建：`frontend/src/composables/useAmap.js`
- 创建：`frontend/src/composables/usePoiSearch.js`
- 创建：`frontend/src/composables/useRoutePlanning.js`
- 创建：`frontend/src/views/DayPlanView.vue`
- 创建：`frontend/src/components/MapContainer.vue`
- 创建：`frontend/src/components/LeftPanel.vue`
- 创建：`frontend/src/components/PendingList.vue`
- 创建：`frontend/src/components/DestinationCard.vue`
- 创建：`frontend/src/components/AddDestination.vue`
- 创建：`frontend/src/components/RightPanel.vue`
- 创建：`frontend/src/components/RoutePlan.vue`
- 创建：`frontend/src/components/RouteActions.vue`

- [ ] **步骤 1：创建 useAmap composable**

```javascript
// src/composables/useAmap.js
import AMapLoader from '@amap/amap-jsapi-loader'
import { ref } from 'vue'

export function useAmap() {
  const map = ref(null)
  const loaded = ref(false)

  const initMap = async (containerId) => {
    window._AMapSecurityConfig = {
      securityJsCode: import.meta.env.VITE_AMAP_SECURITY_CODE
    }
    const AMap = await AMapLoader.load({
      key: import.meta.env.VITE_AMAP_KEY,
      version: '2.0',
      plugins: ['AMap.PlaceSearch', 'AMap.Driving', 'AMap.Walking', 'AMap.Transfer']
    })
    map.value = new AMap.Map(containerId, {
      zoom: 12,
      center: [120.15, 30.28] // 默认杭州
    })
    loaded.value = true
    return { map: map.value, AMap }
  }

  return { map, loaded, initMap }
}
```

- [ ] **步骤 2：创建 usePoiSearch composable**

```javascript
// src/composables/usePoiSearch.js
import { ref } from 'vue'

export function usePoiSearch(AMap) {
  const results = ref([])
  const searching = ref(false)

  const search = (keyword, city = '全国') => {
    return new Promise((resolve) => {
      searching.value = true
      const placeSearch = new AMap.PlaceSearch({ city, pageSize: 10 })
      placeSearch.search(keyword, (status, result) => {
        searching.value = false
        if (status === 'complete' && result.poiList) {
          results.value = result.poiList.pois.map(poi => ({
            name: poi.name,
            address: poi.address || '',
            longitude: poi.location.lng,
            latitude: poi.location.lat,
            category: poi.type || ''
          }))
        } else {
          results.value = []
        }
        resolve(results.value)
      })
    })
  }

  return { results, searching, search }
}
```

- [ ] **步骤 3：创建 useRoutePlanning composable**

```javascript
// src/composables/useRoutePlanning.js
import { ref } from 'vue'

export function useRoutePlanning(AMap) {
  const calculating = ref(false)

  // 计算从 from 到多个 to 的驾车时间
  const calcDrivingTime = (from, toList) => {
    return new Promise((resolve) => {
      calculating.value = true
      const driving = new AMap.Driving({ policy: 0 })
      const results = []
      let completed = 0

      if (toList.length === 0) {
        calculating.value = false
        resolve([])
        return
      }

      toList.forEach((to, index) => {
        driving.search(
          [from.longitude, from.latitude],
          [to.longitude, to.latitude],
          (status, result) => {
            completed++
            if (status === 'complete' && result.routes?.length) {
              const route = result.routes[0]
              results[index] = {
                destId: to.id,
                durationMinutes: Math.round(route.time / 60),
                distanceMeters: route.distance,
                transportMode: 'driving'
              }
            } else {
              results[index] = { destId: to.id, durationMinutes: null, distanceMeters: null, transportMode: 'driving' }
            }
            if (completed === toList.length) {
              calculating.value = false
              resolve(results)
            }
          }
        )
      })
    })
  }

  // 计算两点之间路线（用于绘制）
  const calcRoute = (from, to, mode = 'driving') => {
    return new Promise((resolve) => {
      let planner
      if (mode === 'walking') {
        planner = new AMap.Walking()
      } else {
        planner = new AMap.Driving({ policy: 0 })
      }
      planner.search(
        [from.longitude, from.latitude],
        [to.longitude, to.latitude],
        (status, result) => {
          if (status === 'complete' && result.routes?.length) {
            resolve(result.routes[0])
          } else {
            resolve(null)
          }
        }
      )
    })
  }

  return { calculating, calcDrivingTime, calcRoute }
}
```

- [ ] **步骤 4：创建 MapContainer 组件**

```vue
<template>
  <div id="amap-container" class="map-container"></div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAmap } from '../composables/useAmap'

const { initMap } = useAmap()
const emit = defineEmits(['map-ready'])

onMounted(async () => {
  const { map, AMap } = await initMap('amap-container')
  emit('map-ready', { map, AMap })
})
</script>

<style scoped>
.map-container { width: 100%; height: 100%; }
</style>
```

- [ ] **步骤 5：创建 DayPlanView（核心页面框架）**

```vue
<template>
  <div class="day-plan">
    <LeftPanel
      :dayId="dayId"
      :destinations="pendingList"
      :travel-times="travelTimes"
      @add-to-route="addToRoute"
      @remove-dest="removeDest"
      @add-dest="addDest"
    />
    <MapContainer @map-ready="onMapReady" />
    <RightPanel
      :route-list="routeList"
      :segments="routeSegments"
      @rollback="rollback"
      @save-day="saveDay"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import MapContainer from '../components/MapContainer.vue'
import LeftPanel from '../components/LeftPanel.vue'
import RightPanel from '../components/RightPanel.vue'
import { usePoiSearch } from '../composables/usePoiSearch'
import { useRoutePlanning } from '../composables/useRoutePlanning'
import { getDestinations, createDestination, deleteDestination, updateDestination } from '../api/destinations'
import { getRoute, updateRoute } from '../api/destinations'
import { saveDay as apiSaveDay } from '../api/plans'

const route = useRoute()
const dayId = route.params.dayId
const planId = route.params.id

const destinations = ref([])
const routeList = ref([])      // 已加入路线的目的地（有序）
const routeSegments = ref([])  // 路线段
const travelTimes = ref([])    // 当前所在地到各未加入目的地的时间
const currentLocation = ref(null) // 当前所在地

let mapInstance = null
let AMapInstance = null
let routePlanner = null
let markers = []

const pendingList = computed(() => destinations.value.filter(d => !d.inRoute))

const onMapReady = ({ map, AMap }) => {
  mapInstance = map
  AMapInstance = AMap
  routePlanner = useRoutePlanning(AMap)
}

onMounted(async () => {
  destinations.value = await getDestinations(dayId)
  routeList.value = destinations.value.filter(d => d.inRoute).sort((a, b) => a.routeOrder - b.routeOrder)
  if (routeList.value.length > 0) {
    currentLocation.value = routeList.value[routeList.value.length - 1]
  }
  routeSegments.value = await getRoute(dayId)
})

const addDest = async (poi) => {
  const dest = await createDestination(dayId, poi)
  destinations.value.push(dest)
}

const removeDest = async (id) => {
  await deleteDestination(dayId, id)
  destinations.value = destinations.value.filter(d => d.id !== id)
}

const addToRoute = async (dest) => {
  const order = routeList.value.length
  await updateDestination(dayId, dest.id, { inRoute: true, routeOrder: order })
  dest.inRoute = true
  dest.routeOrder = order
  routeList.value.push(dest)
  currentLocation.value = dest

  // 计算到剩余目的地的时间
  if (routePlanner && pendingList.value.length > 0) {
    travelTimes.value = await routePlanner.calcDrivingTime(dest, pendingList.value)
  }

  // 添加地图标记
  if (mapInstance && AMapInstance) {
    const marker = new AMapInstance.Marker({
      position: [dest.longitude, dest.latitude],
      title: dest.name
    })
    mapInstance.add(marker)
    markers.push(marker)
  }
}

const rollback = async () => {
  if (routeList.value.length === 0) return
  const last = routeList.value.pop()
  await updateDestination(dayId, last.id, { inRoute: false, routeOrder: null })
  last.inRoute = false
  last.routeOrder = null
  currentLocation.value = routeList.value.length > 0 ? routeList.value[routeList.value.length - 1] : null
  travelTimes.value = []
  if (markers.length) {
    mapInstance.remove(markers.pop())
  }
}

const saveDay = async () => {
  // 保存路线段
  if (routeList.value.length > 1 && routePlanner) {
    const segments = []
    for (let i = 0; i < routeList.value.length - 1; i++) {
      const from = routeList.value[i]
      const to = routeList.value[i + 1]
      const routeResult = await routePlanner.calcRoute(from, to)
      segments.push({
        fromDestId: from.id,
        toDestId: to.id,
        transportMode: 'driving',
        durationMinutes: routeResult ? Math.round(routeResult.time / 60) : 0,
        distanceMeters: routeResult ? routeResult.distance : 0,
        routeOrder: i
      })
    }
    await updateRoute(dayId, { segments })
  }
  await apiSaveDay(planId, dayId)
  alert('当天计划已保存！')
}
</script>

<style scoped>
.day-plan {
  display: flex;
  height: 100%;
}
.day-plan > :first-child { width: 22%; overflow-y: auto; border-right: 1px solid #e5e7eb; }
.day-plan > :nth-child(2) { flex: 1; }
.day-plan > :last-child { width: 22%; overflow-y: auto; border-left: 1px solid #e5e7eb; }
</style>
```

- [ ] **步骤 6：创建 LeftPanel + PendingList + AddDestination + DestinationCard**

```vue
<!-- src/components/LeftPanel.vue -->
<template>
  <div class="left-panel">
    <h3>待定目的地</h3>
    <AddDestination @select="$emit('add-dest', $event)" />
    <PendingList
      :destinations="destinations"
      :travel-times="travelTimes"
      @add-to-route="$emit('add-to-route', $event)"
      @remove="$emit('remove-dest', $event)"
    />
  </div>
</template>

<script setup>
import AddDestination from './AddDestination.vue'
import PendingList from './PendingList.vue'

defineProps(['dayId', 'destinations', 'travelTimes'])
defineEmits(['add-to-route', 'remove-dest', 'add-dest'])
</script>

<style scoped>
.left-panel { padding: 1rem; }
</style>
```

```vue
<!-- src/components/AddDestination.vue -->
<template>
  <div class="add-dest">
    <input v-model="keyword" placeholder="搜索目的地..." @keyup.enter="doSearch" />
    <button @click="doSearch">搜索</button>
    <ul v-if="results.length" class="search-results">
      <li v-for="(r, i) in results" :key="i" @click="$emit('select', r)">
        <strong>{{ r.name }}</strong>
        <span>{{ r.address }}</span>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { usePoiSearch } from '../composables/usePoiSearch'

defineEmits(['select'])

const keyword = ref('')
const results = ref([])
let AMap = null

// 地图加载后由父组件注入 AMap 实例（简化处理：延迟加载）
const doSearch = async () => {
  if (!keyword.value.trim()) return
  // 动态加载 AMap
  if (!AMap) {
    window._AMapSecurityConfig = { securityJsCode: import.meta.env.VITE_AMAP_SECURITY_CODE }
    const loader = await import('@amap/amap-jsapi-loader')
    AMap = await loader.default.load({
      key: import.meta.env.VITE_AMAP_KEY,
      version: '2.0',
      plugins: ['AMap.PlaceSearch']
    })
  }
  const { search } = usePoiSearch(AMap)
  results.value = await search(keyword.value)
}
</script>

<style scoped>
.add-dest { margin-bottom: 1rem; }
.add-dest input { width: 70%; padding: 0.4rem; border: 1px solid #d1d5db; border-radius: 4px; }
.add-dest button { padding: 0.4rem 0.8rem; margin-left: 0.5rem; }
.search-results { list-style: none; margin-top: 0.5rem; max-height: 200px; overflow-y: auto; }
.search-results li { padding: 0.4rem; cursor: pointer; border-bottom: 1px solid #f3f4f6; }
.search-results li:hover { background: #eff6ff; }
.search-results span { display: block; font-size: 0.8rem; color: #6b7280; }
</style>
```

```vue
<!-- src/components/PendingList.vue -->
<template>
  <div class="pending-list">
    <DestinationCard
      v-for="dest in destinations" :key="dest.id"
      :dest="dest"
      :travel-time="getTravelTime(dest.id)"
      @add-to-route="$emit('add-to-route', dest)"
      @remove="$emit('remove', dest.id)"
    />
    <p v-if="destinations.length === 0" class="empty">暂无待定目的地</p>
  </div>
</template>

<script setup>
import DestinationCard from './DestinationCard.vue'

const props = defineProps(['destinations', 'travelTimes'])
defineEmits(['add-to-route', 'remove'])

const getTravelTime = (destId) => {
  const t = props.travelTimes?.find(tt => tt.destId === destId)
  return t?.durationMinutes != null ? `${t.durationMinutes}分钟` : null
}
</script>
```

```vue
<!-- src/components/DestinationCard.vue -->
<template>
  <div class="dest-card">
    <div class="dest-info">
      <strong>{{ dest.name }}</strong>
      <span class="addr">{{ dest.address }}</span>
      <span v-if="travelTime" class="time">🚗 {{ travelTime }}</span>
    </div>
    <div class="dest-actions">
      <button class="btn-add" @click="$emit('add-to-route')">加入路线</button>
      <button class="btn-del" @click="$emit('remove')">删除</button>
    </div>
  </div>
</template>

<script setup>
defineProps(['dest', 'travelTime'])
defineEmits(['add-to-route', 'remove'])
</script>

<style scoped>
.dest-card { padding: 0.6rem; border: 1px solid #e5e7eb; border-radius: 6px; margin-bottom: 0.5rem; }
.dest-info { display: flex; flex-direction: column; gap: 2px; }
.addr { font-size: 0.8rem; color: #6b7280; }
.time { font-size: 0.8rem; color: #059669; }
.dest-actions { margin-top: 0.4rem; display: flex; gap: 0.5rem; }
.btn-add { font-size: 0.8rem; padding: 2px 8px; background: #2563eb; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
.btn-del { font-size: 0.8rem; padding: 2px 8px; background: #ef4444; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
</style>
```

- [ ] **步骤 7：创建 RightPanel + RoutePlan + RouteActions**

```vue
<!-- src/components/RightPanel.vue -->
<template>
  <div class="right-panel">
    <h3>路线规划</h3>
    <RoutePlan :route-list="routeList" :segments="segments" />
    <RouteActions @rollback="$emit('rollback')" @save="$emit('save-day')" />
  </div>
</template>

<script setup>
import RoutePlan from './RoutePlan.vue'
import RouteActions from './RouteActions.vue'

defineProps(['routeList', 'segments'])
defineEmits(['rollback', 'save-day'])
</script>

<style scoped>
.right-panel { padding: 1rem; }
</style>
```

```vue
<!-- src/components/RoutePlan.vue -->
<template>
  <div class="route-plan">
    <div v-for="(dest, i) in routeList" :key="dest.id" class="route-item">
      <div class="route-dest">{{ i + 1 }}️⃣ {{ dest.name }}</div>
      <div v-if="i < routeList.length - 1" class="route-seg">
        ↓ 驾车 {{ getSegmentTime(i) }}
      </div>
    </div>
    <p v-if="routeList.length === 0" class="empty">路线为空，从左侧添加目的地</p>
  </div>
</template>

<script setup>
const props = defineProps(['routeList', 'segments'])

const getSegmentTime = (index) => {
  const seg = props.segments?.[index]
  return seg ? `${seg.durationMinutes}分钟` : '...'
}
</script>

<style scoped>
.route-item { margin-bottom: 0.25rem; }
.route-dest { padding: 0.4rem; background: #f9fafb; border-radius: 4px; }
.route-seg { text-align: center; font-size: 0.8rem; color: #6b7280; padding: 0.2rem; }
.empty { color: #9ca3af; font-size: 0.9rem; }
</style>
```

```vue
<!-- src/components/RouteActions.vue -->
<template>
  <div class="route-actions">
    <button class="btn-rollback" @click="$emit('rollback')">↩️ 回退</button>
    <button class="btn-save" @click="$emit('save')">💾 暂存当天</button>
  </div>
</template>

<script setup>
defineEmits(['rollback', 'save'])
</script>

<style scoped>
.route-actions { margin-top: 1rem; display: flex; flex-direction: column; gap: 0.5rem; }
.btn-rollback { padding: 0.5rem; background: #f59e0b; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
.btn-save { padding: 0.5rem; background: #10b981; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
</style>
```

- [ ] **步骤 8：验证前端编译**

运行：`cd frontend && npm run build`
预期：BUILD SUCCESS，无编译错误

- [ ] **步骤 9：Commit**

```bash
git add frontend/src/
git commit -m "feat: 添加核心 DayPlanView 页面（地图 + 待定列表 + 路线规划）"
```

---

## 任务 13：端到端验证

- [ ] **步骤 1：启动后端**

运行：`cd backend && mvn spring-boot:run`
预期：端口 8080 启动成功

- [ ] **步骤 2：启动前端**

运行：`cd frontend && npm run dev`
预期：端口 5173 启动成功

- [ ] **步骤 3：手动验证完整流程**

检查清单：
1. 创建旅行计划（名称 + 日期）→ 自动创建 daily_plans
2. 进入单天计划页面 → 地图加载正常
3. 搜索目的地 → 添加到待定列表
4. 加入路线 → 显示交通时间
5. 回退 → 移除最后一个节点
6. 暂存 → 状态变为 done
7. 收藏功能正常

- [ ] **步骤 4：修复发现的问题（如有）**

- [ ] **步骤 5：最终 Commit**

```bash
git add -A
git commit -m "feat: travel-mouse v1.0 完成"
```
