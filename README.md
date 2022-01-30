# 요소수맵

## Live demo : https://yssmap.com/

![요소수맵](https://user-images.githubusercontent.com/62507373/151695704-e101eaae-ab60-4568-bd53-af3fa665b22e.png)

<br>

## 기능

### 주유소별 요소수 보유 현황 조회 서비스
#### - 지도에서 조회
#### - 목록으로 조회

<br>

## 업데이트 이력

<br>

### v 최초배포 : 2021.11.24
---
<br>

### v 업데이트 - 2021.11.26
#### 공공데이터포털 API 변경에 따른 데이터 적재 작업 개선
- 변경 사유 : 공공데이터포털에서 2021.11.29 부터 기존 베타버전 API가 종료되고 신규 API로 정식 서비스 제공 
#### 추가 개선사항
- 가격정보 추가
- 10분 단위로 데이터를 업데이트하는 배치작업 생성
  - 기존 베타버전 API는 하루 2번(14:00, 20:00) 업데이트된 정보를 제공해주어서 배치를 만들 필요성을 느끼지 못하였으나,
    신규 API는 5분 단위로 데이터가 업데이트 되기 때문에 보다 정확한 데이터를 신속하게 제공하기 위해 배치 작업 생성
  - 다만 서버 비용 문제로 동일하게 5분 단위로 하지는 못하였고 10분단위로 절충
---
<br>

### v업데이트 - 2021.12.08
#### API용 Controller 추가
- 추가 소스 경로 : src/main/java/com/broadenway/ureasolution/api 경로 밑에 api용 Controller 별도 구성
- 추가 사유 : 이 프로젝트를 이용해 프론트를 개발할 수 있도록 view를 반환하지 않고 데이터만 json으로 응답할 수 있는 기능 추가   
---
<br>

### v업데이트 - 2021.12.15
#### 공공데이터 API 호출 문제 개선
- 기존 문제점 : 초기 주유소 개수가 ```100```개 초반이여서 ```page=1&perpage=200```으로 API를 한번만 호출하여 데이터를 적재하였었는데
 API에서 제공하는 데이터가 많아지면서 현재는 ```960```개정도로 증가함
- 개선 방법 : 첫번째 API 호출시 리턴되는 totalCount로 총 페이지 번호를 계산하고, 페이지 번호를 올리면서 전체 데이터를 가져옴
  - page=1&```perPage=1000``` 과 같은 방식도 가능했지만, 한번에 너무 많은 데이터를 요청 시 양단 간 부하가 발생할 수 있는 문제점과
 주유소 수가 어디까지 증가할지 알 수 없기 때문에 추후 동일한 문제로 유지보수 하지 않아도 되도록 페이징 호출 이용
 
#### 로딩 속도 개선
- 기존 문제점 : 첫 화면 로딩시, 서버에서 전체 요소수 데이터를 조회하고 이를 view와 같이 내려보내주어 오래 걸리는 문제가 있었음
- 개선 방법
 1) 로딩시엔 view만 내려주고, ajax로 요소수 데이터를 조회하여 비동기로 주유소 마크를 랜더링하도록 변경
 2) 데이터가 어디까지 증가할 지 알 수 없기 때문에 현재 맵의 중심좌표를 기준으로 +- 특정 반경 범위 내 주유소들 데이터만 조회하여 랜더링할 수 있도록 개선

#### 업데이트 주기 변경
- 매시 30분 요소수 공공데이터를 받아와 업데이트 하도록 변경
  - 서버 비용 문제로 기존 10분 간격에서 변경함 
 
---
### v업데이트 - 2021.12.19
#### 현재 지도 바운더리 좌표값을 이용해 주유소 목록 조회하기 기능 추가
API 호출 정보 : /api/stations/bounds
- 개선 문제점 : center 좌표값을 이용해 반경에 있는 주유소 목록을 조회할 경우, zoom 변경에 따른 처리 불가
- 개선 방법 : 현재 지도의 바운더리 좌표값을 이용하여 바운더리 내의 주유소만 반환하도록 변경

---

### v업데이트 - 2022.1.4
#### 데이터 캐싱 적용
관련 이슈 : https://github.com/jerry92k/yssmap/issues/10

---

### v업데이트 - 2022.1.5
#### 페이징 적용
관련 이슈 : https://github.com/jerry92k/yssmap/issues/6

---

### v업데이트 - 2022.1.9
- Spring Batch, Quartz
  - 배치 작업 퀄리티 개선
  - 관련 이슈 : https://github.com/jerry92k/yssmap/issues/12
- 모듈 분리
  - 웹서비스 처리 : ```yssmap-main``` , 배치 처리 : ```yssmap-batch``` 모듈 분리  
  - 관련 이슈 : https://github.com/jerry92k/yssmap/issues/8

---
### v업데이트 - 2022.1.30
- deleted_at (삭제일시) 칼럼 추가
  - 유효하지 않은 데이터는 삭제일시를 업데이트
  - 클라이언트가 api를 요청하면, 삭제되지 않은 데이터만(삭제일시 없는) 반환
  - 관련 이슈 : https://github.com/jerry92k/yssmap/issues/16
- 배치건수 로깅
  - Spring Batch에서 처리하는 chunk 단위로 1) 신규건수 2) 변경된 건수 3) 변경없는 건수 로깅
  - 관련 이슈 : https://github.com/jerry92k/yssmap/issues/17
