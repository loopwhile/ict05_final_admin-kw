# 재고 및 재료 관리 기능 명세서

## 1. 재료(Material) 관리

본사에서 사용하는 모든 재료의 마스터 정보를 관리합니다. 재료는 코드, 카테고리, 단위, 상태 등의 속성을 가집니다.

### 1.1. 재료 목록 조회

-   **기능**: 등록된 모든 재료를 검색하고 페이징하여 목록을 조회합니다.
-   **Frontend**:
    -   **경로**: `templates/material/list.html`
    -   **URL**: `/material/list`
    -   **주요 기능**:
        -   재료명, 카테고리로 검색 기능 제공
        -   '사용중', '중단' 상태별 필터링 기능
        -   테이블에 재료코드, 카테고리, 재료명, 단위, 공급업체, 상태 표시
        -   페이지네이션 제공
        -   '자재등록' 버튼, '엑셀다운로드' 버튼 제공
-   **Backend**:
    -   **Controller**: `MaterialController.java`
    -   **Method**: `listMaterial(MaterialSearchDTO, Pageable, ...)`
    -   **Endpoint**: `GET /material/list`
    -   **Service**: `MaterialService.selectAllMaterial()`
    -   **주요 로직**:
        -   `MaterialSearchDTO`를 이용해 재료명, 카테고리, 상태(USE/STOP)에 따른 동적 검색 쿼리 수행
        -   `Pageable` 객체를 통해 페이징 및 정렬(기본: ID 내림차순) 처리
        -   조회 결과를 `Page<MaterialListDTO>` 형태로 뷰에 전달

### 1.2. 재료 등록

-   **기능**: 새로운 재료 정보를 시스템에 등록합니다.
-   **Frontend**:
    -   **경로**: `templates/material/write.html`
    -   **URL**: `/material/write`
    -   **주요 기능**:
        -   재료명, 카테고리, 기본/판매 단위, 변환비율, 적정 수량, 공급업체, 보관온도 입력 폼 제공
        -   '저장' 버튼 클릭 시 `fetch` API를 통해 비동기 처리
-   **Backend**:
    -   **Controller**: `MaterialRestController.java`
    -   **Method**: `insertMaterial(@ModelAttribute MaterialWriteFormDTO, ...)`
    -   **Endpoint**: `POST /API/material/write`
    -   **Service**: `MaterialService.insertOfficeMaterial()`
    -   **주요 로직**:
        -   `@Valid`를 통한 서버 측 유효성 검증
        -   카테고리(`MaterialCategory`)에 따라 재료 코드 자동 생성 (예: `BAS0001`)
        -   기본 상태를 '사용중'(`MaterialStatus.USE`)으로 설정하여 저장
        -   저장 후 생성된 재료의 ID 반환

### 1.3. 재료 상세 조회

-   **기능**: 특정 재료의 모든 정보를 조회합니다.
-   **Frontend**:
    -   **경로**: `templates/material/detail.html`
    -   **URL**: `/material/detail/{id}`
    -   **주요 기능**:
        -   재료의 모든 속성(코드, 카테고리, 단위, 상태, 적정 수량 등)을 읽기 전용으로 표시
        -   '삭제', '수정' 버튼 제공
-   **Backend**:
    -   **Controller**: `MaterialController.java`
    -   **Method**: `detailOfficeMaterial(@PathVariable Long id, ...)`
    -   **Endpoint**: `GET /material/detail/{id}`
    -   **Service**: `MaterialService.detailMaterial()`
    -   **주요 로직**:
        -   `id`로 재료 정보를 조회하여 `Material` 엔티티를 뷰에 전달

### 1.4. 재료 수정

-   **기능**: 기존 재료의 정보를 수정합니다.
-   **Frontend**:
    -   **경로**: `templates/material/modify.html`
    -   **URL**: `/material/modify/{id}`
    -   **주요 기능**:
        -   상세 조회와 유사한 폼에 기존 데이터를 채워서 제공
        -   '저장' 버튼 클릭 시 `fetch` API를 통해 비동기 처리
-   **Backend**:
    -   **Controller**: `MaterialRestController.java`
    -   **Method**: `modifyMaterial(@ModelAttribute MaterialModifyFormDTO, ...)`
    -   **Endpoint**: `POST /API/material/modify`
    -   **Service**: `MaterialService.materialModify()`
    -   **주요 로직**:
        -   `id`로 기존 재료 엔티티를 조회
        -   DTO의 내용으로 엔티티 속성 업데이트 후 저장

### 1.5. 재료 삭제

-   **기능**: 재료 정보를 삭제합니다.
-   **Backend**:
    -   **Controller**: `MaterialController.java` & `MaterialRestController.java`
    -   **Endpoints**: `GET /material/delete/{id}` (SSR), `DELETE /API/material/delete` (API)
    -   **Service**: `MaterialService.deleteMaterial()`
    -   **주요 로직**:
        -   `id`에 해당하는 재료를 DB에서 삭제
        -   SSR의 경우 목록 페이지로 리다이렉트

### 1.6. 재료 목록 엑셀 다운로드

-   **기능**: 현재 필터 및 검색 조건이 적용된 재료 목록 전체를 Excel(XLSX) 파일로 다운로드합니다.
-   **Backend**:
    -   **Controller**: `MaterialRestController.java`
    -   **Method**: `downloadMaterial(MaterialSearchDTO, ...)`
    -   **Endpoint**: `GET /API/material/download`
    -   **Service**: `MaterialService.downloadExcel()`
    -   **주요 로직**:
        -   `MaterialSearchDTO`를 기반으로 전체 데이터를 조회
        -   `Apache POI` 라이브러리를 사용하여 XLSX 워크북 생성
        -   `byte[]` 형태로 파일 응답

---

## 2. 본사 재고(Inventory) 관리

재료를 기반으로 본사가 보유한 실제 재고의 수량, 상태, 입출고 내역 등을 관리합니다.

### 2.1. 본사 재고 목록 조회

-   **기능**: 본사가 보유한 모든 재료의 현재 재고 현황을 조회합니다.
-   **Frontend**:
    -   **경로**: `templates/inventory/list.html`
    -   **URL**: `/inventory/list`
    -   **주요 기능**:
        -   재료명, 카테고리로 재고 검색
        -   재고 상태('충분', '부족', '품절')별 필터링
        -   테이블에 재료코드, 카테고리, 재료명, 현재고, 상태 표시
        -   '입출고 로그', 'LOT 현황' 버튼 제공
-   **Backend**:
    -   **Controller**: `InventoryController.java`
    -   **Method**: `listInventory(InventorySearchDTO, ...)`
    -   **Endpoint**: `GET /inventory/list`
    -   **Service**: `InventoryService.listInventory()`
    -   **주요 로직**:
        -   `InventorySearchDTO`를 이용해 재료 및 재고 상태에 따른 동적 검색 수행
        -   페이징 처리하여 `Page<InventoryListDTO>` 형태로 뷰에 전달

### 2.2. 본사 재고 입고 등록

-   **기능**: 본사에 재료가 입고되었을 때, 재고를 증가시키고 이력을 남깁니다.
-   **Frontend**:
    -   **경로**: `templates/inventory/inventory_in_write.html`
    -   **URL**: `/inventory/in/write`
    -   **주요 기능**:
        -   카테고리 선택 시 해당 카테고리의 재료 목록을 동적으로 로드
        -   입고 수량, 단가, 입고일, 유통기한 등 입력 폼 제공
        -   '입고 등록' 시 `fetch` API를 통해 비동기 처리
-   **Backend**:
    -   **Controller**: `InventoryRestController.java`
    -   **Method**: `insertInventoryIn(@RequestBody InventoryInWriteDTO, ...)`
    -   **Endpoint**: `POST /API/inventory/in`
    -   **Service**: `InventoryInService.insertInventoryIn()`
    -   **주요 로직**:
        -   입고 정보(수량, 단가 등)를 받아 `InventoryIn` 이력 생성
        -   `InventoryStockService`를 호출하여 `Inventory`의 현재고 수량 증가
        -   `LOT-YYMMDD-xxxxxx` 형식의 `lotNo` 자동 생성
        -   `InventoryBatch` 엔티티(LOT 정보) 생성 및 저장

### 2.3. 본사 재고 출고

-   **기능**: 외부(가맹점 등)로 재고를 출고할 때, 재고를 감소시키고 이력을 남깁니다. FIFO(선입선출) 규칙을 따릅니다.
-   **Frontend**:
    -   **경로**: `templates/inventory/out_test.html` (테스트용)
    -   **URL**: `/inventory/out_test`
-   **Backend**:
    -   **Controller**: `InventoryRestController.java`
    -   **Endpoints**:
        -   `GET /API/inventory/out/preview`: 출고 미리보기. 요청 수량에 대해 어떤 LOT에서 얼마나 차감될지 계산하여 반환. 재고 부족 시 에러.
        -   `POST /API/inventory/out/confirm`: 출고 확정. FIFO 규칙으로 실제 재고(`InventoryBatch` 잔량)를 차감하고 `InventoryOut`, `InventoryOutLot` 이력 생성.
    -   **Service**: `InventoryOutService.previewFifo()`, `InventoryOutService.confirmOut()`
    -   **주요 로직**:
        -   **FIFO**: 유통기한이 가장 빠른 `InventoryBatch`부터 순차적으로 재고 차감
        -   출고 후 `InventoryStockService`를 통해 `Inventory` 현재고 동기화

### 2.4. 본사 재고 조정

-   **기능**: 분실, 파손, 실사 후 오차 등 입출고 외의 사유로 재고 수량을 직접 변경합니다.
-   **Frontend**:
    -   재고 로그 화면(`log.html`) 내 '조정' 버튼을 통해 모달 팝업으로 기능 제공
-   **Backend**:
    -   **Controller**: `InventoryRestController.java`
    -   **Method**: `adjustInventory(@RequestBody InventoryAdjustDTO, ...)`
    -   **Endpoint**: `POST /API/inventory/adjust`
    -   **Service**: `InventoryAdjustmentService.adjustInventory()`
    -   **주요 로직**:
        -   변경 후 수량과 사유(MANUAL, DAMAGE 등)를 받아 `InventoryAdjustment` 이력 생성
        -   `Inventory`의 현재고를 입력된 수량으로 직접 업데이트

### 2.5. 재고 로그 조회

-   **기능**: 특정 재료의 모든 입고, 출고, 조정 이력을 시간순으로 조회합니다.
-   **Frontend**:
    -   **경로**: `templates/inventory/log.html`
    -   **URL**: `/inventory/log/{materialId}`
    -   **주요 기능**:
        -   현재고, 적정 재고 표시 및 재고 조정 기능 모달 제공
        -   로그 유형(전체, 입고, 출고, 조정) 및 기간별 필터링
        -   로그 유형에 따라 상세 내역(출고 LOT, 조정 사유)을 볼 수 있는 팝업 제공
-   **Backend**:
    -   **Controller**: `InventoryController.java`
    -   **Method**: `logPage(...)`, `logs(...)`
    -   **Endpoints**: `GET /inventory/log/{materialId}` (SSR), `GET /inventory/logs` (API)
    -   **Service**: `InventoryLogViewService.getFilteredLogs()`
    -   **주요 로직**:
        -   `v_inventory_log` 뷰를 사용하여 입고/출고/조정 로그를 통합 조회
        -   유형, 기간 필터링 및 페이징 처리

### 2.6. 재고 배치(LOT) 현황 조회

-   **기능**: 특정 재료에 대해 현재 보유 중인 모든 LOT(배치)의 목록과 각 LOT의 잔량을 조회합니다.
-   **Frontend**:
    -   **경로**: `templates/inventory/batch_status.html`
    -   **URL**: `/inventory/batch-status/{materialId}`
    -   **주요 기능**:
        -   LOT 번호, 입고일, 유통기한, 입고수량, 잔량, 단가를 테이블로 표시
        -   각 LOT의 '출고 내역'을 상세 조회할 수 있는 버튼 제공
-   **Backend**:
    -   **Controller**: `InventoryController.java`
    -   **Method**: `batchStatusPage(...)`
    -   **Endpoint**: `GET /inventory/batch-status/{materialId}`
    -   **Service**: `InventoryBatchService.getBatchesByMaterial()`
    -   **주요 로직**:
        -   `materialId`로 `InventoryBatch` 목록을 입고일 내림차순으로 조회

### 2.7. 재고 배치(LOT) 상세 조회

-   **기능**: 특정 LOT의 상세 정보와 해당 LOT에서 출고된 모든 이력을 조회합니다.
-   **Frontend**:
    -   **경로**: `templates/inventory/batch.html`
    -   **URL**: `/inventory/batch/{batchId}`
    -   **주요 기능**:
        -   상단에 LOT의 기본 정보(입고일, 입고/현재 수량, 유통기한 등) 요약
        -   하단에 해당 LOT에서 차감된 출고 이력(출고일시, 가맹점, 출고 수량)을 테이블로 표시
-   **Backend**:
    -   **Controller**: `InventoryController.java`
    -   **Method**: `batchPage(...)`
    -   **Endpoint**: `GET /inventory/batch/{batchId}`
    -   **Services**: `InventoryBatchService.getLotDetail()`, `InventoryLotService.getOutLotHistory()`
    -   **주요 로직**:
        -   `batchId`로 `InventoryBatch` 정보를 조회하여 상단 요약 DTO 생성
        -   `batchId`를 기준으로 `InventoryOutLot`을 조회하여 출고 이력 페이징

### 2.8. 각종 엑셀 다운로드

-   **기능**: 재고 관련 다양한 목록과 이력을 Excel 파일로 다운로드합니다.
-   **Backend**:
    -   **Controller**: `InventoryRestController.java`
    -   **Service**: `InventoryService`
    -   **Endpoints**:
        -   `GET /API/inventory/download`: 본사 재고 목록
        -   `GET /API/inventory/{materialId}/log/download`: 재료별 재고 로그
        -   `GET /API/inventory/{materialId}/batch-status/download`: 재료별 LOT 현황
        -   `GET /API/inventory/batch/{batchId}/out-history/download`: 특정 LOT의 출고 이력

---
