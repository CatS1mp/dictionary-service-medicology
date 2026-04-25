# Thay đổi kiến trúc editor bài viết JSON

## Mục tiêu

Backend được refactor để hỗ trợ 2 màn FE mới:

- Màn chọn mẫu bài viết trước khi vào editor
- Màn editor + preview bài viết dạng cuộn dọc một cột

Trọng tâm là chuyển dữ liệu bài viết từ `contentMarkdown` sang `contentJson`, đồng thời bổ sung thư viện template và thư viện component để FE có đủ dữ liệu dựng editor động.

## Thay đổi entity

### 1. `Article`

`Article` không còn `themeId`.

Các trường chính sau refactor:

- `id`
- `name`
- `slug`
- `contentJson`
- `contentVersion`
- `contentMarkdown`
- `authorAdminId`
- `isPublished`
- `publishedAt`
- `createdAt`
- `updatedAt`

Ghi chú:

- `contentJson` là dữ liệu chính cho editor mới.
- `contentMarkdown` được giữ lại để tương thích dữ liệu cũ, nhưng không còn là nguồn dữ liệu chính.

### 2. `ArticleTemplate`

Entity mới dùng cho màn chọn mẫu bài viết.

Các trường:

- `id`
- `code`
- `name`
- `description`
- `defaultContentJson`
- `isActive`
- `createdAt`
- `updatedAt`

Vai trò:

- Template chỉ dùng để khởi tạo danh sách component ban đầu cho article.
- `Article` không lưu `templateId`.

### 3. `ComponentDefinition`

Entity mới map tới bảng `components`, dùng làm thư viện component cho editor.

Các trường:

- `id`
- `code`
- `name`
- `componentType`
- `schemaJson`
- `defaultDataJson`
- `isActive`
- `createdAt`
- `updatedAt`

Vai trò:

- `schemaJson` mô tả field mà FE cần render.
- `defaultDataJson` là dữ liệu mặc định khi thêm component vào article.

## API sau thay đổi

### 1. Article API

Giữ các endpoint hiện có:

- `POST /api/dictionary/articles`
- `GET /api/dictionary/articles`
- `GET /api/dictionary/articles/id/{id}`
- `GET /api/dictionary/articles/{slug}`
- `PUT /api/dictionary/articles/{id}`
- `DELETE /api/dictionary/articles/{id}`
- `POST /api/dictionary/articles/{id}/publish`
- `PATCH /api/dictionary/articles/{id}/unpublish`

Request/response mới của article:

- bỏ `themeId`
- thêm `contentJson`
- thêm `contentVersion`

### 2. Template API

API mới:

- `GET /api/dictionary/templates`
- `GET /api/dictionary/templates/{id}`
- `POST /api/dictionary/templates`
- `PUT /api/dictionary/templates/{id}`
- `DELETE /api/dictionary/templates/{id}`

FE màn chọn mẫu dùng:

- `GET /api/dictionary/templates?activeOnly=true`

### 3. Component API

API mới:

- `GET /api/dictionary/components`
- `GET /api/dictionary/components/{id}`
- `POST /api/dictionary/components`
- `PUT /api/dictionary/components/{id}`
- `DELETE /api/dictionary/components/{id}`

FE editor dùng:

- `GET /api/dictionary/components?activeOnly=true`

## Cấu trúc dữ liệu FE cần

### Dữ liệu article

```json
{
  "id": "uuid",
  "name": "Viêm phổi ở người lớn",
  "slug": "viem-phoi-o-nguoi-lon",
  "contentJson": "{\"version\":1,\"blocks\":[]}",
  "contentVersion": 1,
  "authorAdminId": "uuid",
  "isPublished": false,
  "publishedAt": null,
  "createdAt": "2026-04-19T00:00:00",
  "updatedAt": "2026-04-19T00:00:00",
  "tags": []
}
```

### Dữ liệu template

```json
{
  "id": "uuid",
  "code": "benh-hoc-co-ban",
  "name": "Mẫu bệnh học cơ bản",
  "description": "Mẫu khởi tạo cho bài bệnh học cơ bản",
  "defaultContentJson": "{\"version\":1,\"blocks\":[...]}",
  "isActive": true
}
```

### Dữ liệu component definition

```json
{
  "id": "uuid",
  "code": "media_basic",
  "name": "Tệp minh họa",
  "componentType": "media",
  "schemaJson": "{\"fields\":[...]}",
  "defaultDataJson": "{\"kind\":\"image\",\"url\":\"\",\"youtubeUrl\":\"\"}",
  "isActive": true
}
```

## Bộ component v1

Đợt đầu nên seed 7 component cốt lõi, đủ để dựng bài viết y khoa dạng cuộn dọc:

- `h1_basic`
- `h2_basic`
- `h3_basic`
- `paragraph_basic`
- `media_basic`
- `warning_basic`
- `term_box_basic`

Lý do chọn bộ này:

- đủ để xuất bản bài viết thật, không chỉ demo
- khớp với mock editor hiện tại
- hỗ trợ tốt use case y khoa: tiêu đề phân cấp, media, cảnh báo và thuật ngữ

File seed PostgreSQL đã được thêm tại:

- [seed-components-v1.sql](/C:/code/Medicology/dictionary-service-medicology/docs/sql/seed-components-v1.sql)

## Cách FE dùng dữ liệu

### Màn 1: Chọn mẫu bài viết

- Gọi `GET /api/dictionary/templates`
- Hiển thị card template từ:
  - `name`
  - `description`
  - `defaultContentJson`
- Khi chọn template:
  - copy `defaultContentJson`
  - chuyển sang màn editor

### Màn 2: Editor + preview

- Gọi `GET /api/dictionary/components`
- Dựng form động theo `schemaJson`
- Khi thêm component vào article:
  - copy `defaultDataJson`
  - chèn vào `contentJson.blocks`
- Preview lấy dữ liệu từ `contentJson`

## Kết luận

Sau refactor:

- Article không còn phụ thuộc `themeId`
- FE đã có đủ dữ liệu từ API để dựng 2 màn mới
- Template chỉ dùng ở bước khởi tạo
- Component library là nguồn định nghĩa field cho editor
